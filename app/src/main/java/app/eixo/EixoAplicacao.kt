package app.eixo

import android.app.Application
import app.eixo.dados.Dados

/** Roda uma vez por processo, antes de qualquer tela: abre o banco local e prepara o login. */
class EixoAplicacao : Application() {
    override fun onCreate() {
        super.onCreate()
        Dados.iniciar(this)
    }
}
