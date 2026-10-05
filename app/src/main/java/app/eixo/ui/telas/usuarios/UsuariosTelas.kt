package app.eixo.ui.telas.usuarios

import androidx.compose.runtime.Composable
import app.eixo.ui.telas.comum.TelaEmConstrucao

/*
 * MÓDULO ACESSO — cadastro de motoristas e administradores.
 * Dados: Dados.usuarios (Repositorio<Usuario>). Motorista tem `motorista: DadosMotorista`.
 * Telas de referência no design system: "Novo acesso" (gestor).
 * Padrão a copiar: ui/telas/cargas/.
 */

/** Lista de pessoas com filtro por perfil (Motoristas | Administradores). */
@Composable
fun UsuariosTela(
    barra: @Composable () -> Unit,
    aoNovo: () -> Unit,
    aoAbrir: (usuarioId: String) -> Unit,
) {
    TelaEmConstrucao(
        titulo = "Pessoas",
        modulo = "Acesso",
        oQueFazer = "Listar motoristas e administradores com EixoLinhaLista, filtro por perfil com EixoSegmentado, e botão \"Novo cadastro\".",
        barra = barra,
    )
}

/**
 * Cadastro e edição. `usuarioId == null` = novo.
 * Campos comuns: nome, login (e-mail p/ admin, matrícula p/ motorista), perfil, ativo.
 * Só para motorista: CPF, CNH, categoria, validade da CNH (EixoCampoData), telefone.
 */
@Composable
fun UsuarioFormTela(usuarioId: String?, aoVoltar: () -> Unit) {
    TelaEmConstrucao(
        titulo = if (usuarioId == null) "Novo cadastro" else "Editar cadastro",
        modulo = "Acesso",
        oQueFazer = "Formulário com perfil (EixoSegmentado) que mostra os campos de motorista só quando o perfil for Motorista.",
        aoVoltar = aoVoltar,
    )
}
