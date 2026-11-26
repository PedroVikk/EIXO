package app.eixo

import app.eixo.dados.Carga
import app.eixo.dados.Conversores
import app.eixo.dados.DaoEixo
import app.eixo.dados.LoginFirebase
import app.eixo.dados.RepositorioRoom
import app.eixo.dados.ResultadoLogin
import app.eixo.dados.Sessao
import app.eixo.dados.TipoCarga
import app.eixo.ui.telas.viagens.cidadeDe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/** DAO de mentira: guarda o que o repositório mandou gravar, para conferir. */
private class DaoFalso : DaoEixo<Carga> {
    val gravados = mutableListOf<Carga>()
    val removidos = mutableListOf<String>()
    override suspend fun todos() = gravados.toList()
    override suspend fun remover(id: String) { removidos += id }
    override suspend fun salvar(item: Carga) { gravados += item }
    override suspend fun salvarTodos(itens: List<Carga>) { gravados += itens }
}

class RepositorioRoomTest {
    private val carga = Carga("k9", "Milho", TipoCarga.GRAOS, 30_000, "999", "Cooperativa")

    @Test fun salvarAtualizaATelaNaHoraEGravaNoBanco() {
        val dao = DaoFalso()
        val repositorio = RepositorioRoom(emptyList(), Carga::id, dao, CoroutineScope(Dispatchers.Unconfined))
        repositorio.salvar(carga)
        assertEquals(listOf(carga), repositorio.itens.value)
        assertEquals(listOf(carga), dao.gravados)
    }

    @Test fun removerTiraDaListaEDoBanco() {
        val dao = DaoFalso()
        val repositorio = RepositorioRoom(listOf(carga), Carga::id, dao, CoroutineScope(Dispatchers.Unconfined))
        repositorio.remover("k9")
        assertTrue(repositorio.itens.value.isEmpty())
        assertEquals(listOf("k9"), dao.removidos)
    }
}

class ConversoresTest {
    private val conversores = Conversores()

    @Test fun dataIdaEVolta() {
        val data = LocalDate.of(2027, 10, 15)
        assertEquals("2027-10-15", conversores.deData(data))
        assertEquals(data, conversores.paraData("2027-10-15"))
    }

    @Test fun dataHoraIdaEVolta() {
        val dataHora = LocalDateTime.of(2026, 11, 26, 14, 30)
        assertEquals(dataHora, conversores.paraDataHora(conversores.deDataHora(dataHora)))
    }

    @Test fun nuloContinuaNulo() {
        assertNull(conversores.deData(null))
        assertNull(conversores.paraDataHora(null))
    }
}

/** Sem google-services.json o Firebase não é configurado e vale a senha de demonstração. */
class SessaoLocalTest {
    private fun entrar(login: String, senha: String): ResultadoLogin {
        var resultado: ResultadoLogin? = null
        Sessao.entrar(login, senha) { resultado = it }
        return resultado!!
    }

    @After fun sair() = Sessao.sair()

    @Test fun adminEntraPeloEmail() =
        assertTrue(entrar("admin@eixo.app", Sessao.SENHA_DEMO) is ResultadoLogin.Sucesso)

    @Test fun motoristaEntraPelaMatricula() =
        assertTrue(entrar(" 1001 ", Sessao.SENHA_DEMO) is ResultadoLogin.Sucesso)

    @Test fun senhaErradaELoginDesconhecidoSaoRecusados() {
        assertEquals(ResultadoLogin.CredenciaisInvalidas, entrar("1001", "000000"))
        assertEquals(ResultadoLogin.CredenciaisInvalidas, entrar("9999", Sessao.SENHA_DEMO))
    }

    @Test fun inativoTemMensagemPropria() =
        assertEquals(ResultadoLogin.AcessoInativo, entrar("1005", Sessao.SENHA_DEMO))
}

class FormatoLoginECidadeTest {
    @Test fun emailDoFirebase() {
        assertEquals("1001@eixo.app", LoginFirebase.emailDe(" 1001 "))
        assertEquals("admin@eixo.app", LoginFirebase.emailDe("Admin@Eixo.app"))
    }

    @Test fun cidadeSemAUf() {
        assertEquals("Goiânia", cidadeDe("Goiânia/GO"))
        assertEquals("", cidadeDe("/GO"))
        assertEquals("Goiânia", cidadeDe("Goiânia"))
    }
}
