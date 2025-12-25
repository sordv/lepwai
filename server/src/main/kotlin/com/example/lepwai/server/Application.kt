package com.example.lepwai.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.dao.id.IntIdTable
import java.time.Instant
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.serialization.json.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.client.statement.bodyAsText
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.io.File
import javax.net.ssl.*
import java.security.SecureRandom
import java.security.cert.X509Certificate
import ai.LocalAiClient
import kotlinx.coroutines.launch

object Users : Table("users") {
    val login = varchar("login", 30)
    val password = varchar("password", 60)
    override val primaryKey = PrimaryKey(login)
}

object Courses : Table("courses") {
    val id = integer("id")
    val name = varchar("name", 30)
    val sort = integer("sort")
    override val primaryKey = PrimaryKey(id)
}

object Topics : Table("topics") {
    val id = integer("id")
    val name = varchar("name", 60)
    val sort = integer("sort")
    val parent = integer("parent")
    override val primaryKey = PrimaryKey(id)
}

object Levels : Table("levels") {
    val id = integer("id")
    val name = varchar("name", 60)
    val sort = integer("sort")
    val parent = integer("parent")
    val value = text("value")
    val difficulty = integer("difficulty").nullable()
    val input1 = text("input1").nullable()
    val input2 = text("input2").nullable()
    val answer1 = text("answer1").nullable()
    val answer2 = text("answer2").nullable()
    override val primaryKey = PrimaryKey(id)
}

object Chats : IntIdTable("chats") {
    val userLogin = varchar("user_login", 30)
    val title = text("title")
}

object Messages : IntIdTable("messages") {
    val chatId = integer("chat_id")
    val isUserMsg = bool("is_user_msg")
    val content = text("content")
}

object UserLevelProgress : IntIdTable("user_level_progress") {
    val userLogin = varchar("user_login", 30)
    val levelId = integer("level_id")
    val status = varchar("status", 15)
    val answer = text("answer").nullable()
}

@Serializable data class Health(val status: String, val ts: String)
@Serializable data class DbTestResult(val ok: Boolean, val result: Int?, val error: String?)

@Serializable data class RegisterRequest(val login: String, val password: String, val passwordRepeat: String)
@Serializable data class RegisterResponse(val ok: Boolean, val error: String? = null)
@Serializable data class LoginRequest(val login: String, val password: String)
@Serializable data class LoginResponse(val ok: Boolean, val error: String? = null, val login: String? = null)

@kotlinx.serialization.Serializable data class CourseDTO(val id: Int, val name: String, val sort: Int)
@kotlinx.serialization.Serializable data class TopicDTO(val id: Int, val name: String, val sort: Int, val parent: Int)
@kotlinx.serialization.Serializable data class LevelDTO(val id: Int, val name: String, val sort: Int, val parent: Int, val value: String, val input1: String?, val input2: String?, val difficulty: Int?)

@Serializable data class LevelProgressDto(val levelId: Int, val status: String, val answer: String? = null)
@Serializable data class RunPracticeRequest(val login: String, val levelId: Int, val code: String)
@Serializable data class RunPracticeResponse(val status: String, val output: String)

@Serializable data class CompleteLevelRequest(val login: String, val levelId: Int)
@Serializable data class ChatDto(val id: Int, val title: String)
@Serializable data class MessageDto(val id: Int, val isUserMsg: Boolean, val content: String)
@Serializable data class SendMessageRequest(val chatId: Int?, val message: String)
@Serializable data class SendMessageResponse(val chatId: Int, val messages: List<MessageDto>)

fun hikariDataSource(
    host: String,
    port: Int,
    database: String,
    user: String,
    password: String
): HikariDataSource {
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://$host:$port/$database"
        username = user
        this.password = password
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }
    return HikariDataSource(config)
}

fun connectToDatabase(
    host: String, port: Int, database: String, user: String, password: String
): Database {
    val ds = hikariDataSource(host, port, database, user, password)
    return Database.connect(ds)
}

