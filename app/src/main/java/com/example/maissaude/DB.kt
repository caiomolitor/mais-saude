package com.example.maissaude

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Modelo para Agendamento
data class Agendamento(
    val id: Int,
    val usuario: String,
    val profissional: String,
    val especializacao: String,
    var data: String,
    var hora: String,
    val lembrete: Boolean,
    var observacoes: String?,
    val telefone: String
)

// Modelo para Evento
data class Evento(
    val id: Int,
    val nome: String,
    val descricao: String,
    val data: String,
    val hora: String,
    val local: String,
    val tipo: String
)

class DB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "saude.db"
        private const val DATABASE_VERSION = 10  // aumentada para forçar upgrade se necessário

        private const val TABLE_USERS = "usuarios"
        private const val TABLE_ESPECIALIZACOES = "especializacoes"
        private const val TABLE_PROFISSIONAIS = "profissionais"
        private const val TABLE_AGENDAMENTOS = "agendamentos"
        private const val TABLE_EVENTOS = "eventos"

        private const val COLUMN_ID = "id"
        private const val COLUMN_NOME = "nome"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_SENHA = "senha"
        private const val COLUMN_TIPO = "tipo"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db ?: return

        // Tabela de usuários
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOME TEXT,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_SENHA TEXT,
                $COLUMN_TIPO TEXT DEFAULT 'usuario'
            );
        """.trimIndent())
        insertInitialUsers(db)

        // Tabela de especializações
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_ESPECIALIZACOES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT
            );
        """.trimIndent())
        insertInitialEspecializacoes(db)

        // Tabela de profissionais
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_PROFISSIONAIS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT,
                especializacao_id INTEGER,
                FOREIGN KEY(especializacao_id) REFERENCES $TABLE_ESPECIALIZACOES(id)
            );
        """.trimIndent())
        insertInitialProfissionais(db)

        // Tabela de agendamentos
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_AGENDAMENTOS (
                nome_paciente TEXT,
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER,
                profissional_id INTEGER,
                data TEXT,
                hora TEXT,
                telefone TEXT,
                lembrete INTEGER DEFAULT 0,
                observacoes TEXT,
                FOREIGN KEY(usuario_id) REFERENCES $TABLE_USERS(id),
                FOREIGN KEY(profissional_id) REFERENCES $TABLE_PROFISSIONAIS(id)
            );
        """.trimIndent())

        // Tabela de eventos
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_EVENTOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                descricao TEXT,
                data TEXT NOT NULL,
                hora TEXT,
                local TEXT,
                tipo TEXT
            );
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db ?: return
        // Descarta tabelas antigas e recria tudo
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AGENDAMENTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFISSIONAIS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ESPECIALIZACOES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // =================== INSERÇÃO DE DADOS INICIAIS ===================
    private fun insertInitialUsers(db: SQLiteDatabase) {
        val usuariosIniciais = listOf(
            Triple("Sanderson", "sanderson@gmail.com", "admin"),
            Triple("Cliente", "cliente@saude.com", "usuario")
        )
        for ((nome, email, tipo) in usuariosIniciais) {
            val cursor = db.rawQuery("SELECT id FROM $TABLE_USERS WHERE $COLUMN_EMAIL=?", arrayOf(email))
            if (!cursor.moveToFirst()) {
                val values = ContentValues().apply {
                    put(COLUMN_NOME, nome)
                    put(COLUMN_EMAIL, email)
                    put(COLUMN_SENHA, "1234")
                    put(COLUMN_TIPO, tipo)
                }
                db.insert(TABLE_USERS, null, values)
            }
            cursor.close()
        }
    }

    private fun insertInitialEspecializacoes(db: SQLiteDatabase) {
        val especializacoes = listOf("Cardiologia", "Dermatologia", "Nutrição", "Fisioterapia")
        for (nome in especializacoes) {
            val cursor = db.rawQuery("SELECT id FROM $TABLE_ESPECIALIZACOES WHERE nome=?", arrayOf(nome))
            if (!cursor.moveToFirst()) {
                val values = ContentValues().apply { put("nome", nome) }
                db.insert(TABLE_ESPECIALIZACOES, null, values)
            }
            cursor.close()
        }
    }

    private fun insertInitialProfissionais(db: SQLiteDatabase) {
        val profissionais = listOf(
            Pair("Dr. João", "Cardiologia"),
            Pair("Dra. Maria", "Dermatologia"),
            Pair("Dr. Pedro", "Nutrição"),
            Pair("Dra. Ana", "Fisioterapia")
        )

        for ((nome, nomeEspecializacao) in profissionais) {
            // Pega o ID da especialização pelo nome
            val cursorEspec = db.rawQuery(
                "SELECT id FROM $TABLE_ESPECIALIZACOES WHERE nome=?",
                arrayOf(nomeEspecializacao)
            )
            var especializacaoId = 0
            if (cursorEspec.moveToFirst()) {
                especializacaoId = cursorEspec.getInt(cursorEspec.getColumnIndexOrThrow("id"))
            }
            cursorEspec.close()

            // Verifica se o profissional já existe
            val cursorProf = db.rawQuery(
                "SELECT id FROM $TABLE_PROFISSIONAIS WHERE nome=?",
                arrayOf(nome)
            )
            if (!cursorProf.moveToFirst()) {
                val values = ContentValues().apply {
                    put("nome", nome)
                    put("especializacao_id", especializacaoId)
                }
                db.insert(TABLE_PROFISSIONAIS, null, values)
            }
            cursorProf.close()
        }
    }


    // =================== FUNÇÕES DE USUÁRIO ===================
    fun validarLogin(email: String, senha: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM $TABLE_USERS WHERE $COLUMN_EMAIL=? AND $COLUMN_SENHA=?",
            arrayOf(email, senha)
        )
        val valido = cursor.moveToFirst()
        cursor.close()
        return valido
    }

    fun getAllUsers(): List<Triple<String, String, String>> {
        val lista = mutableListOf<Triple<String, String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_NOME, $COLUMN_EMAIL, $COLUMN_TIPO FROM $TABLE_USERS", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    Triple(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun insertUser(nome: String, email: String, senha: String, tipo: String = "usuario"): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOME, nome)
            put(COLUMN_EMAIL, email)
            put(COLUMN_SENHA, senha)
            put(COLUMN_TIPO, tipo)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    fun getNomeUsuario(email: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_NOME FROM $TABLE_USERS WHERE $COLUMN_EMAIL=?", arrayOf(email))
        val nome = if (cursor.moveToFirst()) cursor.getString(0) else ""
        cursor.close()
        return nome
    }

    fun getTipoUsuario(email: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_TIPO FROM $TABLE_USERS WHERE $COLUMN_EMAIL=?", arrayOf(email))
        val tipo = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        return tipo
    }

    // =================== FUNÇÕES DE AGENDAMENTO ===================
    fun salvarAgendamento(
        nomePaciente: String,
        usuarioId: Int,
        profissionalId: Int,
        data: String,
        hora: String,
        telefone: String,
        lembrete: Boolean,
        observacoes: String?
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nome_paciente", nomePaciente)
            put("usuario_id", usuarioId)
            put("profissional_id", profissionalId)
            put("data", data)
            put("hora", hora)
            put("telefone", telefone)
            put("lembrete", if (lembrete) 1 else 0)
            put("observacoes", observacoes)
        }
        val result = db.insert(TABLE_AGENDAMENTOS, null, values)
        db.close()
        return result != -1L
    }


    fun listarAgendamentos(): ArrayList<Agendamento> {
        val lista = arrayListOf<Agendamento>()
        val db = readableDatabase
        val query = """
            SELECT a.id, a.nome_paciente, p.nome, e.nome, a.data, a.hora, a.telefone, a.lembrete, a.observacoes
            FROM $TABLE_AGENDAMENTOS a
            JOIN $TABLE_USERS u ON a.usuario_id = u.id
            JOIN $TABLE_PROFISSIONAIS p ON a.profissional_id = p.id
            JOIN $TABLE_ESPECIALIZACOES e ON p.especializacao_id = e.id
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    Agendamento(
                        id = cursor.getInt(0),
                        usuario = cursor.getString(1),
                        profissional = cursor.getString(2),
                        especializacao = cursor.getString(3),
                        data = cursor.getString(4),
                        hora = cursor.getString(5),
                        telefone = cursor.getString(6),
                        lembrete = cursor.getInt(7) == 1,
                        observacoes = cursor.getString(8)

                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun deletarAgendamento(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete("agendamentos", "id = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun atualizarAgendamento(id: Int, novaData: String, novaHora: String, novasObs: String?): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("data", novaData)
            put("hora", novaHora)
            put("observacoes", novasObs)
        }
        val result = db.update("agendamentos", values, "id = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }


    fun verificarDisponibilidade(profissionalId: Int, data: String, hora: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM $TABLE_AGENDAMENTOS WHERE profissional_id=? AND data=? AND hora=?",
            arrayOf(profissionalId.toString(), data, hora)
        )
        val disponivel = !cursor.moveToFirst()
        cursor.close()
        return disponivel
    }

    fun getNomeProfissional(id: Int): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT nome FROM $TABLE_PROFISSIONAIS WHERE id=?", arrayOf(id.toString()))
        val nome = if (cursor.moveToFirst()) cursor.getString(0) else ""
        cursor.close()
        return nome
    }

    fun getHorariosOcupados(profissionalId: Int, data: String): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT hora FROM $TABLE_AGENDAMENTOS WHERE profissional_id = ? AND data = ?",
            arrayOf(profissionalId.toString(), data)
        )
        val horarios = mutableListOf<String>()
        if (cursor.moveToFirst()) {
            do {
                horarios.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return horarios
    }

    fun getProfissionalIdByEspecializacao(nomeEspecializacao: String): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT p.id 
        FROM $TABLE_PROFISSIONAIS p
        JOIN $TABLE_ESPECIALIZACOES e ON p.especializacao_id = e.id
        WHERE e.nome = ?
        LIMIT 1
        """.trimIndent(),
            arrayOf(nomeEspecializacao)
        )

        val profissionalId = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        } else {
            null
        }

        cursor.close()
        return profissionalId
    }


    // =================== FUNÇÕES DE EVENTOS ===================
    fun inserirEvento(nome: String, descricao: String, data: String, hora: String, local: String, tipo: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nome", nome)
            put("descricao", descricao)
            put("data", data)
            put("hora", hora)
            put("local", local)
            put("tipo", tipo)
        }
        val result = db.insert(TABLE_EVENTOS, null, values)
        db.close()
        return result != -1L
    }

    fun inserirEvento(evento: Evento): Boolean {
        return inserirEvento(evento.nome, evento.descricao, evento.data, evento.hora, evento.local, evento.tipo)
    }

    fun listarEventos(): List<Evento> {
        val lista = mutableListOf<Evento>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EVENTOS ORDER BY data ASC", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    Evento(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                        descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao")),
                        data = cursor.getString(cursor.getColumnIndexOrThrow("data")),
                        hora = cursor.getString(cursor.getColumnIndexOrThrow("hora")),
                        local = cursor.getString(cursor.getColumnIndexOrThrow("local")),
                        tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun verificarEventoExistente(nome: String, data: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_EVENTOS WHERE nome=? AND data=?",
            arrayOf(nome, data)
        )
        val existe = cursor.moveToFirst()
        cursor.close()
        return existe
    }

    fun deletarEvento(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_EVENTOS, "id=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }
}
