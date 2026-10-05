package app.eixo.ui.telas.painel

import androidx.compose.runtime.Composable
import app.eixo.ui.telas.comum.TelaEmConstrucao

/*
 * MÓDULO PAINEL (dashboard do administrador).
 * Os números já estão calculados: dominio/Indicadores.kt → calcularIndicadores(...).
 * Componentes: EixoOdometro, EixoResumoNumeros, EixoKpi, EixoCartaoEficiencia, EixoAlerta.
 * Tela de referência no design system: "Painel da frota".
 */

@Composable
fun PainelTela(
    barra: @Composable () -> Unit,
    aoSair: () -> Unit,
    aoAbrirViagens: () -> Unit,
) {
    TelaEmConstrucao(
        titulo = "Painel",
        modulo = "Painel",
        oQueFazer = "Odômetro com km rodados, caminhões por situação, KPIs do mês (gasto, litros, km/L, R$/km) e alertas de consumo fora da faixa.",
        barra = barra,
        aoSair = aoSair,
    )
}