fun main() {
    val dbHost = System.getenv("DB_HOST") ?: "127.0.0.1"
    val dbPort = (System.getenv("DB_PORT") ?: "5432").toInt()
    val dbName = System.getenv("DB_NAME") ?: "lepwai"
    val dbUser = System.getenv("DB_USER") ?: "postgres"
    val dbPassword = System.getenv("DB_PASSWORD") ?: "admin"

    val httpPort = (System.getenv("PORT") ?: "8080").toInt()

    // попытка подключения к БД
    val db: Database? = try {
        val d = connectToDatabase(dbHost, dbPort, dbName, dbUser, dbPassword)
        println("Connected to DB $dbName at $dbHost:$dbPort as $dbUser")
        d
    } catch (t: Throwable) {
        t.printStackTrace()
        println("WARNING: Cannot connect to DB: ${t.message}")
        null
    }

    embeddedServer(Netty, port = httpPort) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            )
        }
        routing {
            get("/") {
                call.respondText("lepwai Ktor server running")
            }

            get("/health") {
                call.respond(Health("ok", Instant.now().toString()))
            }

            get("/dbtest") {
                if (db == null) {
                    call.respond(DbTestResult(false, null, "No DB connection configured or connection failed"))
                    return@get
                }

                try {
                    val value = transaction(db) {
                        val rs = exec("SELECT 1") { rs ->
                            if (rs.next()) rs.getInt(1) else null
                        }
                        rs ?: 0
                    }
                    call.respond(DbTestResult(true, value, null))
                } catch (t: Throwable) {
                    call.respond(DbTestResult(false, null, t.message))
                }
            }

            post("/echo") {
                val body = call.receiveText()
                call.respondText("echo: $body")
            }

            post("/register") {
                val req = call.receive<RegisterRequest>()

                if (req.login.isBlank() || req.login.length > 30) {
                    call.respond(HttpStatusCode.BadRequest, RegisterResponse(false, "Логин должен быть не пустым и до 30 символов"))
                    return@post
                }
                if (req.password.length < 8 || req.password.length > 30) {
                    call.respond(HttpStatusCode.BadRequest, RegisterResponse(false, "Пароль должен быть от 8 до 30 символов"))
                    return@post
                }
                if (req.password != req.passwordRepeat) {
                    call.respond(HttpStatusCode.BadRequest, RegisterResponse(false, "Пароли не совпадают"))
                    return@post
                }

                try {
                    val existed = transaction {
                        Users.select { Users.login eq req.login }.count() > 0
                    }
                    if (existed) {
                        call.respond(HttpStatusCode.Conflict, RegisterResponse(false, "Логин уже занят"))
                        return@post
                    }
                    val hashed = BCrypt.withDefaults().hashToString(10, req.password.toCharArray())

                    transaction {
                        Users.insert {
                            it[login] = req.login
                            it[password] = hashed
                        }
                    }
                    call.respond(HttpStatusCode.OK, RegisterResponse(true, null))
                } catch (t: Throwable) {
                    call.respond(HttpStatusCode.InternalServerError, RegisterResponse(false, "Ошибка на сервере: ${t.message}"))
                }
            }

            post("/login") {
                val req = call.receive<LoginRequest>()
                try {
                    val userVerified  = transaction {
                        val row = Users.select { Users.login eq req.login }.limit(1).firstOrNull()
                        if (row == null) {
                            false
                        } else {
                            val storedHash = row[Users.password]
                            val result = BCrypt.verifyer().verify(req.password.toCharArray(), storedHash)
                            result.verified
                        }
                    }
                    if (userVerified) {
                        call.respond(HttpStatusCode.OK, LoginResponse(true, null, req.login))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, LoginResponse(false, "Неверный логин или пароль"))
                    }
                } catch (t: Throwable) {
                    call.respond(HttpStatusCode.InternalServerError, LoginResponse(false, "Ошибка на сервере: ${t.message}"))
                }
            }

            get("/courses") {
                if (db == null) {
                    call.respond(HttpStatusCode.InternalServerError, "DB connection is not configured")
                    return@get
                }
                try {
                    val courses = transaction(db) {
                        Courses.selectAll()
                            .orderBy(Courses.sort to SortOrder.ASC)
                            .map { row ->
                                CourseDTO(
                                    id = row[Courses.id],
                                    name = row[Courses.name],
                                    sort = row[Courses.sort]
                                )
                            }
                    }
                    call.respond(courses)
                } catch (t: Throwable) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (t.message ?: "unknown")))
                }
            }

            get("/courses/{courseId}/topics") {
                if (db == null) {
                    call.respond(HttpStatusCode.InternalServerError, "DB connection missing")
                    return@get
                }

                val courseId = call.parameters["courseId"]?.toIntOrNull()
                if (courseId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Bad courseId")
                    return@get
                }

                try {
                    val topics = transaction(db) {
                        Topics.select { Topics.parent eq courseId }
                            .orderBy(Topics.sort to SortOrder.ASC)
                            .map { row ->
                                TopicDTO(
                                    id = row[Topics.id],
                                    name = row[Topics.name],
                                    sort = row[Topics.sort],
                                    parent = row[Topics.parent]
                                )
                            }
                    }
                    call.respond(topics)
                } catch (t: Throwable) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (t.message ?: "unknown"))
                    )
                }
            }

            get("/topics/{topicId}/levels") {
                if (db == null) {
                    call.respond(HttpStatusCode.InternalServerError, "DB connection missing")
                    return@get
                }

                val topicId = call.parameters["topicId"]?.toIntOrNull()
                if (topicId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Bad topicId")
                    return@get
                }

                try {
                    val levels = transaction(db) {
                        Levels.select { Levels.parent eq topicId }
                            .orderBy(Levels.sort to SortOrder.ASC)
                            .map { row ->
                                LevelDTO(
                                    id = row[Levels.id],
                                    name = row[Levels.name],
                                    sort = row[Levels.sort],
                                    parent = row[Levels.parent],
                                    value = row[Levels.value],
                                    input1 = row[Levels.input1],
                                    input2 = row[Levels.input2],
                                    difficulty = row[Levels.difficulty]
                                )
                            }
                    }
                    call.respond(levels)
                } catch (t: Throwable) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (t.message ?: "unknown"))
                    )
                }
            }

            get("/levels/{levelId}") {
                if (db == null) {
                    call.respond(HttpStatusCode.InternalServerError, "DB connection missing")
                    return@get
                }

                val levelId = call.parameters["levelId"]?.toIntOrNull()
                if (levelId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Bad levelId")
                    return@get
                }

                try {
                    val level = transaction(db) {
                        Levels.select { Levels.id eq levelId }
                            .limit(1)
                            .map { row ->
                                LevelDTO(
                                    id = row[Levels.id],
                                    name = row[Levels.name],
                                    sort = row[Levels.sort],
                                    parent = row[Levels.parent],
                                    value = row[Levels.value],
                                    input1 = row[Levels.input1],
                                    input2 = row[Levels.input2],
                                    difficulty = row[Levels.difficulty]
                                )
                            }
                            .firstOrNull()
                    }

                    if (level == null) {
                        call.respond(HttpStatusCode.NotFound, "Level not found")
                    } else {
                        call.respond(level)
                    }
                } catch (t: Throwable) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (t.message ?: "unknown"))
                    )
                }
            }

            get("/user/{login}/progress") {
                val login = call.parameters["login"]!!

                val progress = transaction {
                    UserLevelProgress
                        .select { UserLevelProgress.userLogin eq login }
                        .map {
                            LevelProgressDto(
                                levelId = it[UserLevelProgress.levelId],
                                status = it[UserLevelProgress.status],
                                answer = it[UserLevelProgress.answer]
                            )
                        }
                }

                call.respond(progress)
            }

            post("/levels/complete") {
                val req = call.receive<CompleteLevelRequest>()

                transaction {
                    val exists = UserLevelProgress.select {
                        (UserLevelProgress.userLogin eq req.login) and
                                (UserLevelProgress.levelId eq req.levelId)
                    }.firstOrNull()

                    if (exists == null) {
                        UserLevelProgress.insert {
                            it[userLogin] = req.login
                            it[levelId] = req.levelId
                            it[status] = "done"
                            it[answer] = null
                        }
                    }
                }

                call.respond(HttpStatusCode.OK)
            }

            post("/levels/run-practice") {
                val req = call.receive<RunPracticeRequest>()

                val level = transaction {
                    Levels.select { Levels.id eq req.levelId }.first()
                }

                val language = detectLanguageByLevelId(db!!, req.levelId)
                //println("RUN PRACTICE: level=${req.levelId}, language=$language")

                fun runTest(input: String?, expected: String): Pair<String, Boolean> {
                    val fullCode = "${input ?: ""}\n${req.code}"

                    val (out, ok, compileError) = runCode(
                        code = fullCode,
                        language = language,
                        expectedOutput = expected
                    )

                    if (compileError) {
                        throw RuntimeException(out.ifBlank { "Ошибка выполнения" })
                    }

                    return out to ok
                }

                try {
                    val (out1, ok1) = runTest(level[Levels.input1], level[Levels.answer1]!!)
                    if (!ok1) {
                        call.respond(RunPracticeResponse("wrong_answer", out1))
                        return@post
                    }

                    val (out2, ok2) = runTest(level[Levels.input2], level[Levels.answer2]!!)
                    if (!ok2) {
                        call.respond(RunPracticeResponse("hidden_failed", out1))
                        return@post
                    }

                    transaction(db!!) {
                        UserLevelProgress.insertIgnore {
                            it[userLogin] = req.login
                            it[levelId] = req.levelId
                            it[status] = "done"
                            it[answer] = req.code
                        }
                    }

                    call.respond(RunPracticeResponse("success", out1))

                } catch (e: RuntimeException) {
                    call.respond(RunPracticeResponse("compile_error", e.message ?: "Ошибка"))
                }
            }

            get("/chat/list") {
                val login = call.request.headers["X-User-Login"]!!

                call.respond(transaction {
                    Chats
                        .select { Chats.userLogin eq login }
                        .orderBy(Chats.id, SortOrder.DESC)
                        .map {
                            ChatDto(
                                it[Chats.id].value,
                                it[Chats.title]
                            )
                        }
                })
            }

            get("/chat/{id}") {
                val chatId = call.parameters["id"]!!.toInt()
                call.respond(transaction {
                    Messages.select { Messages.chatId eq chatId }
                        .orderBy(Messages.id)
                        .map {
                            MessageDto(
                                it[Messages.id].value,
                                it[Messages.isUserMsg],
                                it[Messages.content]
                            )
                        }
                })
            }

            post("/chat/send") {
                println("DEBUG: Received chat/send request")
                val login = call.request.headers["X-User-Login"]!!
                println("DEBUG: User login: $login")

                val req = call.receive<SendMessageRequest>()
                println("DEBUG: Request: chatId=${req.chatId}, message=${req.message.take(50)}...")

                // 1. создаём чат если нужно
                val chatId = req.chatId ?: transaction {
                    Chats.insertAndGetId {
                        it[userLogin] = login
                        it[title] = req.message.take(30)
                    }.value
                }

                // 2. сохраняем сообщение пользователя
                transaction {
                    Messages.insert {
                        it[Messages.chatId] = chatId
                        it[isUserMsg] = true
                        it[content] = req.message
                    }
                }

                // 3. отвечаем клиенту СРАЗУ (он ничего не ждёт)
                call.respond(HttpStatusCode.OK, mapOf("chatId" to chatId))

                // 3. получаем ответ бота (или текст ошибки)
                println("DEBUG: Asking Local AI (Ollama)...")

                // 4. ИИ — в фоне
                launch {
                    try {
                        val history = transaction {
                            Messages
                                .select { Messages.chatId eq chatId }
                                .orderBy(Messages.id)
                                .map {
                                    ai.LocalAiClient.Message(
                                        role = if (it[Messages.isUserMsg]) "user" else "assistant",
                                        content = it[Messages.content]
                                    )
                                }
                        }

                        val botReply = LocalAiClient.ask(history)

                        transaction {
                            Messages.insert {
                                it[Messages.chatId] = chatId
                                it[isUserMsg] = false
                                it[content] = botReply
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            delete("/chat/{id}") {
                val id = call.parameters["id"]!!.toInt()
                transaction {
                    Messages.deleteWhere { chatId eq id }
                    Chats.deleteWhere { Chats.id eq id }
                }
                call.respond(HttpStatusCode.OK)
            }

            get("/ai/test") {
                try {
                    val answer = LocalAiClient.ask(
                        listOf(
                            ai.LocalAiClient.Message(
                                role = "user",
                                content = "Объясни что такое if else простыми словами"
                            )
                        )
                    )
                    call.respondText(answer)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondText(
                        "ERROR: ${e::class.simpleName}\n${e.message}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }
    }.start(wait = true)
}

fun runCode(
code: String,
language: String,
expectedOutput: String
): Triple<String, Boolean, Boolean> {

    val file = when (language) {
        "python" -> File.createTempFile("code", ".py")
        "javascript" -> File.createTempFile("code", ".js")
        else -> error("Unknown language")
    }

    file.writeText(code)

    val process = try {
        when (language) {
            "python" -> ProcessBuilder("python", file.absolutePath)
            "javascript" -> ProcessBuilder("node", file.absolutePath)
            else -> error("Unknown language")
        }
            .redirectErrorStream(true)
            .start()
    } catch (e: Exception) {
        file.delete()
        return Triple(e.message ?: "Compilation error", false, true)
    }

    val output = process.inputStream.bufferedReader().readText()

    val exitCode = process.waitFor()

    file.delete()

    val compileError = exitCode != 0
    val success = !compileError && output.trim() == expectedOutput.trim()

    return Triple(output, success, compileError)
}

fun detectLanguageByLevelId(db: Database, levelId: Int): String {
    return transaction(db) {

        val topicId = Levels
            .slice(Levels.parent)
            .select { Levels.id eq levelId }
            .first()[Levels.parent]

        val courseId = Topics
            .slice(Topics.parent)
            .select { Topics.id eq topicId }
            .first()[Topics.parent]

        val courseName = Courses
            .slice(Courses.name)
            .select { Courses.id eq courseId }
            .first()[Courses.name]
            .lowercase()

        when {
            "python" in courseName -> "python"
            "js" in courseName || "javascript" in courseName -> "javascript"
            else -> error("Unknown course language: $courseName")
        }
    }
}