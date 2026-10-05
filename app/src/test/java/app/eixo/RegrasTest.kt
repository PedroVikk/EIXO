package app.eixo

import androidx.compose.ui.text.AnnotatedString
import app.eixo.dados.Abastecimento
import app.eixo.dados.Caminhao
import app.eixo.dados.TipoCombustivel
import app.eixo.dominio.Classificacao
import app.eixo.dominio.classificarConsumo
import app.eixo.dominio.consumoDoCaminhao
import app.eixo.dominio.desvioPercentual
import app.eixo.ui.componentes.MascaraDecimal
import app.eixo.ui.componentes.MascaraKm
import app.eixo.ui.componentes.somenteDigitos
import app.eixo.util.decimalParaDigitos
import app.eixo.util.digitosParaDecimal
import app.eixo.util.formatarConsumo
import app.eixo.util.formatarDecimal
import app.eixo.util.formatarKm
import app.eixo.util.formatarMoeda
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class FormatosTest {
    @Test fun km() {
        assertEquals("152.430", formatarKm(152_430))
        assertEquals("0", formatarKm(0))
        assertEquals("1.000.000", formatarKm(1_000_000))
    }

    @Test fun dinheiroSempreComDuasCasas() {
        assertEquals("R$ 2.769,00", formatarMoeda(2769.0))
        assertEquals("R$ 0,05", formatarMoeda(0.05))
        assertEquals("R$ 7,10", formatarMoeda(2769.0 / 390.0))
    }

    @Test fun consumo() = assertEquals("1,96 km/L", formatarConsumo(1550.0 / 790.0))

    @Test fun idaEVoltaEntreDigitosEDecimal() {
        assertEquals(2769.0, digitosParaDecimal("276900"), 0.0001)
        assertEquals("276900", decimalParaDigitos(2769.0))
        assertEquals("", decimalParaDigitos(0.0))
    }

    @Test fun decimalNegativoUsaValorAbsoluto() = assertEquals("12,1", formatarDecimal(-12.1, 1))
}

class MascarasTest {
    private fun mostrar(m: androidx.compose.ui.text.input.VisualTransformation, t: String) = m.filter(AnnotatedString(t)).text.text

    @Test fun kmAgrupaMilhar() {
        assertEquals("152.430", mostrar(MascaraKm, "152430"))
        assertEquals("15", mostrar(MascaraKm, "15"))
    }

    @Test fun decimalPreencheDaDireita() {
        val m = MascaraDecimal(2)
        assertEquals("0,05", mostrar(m, "5"))
        assertEquals("2.769,00", mostrar(m, "276900"))
        assertEquals("", mostrar(m, ""))
    }

    @Test fun cursorNoFimContinuaNoFim() {
        val t = MascaraDecimal(2).filter(AnnotatedString("276900"))
        assertEquals(t.text.length, t.offsetMapping.originalToTransformed(6))
        assertEquals(6, t.offsetMapping.transformedToOriginal(t.text.length))
    }

    @Test fun teclaDescartaPontuacaoEZeroAEsquerda() {
        assertEquals("152430", somenteDigitos("0152.430", 7))
        assertEquals("1234567", somenteDigitos("123456789", 7))
    }
}

class ConsumoTest {
    private val caminhao = Caminhao("c1", "RTA4B12", "Scania R450", consumoEsperado = 2.00, kmAtual = 0)
    private fun abast(km: Long, litros: Double, cheio: Boolean = true, valor: Double = litros * 6.4, arla: Double? = null) =
        Abastecimento("a$km", null, "c1", "u1", LocalDateTime.now(), km, litros, valor, TipoCombustivel.DIESEL_S10, "Posto", cheio, arlaValor = arla)

    @Test fun primeiroAbastecimentoSoServeDeReferencia() {
        val r = consumoDoCaminhao(caminhao, listOf(abast(152_430, 400.0)))
        assertNull(r.consumo)
    }

    @Test fun mediaPonderadaNaoMediaDasMedias() {
        // trechos: 880 km / 450 L  e  670 km / 340 L  →  1550 / 790 = 1,962
        val r = consumoDoCaminhao(caminhao, listOf(abast(152_430, 400.0), abast(153_310, 450.0), abast(153_980, 340.0)))
        assertEquals(1550.0 / 790.0, r.consumo!!, 0.0001)
    }

    @Test fun tanqueParcialNaoFechaTrecho() {
        // o parcial no meio não fecha trecho; o trecho fecha no próximo tanque cheio
        val r = consumoDoCaminhao(caminhao, listOf(abast(1_000, 400.0), abast(1_300, 100.0, cheio = false), abast(1_900, 450.0)))
        assertEquals(900L, r.kmRodados)
        assertEquals(450.0, r.litros, 0.0001)
    }

    @Test fun arlaEntraNoCustoMasNaoNoConsumo() {
        val semArla = consumoDoCaminhao(caminhao, listOf(abast(0, 400.0), abast(800, 400.0, valor = 2_560.0)))
        val comArla = consumoDoCaminhao(caminhao, listOf(abast(0, 400.0), abast(800, 400.0, valor = 2_560.0, arla = 140.0)))
        assertEquals(semArla.consumo!!, comArla.consumo!!, 0.0)
        assertEquals(2_700.0 / 800.0, comArla.custoPorKm!!, 0.0001)
    }

    @Test fun classificacao() {
        assertEquals(Classificacao.NORMAL, classificarConsumo(2.10, 2.00))
        assertEquals(Classificacao.ATENCAO, classificarConsumo(1.69, 1.80))   // 93,9%
        assertEquals(Classificacao.CRITICO, classificarConsumo(1.23, 1.40))   // 87,9%
        assertEquals(-12.14, desvioPercentual(1.23, 1.40), 0.01)
    }
}
