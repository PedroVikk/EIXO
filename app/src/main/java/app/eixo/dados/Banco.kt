package app.eixo.dados

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate
import java.time.LocalDateTime

/*
 * Banco local do app (Room). O arquivo fica no próprio aparelho: os cadastros sobrevivem
 * a fechar e abrir o app. Para ver as tabelas: Android Studio → View → Tool Windows →
 * App Inspection → Database Inspector.
 */

/** Operações que todo repositório precisa; cada DAO completa com as consultas da sua tabela. */
interface DaoEixo<T> {
    suspend fun todos(): List<T>
    suspend fun remover(id: String)

    /** Insere se o id não existe; atualiza se existe. */
    @Upsert
    suspend fun salvar(item: T)

    @Upsert
    suspend fun salvarTodos(itens: List<T>)
}

@Dao
interface UsuarioDao : DaoEixo<Usuario> {
    @Query("SELECT * FROM usuarios")
    override suspend fun todos(): List<Usuario>

    @Query("DELETE FROM usuarios WHERE id = :id")
    override suspend fun remover(id: String)
}

@Dao
interface CaminhaoDao : DaoEixo<Caminhao> {
    @Query("SELECT * FROM caminhoes")
    override suspend fun todos(): List<Caminhao>

    @Query("DELETE FROM caminhoes WHERE id = :id")
    override suspend fun remover(id: String)
}

@Dao
interface CargaDao : DaoEixo<Carga> {
    @Query("SELECT * FROM cargas")
    override suspend fun todos(): List<Carga>

    @Query("DELETE FROM cargas WHERE id = :id")
    override suspend fun remover(id: String)
}

@Dao
interface ViagemDao : DaoEixo<Viagem> {
    @Query("SELECT * FROM viagens")
    override suspend fun todos(): List<Viagem>

    @Query("DELETE FROM viagens WHERE id = :id")
    override suspend fun remover(id: String)
}

@Dao
interface AbastecimentoDao : DaoEixo<Abastecimento> {
    @Query("SELECT * FROM abastecimentos")
    override suspend fun todos(): List<Abastecimento>

    @Query("DELETE FROM abastecimentos WHERE id = :id")
    override suspend fun remover(id: String)
}

/** Data e data-hora gravadas como texto ISO ("2026-11-26", "2026-11-26T14:30"): legível no inspetor. */
class Conversores {
    @TypeConverter fun deData(valor: LocalDate?): String? = valor?.toString()
    @TypeConverter fun paraData(valor: String?): LocalDate? = valor?.let(LocalDate::parse)
    @TypeConverter fun deDataHora(valor: LocalDateTime?): String? = valor?.toString()
    @TypeConverter fun paraDataHora(valor: String?): LocalDateTime? = valor?.let(LocalDateTime::parse)
}

@Database(
    entities = [Usuario::class, Caminhao::class, Carga::class, Viagem::class, Abastecimento::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Conversores::class)
abstract class EixoBanco : RoomDatabase() {
    abstract fun usuarios(): UsuarioDao
    abstract fun caminhoes(): CaminhaoDao
    abstract fun cargas(): CargaDao
    abstract fun viagens(): ViagemDao
    abstract fun abastecimentos(): AbastecimentoDao

    /** true só na primeira abertura, quando o arquivo do banco acabou de ser criado. */
    @Volatile
    var acabouDeSerCriado = false
        private set

    companion object {
        fun abrir(context: Context): EixoBanco {
            lateinit var banco: EixoBanco
            banco = Room.databaseBuilder(context, EixoBanco::class.java, "eixo.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        banco.acabouDeSerCriado = true
                    }
                })
                .build()
            return banco
        }
    }
}
