package app.eixo.dados

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Contrato de acesso a dados. As telas só conhecem esta interface.
 * Hoje a implementação é em memória; quando o Firebase entrar, basta criar
 * uma RepositorioFirestore<T> e trocar em `Dados` — nenhuma tela muda.
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
 * Ponto único de acesso aos dados do app. Use assim numa tela:
 * ```
 * val cargas by Dados.cargas.itens.collectAsState()
 * Dados.cargas.salvar(novaCarga)
 * ```
 * Os dados voltam ao estado inicial sempre que o app é reiniciado.
 */
object Dados {
    val usuarios: Repositorio<Usuario> = RepositorioEmMemoria(Semente.usuarios, Usuario::id)
    val caminhoes: Repositorio<Caminhao> = RepositorioEmMemoria(Semente.caminhoes, Caminhao::id)
    val cargas: Repositorio<Carga> = RepositorioEmMemoria(Semente.cargas, Carga::id)
    val viagens: Repositorio<Viagem> = RepositorioEmMemoria(Semente.viagens, Viagem::id)
    val abastecimentos: Repositorio<Abastecimento> = RepositorioEmMemoria(Semente.abastecimentos, Abastecimento::id)

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
}

/**
 * Login de mentira para a versão só-front. Qualquer usuário cadastrado entra com a
 * senha [SENHA_DEMO]. Admin entra pelo e-mail; motorista, pela matrícula.
 */
object Sessao {
    const val SENHA_DEMO = "123456"

    private val atual = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = atual.asStateFlow()

    fun entrar(login: String, senha: String): ResultadoLogin {
        val encontrado = Dados.usuarios.itens.value.firstOrNull { it.login.equals(login.trim(), ignoreCase = true) }
        return when {
            encontrado == null || senha != SENHA_DEMO -> ResultadoLogin.CredenciaisInvalidas
            !encontrado.ativo -> ResultadoLogin.AcessoInativo
            else -> {
                atual.value = encontrado
                ResultadoLogin.Sucesso(encontrado)
            }
        }
    }

    /** Atalho de desenvolvimento: entra direto com o primeiro usuário do perfil. */
    fun entrarComo(perfil: Perfil): Usuario? {
        val usuario = Dados.usuarios.itens.value.firstOrNull { it.perfil == perfil && it.ativo }
        atual.value = usuario
        return usuario
    }

    fun sair() {
        atual.value = null
    }
}
