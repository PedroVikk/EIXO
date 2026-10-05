package app.eixo.dados

import java.time.LocalDate
import java.time.LocalDateTime

/*
 * Modelos do app. Nesta versão (só front) os dados vivem em memória —
 * ver Dados.kt. Os nomes dos campos já seguem o modelo que irá para o Firebase.
 */

enum class Perfil(val rotulo: String) {
    ADMIN("Administrador"),
    MOTORISTA("Motorista"),
}

/** Dados que só existem quando o usuário é motorista. */
data class DadosMotorista(
    val cpf: String,
    val cnh: String,
    val categoriaCnh: String,
    val validadeCnh: LocalDate,
    val telefone: String,
)

/**
 * Pessoa com acesso ao app. Admin e motorista são o mesmo modelo;
 * o motorista carrega `motorista` preenchido.
 *
 * @param login e-mail (admin) ou matrícula (motorista).
 */
data class Usuario(
    val id: String,
    val nome: String,
    val login: String,
    val perfil: Perfil,
    val ativo: Boolean = true,
    val motorista: DadosMotorista? = null,
)

/** Caminhões vêm prontos nesta versão — não há tela de cadastro de caminhão no escopo. */
data class Caminhao(
    val id: String,
    val placa: String,
    val modelo: String,
    val consumoEsperado: Double,
    val kmAtual: Long,
)

enum class TipoCarga(val rotulo: String) {
    GRAOS("Grãos"),
    GERAL("Carga geral"),
    REFRIGERADA("Refrigerada"),
    GRANEL_LIQUIDO("Granel líquido"),
    PERIGOSA("Perigosa"),
    OUTRA("Outra"),
}

data class Carga(
    val id: String,
    val descricao: String,
    val tipo: TipoCarga,
    val pesoKg: Long,
    val notaFiscal: String,
    val cliente: String,
    val observacao: String = "",
)

enum class StatusViagem(val rotulo: String) {
    PLANEJADA("Planejada"),
    EM_ANDAMENTO("Em andamento"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada"),
}

data class Viagem(
    val id: String,
    val numero: String,
    val caminhaoId: String,
    val motoristaId: String,
    val cargaId: String?,
    val origem: String,
    val destino: String,
    val saidaPrevista: LocalDateTime,
    val status: StatusViagem,
    val kmInicial: Long? = null,
    val kmFinal: Long? = null,
    val observacao: String = "",
) {
    val rota: String get() = "$origem → $destino"
    val kmPercorridos: Long? get() = if (kmInicial != null && kmFinal != null) kmFinal - kmInicial else null
}

enum class TipoCombustivel(val rotulo: String) {
    DIESEL_S10("Diesel S10"),
    DIESEL_S500("Diesel S500"),
    GASOLINA("Gasolina"),
    ETANOL("Etanol"),
    GNV("GNV"),
}

/**
 * Registro de abastecimento ("cadastro de combustível").
 * ARLA é opcional e entra no custo por km, mas NUNCA no cálculo de km/L.
 */
data class Abastecimento(
    val id: String,
    val viagemId: String?,
    val caminhaoId: String,
    val motoristaId: String,
    val dataHora: LocalDateTime,
    val km: Long,
    val litros: Double,
    val valorTotal: Double,
    val combustivel: TipoCombustivel,
    val posto: String,
    val tanqueCheio: Boolean,
    val arlaLitros: Double? = null,
    val arlaValor: Double? = null,
) {
    val precoLitro: Double get() = if (litros > 0) valorTotal / litros else 0.0
}
