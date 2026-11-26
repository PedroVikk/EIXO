package app.eixo.dados

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
import java.util.concurrent.Executors

/**
 * Contrato de acesso a dados. As telas só conhecem esta interface.
 * No app, a implementação grava no banco local (RepositorioRoom); nos testes, fica em memória.
 */
interface Repositorio<T> {
    /** Lista observável: a tela se atualiza sozinha quando algo muda. */
    val itens: StateFlow<List<T>>
    fun buscar(id: String): T?
    /** Insere se o id não existe; atualiza se existe. */
    fun salvar(item: T)
    fun remover(id: String)
    fun novoId(): String = UUID.randomUUID().toString()
}

class RepositorioEmMemoria<T>(
    inicial: List<T>,
    private val idDe: (T) -> String,
) : Repositorio<T> {
    private val estado = MutableStateFlow(inicial)
    override val itens: StateFlow<List<T>> = estado.asStateFlow()

    override fun buscar(id: String): T? = estado.value.firstOrNull { idDe(it) == id }

    override fun salvar(item: T) = estado.update { lista ->
        val indice = lista.indexOfFirst { idDe(it) == idDe(item) }
        if (indice >= 0) lista.toMutableList().also { it[indice] = item } else lista + item
    }

    override fun remover(id: String) = estado.update { lista -> lista.filterNot { idDe(it) == id } }
}

/**
 * Lista em memória (a tela lê na hora, sem esperar o disco) que grava cada mudança no Room.
 * As gravações rodam em fila, uma por vez, fora da thread da tela.
 */
class RepositorioRoom<T>(
    inicial: List<T>,
    idDe: (T) -> String,
    private val dao: DaoEixo<T>,
    private val gravacao: CoroutineScope,
) : Repositorio<T> {
    private val memoria = RepositorioEmMemoria(inicial, idDe)
    override val itens: StateFlow<List<T>> = memoria.itens

    override fun buscar(id: String): T? = memoria.buscar(id)

    override fun salvar(item: T) {
        memoria.salvar(item)
        gravacao.launch { dao.salvar(item) }
    }

    override fun remover(id: String) {
        memoria.remover(id)
        gravacao.launch { dao.remover(id) }
    }
}

/**
 * Ponto único de acesso aos dados do app. Use assim numa tela:
 * ```
 * val cargas by Dados.cargas.itens.collectAsState()
 * Dados.cargas.salvar(novaCarga)
 * ```
 * Os cadastros ficam gravados no aparelho (Room). Na primeira abertura o banco nasce com
 * os dados de exemplo; para voltar a eles, limpe os dados do app nas configurações do Android.
 */
object Dados {
    // Em memória até `iniciar` (testes e prévias). No app, `iniciar` troca pelos repositórios do Room.
    var usuarios: Repositorio<Usuario> = RepositorioEmMemoria(Semente.usuarios, Usuario::id)
        private set
    var caminhoes: Repositorio<Caminhao> = RepositorioEmMemoria(Semente.caminhoes, Caminhao::id)
        private set
    var cargas: Repositorio<Carga> = RepositorioEmMemoria(Semente.cargas, Carga::id)
        private set
    var viagens: Repositorio<Viagem> = RepositorioEmMemoria(Semente.viagens, Viagem::id)
        private set
    var abastecimentos: Repositorio<Abastecimento> = RepositorioEmMemoria(Semente.abastecimentos, Abastecimento::id)
        private set

    private var banco: EixoBanco? = null

