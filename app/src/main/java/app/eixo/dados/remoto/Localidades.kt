package app.eixo.dados.remoto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/*
 * Estados e cidades do Brasil, da API pública de localidades do IBGE (sem chave, sem conta).
 * Documentação: https://servicodados.ibge.gov.br/api/docs/localidades
 * Usada na origem e no destino da viagem.
 */

@Serializable
data class Estado(val sigla: String, val nome: String)

@Serializable
data class Municipio(val nome: String)

interface IbgeApi {
    @GET("estados?orderBy=nome")
    suspend fun estados(): List<Estado>

    @GET("estados/{uf}/municipios?orderBy=nome")
    suspend fun municipios(@Path("uf") uf: String): List<Municipio>
}

object Localidades {
    private const val ENDERECO = "https://servicodados.ibge.gov.br/api/v1/localidades/"

    // A resposta traz muito mais campo (região, microrregião...) do que o app usa.
    private val json = Json { ignoreUnknownKeys = true }

    private val cliente = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        // Mostra no Logcat cada requisição feita e o código da resposta.
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .build()

    private val api: IbgeApi = Retrofit.Builder()
        .baseUrl(ENDERECO)
        .client(cliente)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(IbgeApi::class.java)

    // A lista não muda durante o uso: busca uma vez e guarda.
    private var estadosEmCache: List<Estado>? = null
    private val municipiosEmCache = ConcurrentHashMap<String, List<String>>()

    /** Estados por ordem alfabética do nome. Lança exceção se não houver rede. */
    suspend fun estados(): List<Estado> =
        estadosEmCache ?: api.estados().also { estadosEmCache = it }

    /** Nomes das cidades da UF, em ordem alfabética. Lança exceção se não houver rede. */
    suspend fun municipios(uf: String): List<String> =
        municipiosEmCache[uf] ?: api.municipios(uf).map { it.nome }.also { municipiosEmCache[uf] = it }
}
