package app.eixo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.eixo.ui.navegacao.EixoApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Toda tela do Eixo começa com o cabeçalho petróleo, nos dois temas: os ícones da
        // barra de status precisam ser SEMPRE claros. O padrão do Android os deixaria pretos
        // quando o celular está em tema claro — hora e bateria sumiriam sobre o petróleo.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent { EixoApp() }
    }
}
