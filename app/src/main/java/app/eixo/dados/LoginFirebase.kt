package app.eixo.dados

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

/**
 * Login pelo Firebase Authentication (e-mail e senha).
 *
 * O e-mail do administrador é o próprio login; o do motorista é a matrícula + "@eixo.app"
 * (1001 → 1001@eixo.app). Perfil e situação continuam no banco local — o Firebase só confere a senha.
 *
 * Conta que existe no app mas ainda não no Firebase é criada no primeiro acesso com a senha
 * inicial (Sessao.SENHA_DEMO). Assim os dados de exemplo e as pessoas cadastradas pelo
 * administrador entram sem ninguém precisar criar usuário no console.
 */
internal class LoginFirebase private constructor(private val auth: FirebaseAuth) {

    enum class Resposta { Aceito, Recusado, SemConexao, Falhou }

    fun entrar(email: String, senha: String, aoTerminar: (Resposta) -> Unit) {
        auth.signInWithEmailAndPassword(email, senha).addOnCompleteListener { tarefa ->
            val erro = tarefa.exception
            when {
                tarefa.isSuccessful -> aoTerminar(Resposta.Aceito)
                erro is FirebaseNetworkException -> aoTerminar(Resposta.SemConexao)
                // Com a proteção contra enumeração de e-mails (padrão em projetos novos), conta
                // inexistente e senha errada voltam o mesmo erro; tentar criar a conta desempata.
                senha == Sessao.SENHA_DEMO &&
                    (erro is FirebaseAuthInvalidCredentialsException || erro is FirebaseAuthInvalidUserException) ->
                    criarConta(email, senha, aoTerminar)
                erro is FirebaseAuthInvalidCredentialsException || erro is FirebaseAuthInvalidUserException ->
                    aoTerminar(Resposta.Recusado)
                else -> aoTerminar(Resposta.Falhou)
            }
        }
    }

    /** Cria a conta e já deixa logado. Se ela já existia, a senha digitada estava errada. */
    private fun criarConta(email: String, senha: String, aoTerminar: (Resposta) -> Unit) {
        auth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener { tarefa ->
            when {
                tarefa.isSuccessful -> aoTerminar(Resposta.Aceito)
                tarefa.exception is FirebaseNetworkException -> aoTerminar(Resposta.SemConexao)
                else -> aoTerminar(Resposta.Recusado)
            }
        }
    }

    fun sair() = auth.signOut()

    companion object {
        /** null quando o app foi compilado sem o google-services.json (Firebase não configurado). */
        fun seConfigurado(context: Context): LoginFirebase? =
            if (FirebaseApp.getApps(context).isEmpty()) null else LoginFirebase(FirebaseAuth.getInstance())

        fun emailDe(login: String): String = login.trim().lowercase().let { if ('@' in it) it else "$it@eixo.app" }
    }
}
