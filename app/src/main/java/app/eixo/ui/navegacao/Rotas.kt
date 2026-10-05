package app.eixo.ui.navegacao

import app.eixo.dados.Perfil

/**
 * Todas as rotas do app num lugar só. Para navegar, use as funções —
 * nunca monte a string da rota na mão dentro de uma tela.
 */
object Rotas {
    const val LOGIN = "login"
    const val CATALOGO = "catalogo"
    const val CONTA = "conta"

    const val PAINEL = "painel"

    const val USUARIOS = "usuarios"
    const val USUARIO_FORM = "usuario?id={id}"
    fun usuarioForm(id: String? = null) = if (id == null) "usuario" else "usuario?id=$id"

    const val CARGAS = "cargas"
    const val CARGA_FORM = "carga?id={id}"
    fun cargaForm(id: String? = null) = if (id == null) "carga" else "carga?id=$id"

    const val VIAGENS = "viagens"
    const val VIAGEM_FORM = "viagem/editar?id={id}"
    fun viagemForm(id: String? = null) = if (id == null) "viagem/editar" else "viagem/editar?id=$id"
    const val VIAGEM_DETALHE = "viagem/{id}"
    fun viagemDetalhe(id: String) = "viagem/$id"

    const val ABASTECIMENTOS = "abastecimentos"
    const val ABASTECIMENTO_FORM = "abastecimento?id={id}&viagemId={viagemId}"
    fun abastecimentoForm(id: String? = null, viagemId: String? = null): String {
        val params = listOfNotNull(id?.let { "id=$it" }, viagemId?.let { "viagemId=$it" })
        return if (params.isEmpty()) "abastecimento" else "abastecimento?" + params.joinToString("&")
    }

    /** Tela inicial depois do login. */
    fun inicioDe(perfil: Perfil) = when (perfil) {
        Perfil.ADMIN -> PAINEL
        Perfil.MOTORISTA -> VIAGENS
    }
}
