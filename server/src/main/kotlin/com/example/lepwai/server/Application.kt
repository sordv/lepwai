package com.example.lepwai.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import java.time.Instant
import kotlin.system.exitProcess

object Users : Table("users") {
    val login = varchar("login", 30)
    val password = varchar("password", 30)
    override val primaryKey = PrimaryKey(login)
}

@Serializable
data class Health(val status: String, val ts: String)

@Serializable
data class DbTestResult(val ok: Boolean, val result: Int?, val error: String?)

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
        }
    }.start(wait = true)
}