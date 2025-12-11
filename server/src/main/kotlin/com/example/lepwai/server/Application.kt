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
import io.ktor.server.plugins.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import java.time.Instant
import kotlin.system.exitProcess
import at.favre.lib.crypto.bcrypt.BCrypt

object Users : Table("users") {
    val login = varchar("login", 30)
    val password = varchar("password", 60)
    override val primaryKey = PrimaryKey(login)
}

object Courses : org.jetbrains.exposed.sql.Table("courses") {
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
    val answer = text("answer").nullable()
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Health(val status: String, val ts: String)

@Serializable
data class DbTestResult(val ok: Boolean, val result: Int?, val error: String?)

@Serializable
data class RegisterRequest(val login: String, val password: String, val passwordRepeat: String)

@Serializable
data class RegisterResponse(val ok: Boolean, val error: String? = null)

@Serializable
data class LoginRequest(val login: String, val password: String)

@Serializable
data class LoginResponse(val ok: Boolean, val error: String? = null, val login: String? = null)

@kotlinx.serialization.Serializable
data class CourseDTO(val id: Int, val name: String, val sort: Int)

@kotlinx.serialization.Serializable
data class TopicDTO(val id: Int, val name: String, val sort: Int, val parent: Int)

@kotlinx.serialization.Serializable
data class LevelDTO(val id: Int, val name: String, val sort: Int, val parent: Int, val value: String, val answer: String?)

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
            json()
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
                        // простой SQL — SELECT 1
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

                // Валидация (сервера) — правила, которые вы просили:
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
                                    answer = row[Levels.answer]
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
        }
    }.start(wait = true)
}