    /**
     * Abre o banco e carrega os cadastros. Chamado uma vez, ao abrir o app (EixoAplicacao).
     * Carrega antes da primeira tela: são poucos registros, e assim toda tela já encontra os dados.
     */
    fun iniciar(context: Context) {
        if (banco != null) return
        val b = EixoBanco.abrir(context.applicationContext)
        banco = b
        val gravacao = CoroutineScope(SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        runBlocking(Dispatchers.IO) {
            b.openHelper.writableDatabase // abre o arquivo; na primeira vez, cria as tabelas
            if (b.acabouDeSerCriado) {
                b.usuarios().salvarTodos(Semente.usuarios)
                b.caminhoes().salvarTodos(Semente.caminhoes)
                b.cargas().salvarTodos(Semente.cargas)
                b.viagens().salvarTodos(Semente.viagens)
                b.abastecimentos().salvarTodos(Semente.abastecimentos)
            }
            usuarios = RepositorioRoom(b.usuarios().todos(), Usuario::id, b.usuarios(), gravacao)
            caminhoes = RepositorioRoom(b.caminhoes().todos(), Caminhao::id, b.caminhoes(), gravacao)
            cargas = RepositorioRoom(b.cargas().todos(), Carga::id, b.cargas(), gravacao)
            viagens = RepositorioRoom(b.viagens().todos(), Viagem::id, b.viagens(), gravacao)
            abastecimentos = RepositorioRoom(b.abastecimentos().todos(), Abastecimento::id, b.abastecimentos(), gravacao)
        }
        Sessao.configurar(context.applicationContext)
    }

    /** Próximo número de viagem com seis dígitos: "000129". */
    fun proximoNumeroViagem(): String {
        val maior = viagens.itens.value.mapNotNull { it.numero.toIntOrNull() }.maxOrNull() ?: 0
        return (maior + 1).toString().padStart(6, '0')
    }

    /** Último KM conhecido do caminhão — usado para pré-preencher campos de KM. */
    fun ultimoKm(caminhaoId: String): Long? {
        val doAbastecimento = abastecimentos.itens.value.filter { it.caminhaoId == caminhaoId }.maxOfOrNull { it.km }
        val doCadastro = caminhoes.buscar(caminhaoId)?.kmAtual
        return listOfNotNull(doAbastecimento, doCadastro).maxOrNull()
    }
}

sealed interface ResultadoLogin {
    data class Sucesso(val usuario: Usuario) : ResultadoLogin
    data object CredenciaisInvalidas : ResultadoLogin
    data object AcessoInativo : ResultadoLogin
    /** Não deu para conferir a senha agora (sem internet ou erro no Firebase). */
    data class Falha(val mensagem: String) : ResultadoLogin
}

/**
 * Sessão de quem está usando o app. Admin entra pelo e-mail; motorista, pela matrícula.
 *
 * Com o Firebase configurado (google-services.json em app/), a senha é conferida no
 * Firebase Authentication — ver LoginFirebase. Sem ele, qualquer usuário cadastrado entra
 * com a senha [SENHA_DEMO].
 */
object Sessao {
    const val SENHA_DEMO = "123456"

    private val atual = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = atual.asStateFlow()

    private var firebase: LoginFirebase? = null

    /** true quando o login passa pelo Firebase. */
    val usaFirebase: Boolean get() = firebase != null

    internal fun configurar(context: Context) {
        firebase = LoginFirebase.seConfigurado(context)
    }

    /** O resultado chega em [aoTerminar], na thread da tela. Com Firebase, depende da rede. */
    fun entrar(login: String, senha: String, aoTerminar: (ResultadoLogin) -> Unit) {
        val encontrado = Dados.usuarios.itens.value.firstOrNull { it.login.equals(login.trim(), ignoreCase = true) }
        val auth = firebase
        when {
            encontrado == null -> aoTerminar(ResultadoLogin.CredenciaisInvalidas)
            auth == null -> aoTerminar(if (senha == SENHA_DEMO) liberar(encontrado) else ResultadoLogin.CredenciaisInvalidas)
            else -> auth.entrar(LoginFirebase.emailDe(encontrado.login), senha) { resposta ->
                aoTerminar(
                    when (resposta) {
                        LoginFirebase.Resposta.Aceito -> liberar(encontrado)
                        LoginFirebase.Resposta.Recusado -> ResultadoLogin.CredenciaisInvalidas
                        LoginFirebase.Resposta.SemConexao ->
                            ResultadoLogin.Falha("Sem conexão com a internet. Confira a rede e tente de novo.")
                        LoginFirebase.Resposta.Falhou ->
                            ResultadoLogin.Falha("Não foi possível conferir a senha agora. Tente de novo em instantes.")
                    },
                )
            }
        }
    }

    /** Senha conferida: só falta ver se o acesso está ativo. */
    private fun liberar(usuario: Usuario): ResultadoLogin {
        if (!usuario.ativo) {
            firebase?.sair()
            return ResultadoLogin.AcessoInativo
        }
        atual.value = usuario
        return ResultadoLogin.Sucesso(usuario)
    }

    /** Atalho da demonstração: entra com o primeiro usuário ativo do perfil e a senha padrão. */
    fun entrarComo(perfil: Perfil, aoTerminar: (ResultadoLogin) -> Unit) {
        val usuario = Dados.usuarios.itens.value.firstOrNull { it.perfil == perfil && it.ativo }
            ?: return aoTerminar(ResultadoLogin.CredenciaisInvalidas)
        entrar(usuario.login, SENHA_DEMO, aoTerminar)
    }

    fun sair() {
        firebase?.sair()
        atual.value = null
    }
}
