package com.github.titovart.involut

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.titovart.involut.controller.AccountController
import com.github.titovart.involut.controller.TransactionController
import com.github.titovart.involut.controller.TransferController
import com.github.titovart.involut.db.ThreadLocalConnectionHolder
import com.github.titovart.involut.db.TransactionManager
import com.github.titovart.involut.dto.ErrorResponse
import com.github.titovart.involut.repository.JdbcAccountRepository
import com.github.titovart.involut.repository.JdbcTransactionRepository
import com.github.titovart.involut.service.AccountService
import com.github.titovart.involut.service.BalanceService
import com.github.titovart.involut.service.TransactionService
import com.github.titovart.involut.service.TransferService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.BadRequestResponse
import io.javalin.http.HttpResponseException
import io.javalin.plugin.json.JavalinJackson
import org.apache.commons.dbutils.QueryRunner
import java.io.File
import java.time.Clock
import kotlin.text.Charsets.UTF_8


class InvolutApplication {

    private lateinit var app: Javalin
    lateinit var dataSource: HikariDataSource
    var clock: Clock = Clock.systemDefaultZone()

    fun init(): InvolutApplication {
        JavalinJackson.getObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)

        app = Javalin.create()
        initExceptionHandlers(app)
        initRoutes(app)
        return this
    }

    fun instance(): Javalin = app

    fun start(port: Int? = null): InvolutApplication {
        if (port != null) {
            app.start(port)
        } else {
            app.start()
        }
        return this
    }

    fun stop() {
        app.stop()
    }

    private fun initExceptionHandlers(app: Javalin): Javalin {
        return app.apply {
            exception(IllegalArgumentException::class.java) { exc, ctx ->
                ctx.status(400)
                val resp = ErrorResponse(exc.message ?: "Bad request")
                ctx.json(resp)
            }
            exception(HttpResponseException::class.java) { exc, ctx ->
                ctx.status(exc.status)
                ctx.json(ErrorResponse(exc.message ?: "Error"))
            }
            exception(RuntimeException::class.java) { exc, _ -> throw Exception(exc) }
            exception(Exception::class.java) { exc, ctx ->
                exc.printStackTrace()
                ctx.status(500)
                ctx.json(ErrorResponse("Internal server error"))
            }
        }
    }

    private fun initRoutes(app: Javalin): Javalin {
        // TODO: replace this boilerplate by DI framework
        dataSource  = createDataSource()
        val queryRunner = QueryRunner(dataSource)
        applyDbSchema(queryRunner)

        val connectionHolder = ThreadLocalConnectionHolder(dataSource)
        val transactionManager = TransactionManager(connectionHolder)

        val transactionRepository =
            JdbcTransactionRepository(queryRunner, connectionHolder)

        val accountRepository =
            JdbcAccountRepository(queryRunner, connectionHolder)
        val transactionService = TransactionService(clock, transactionManager, accountRepository, transactionRepository)
        val transactionController = TransactionController(transactionService)

        val accountService = AccountService(clock, transactionManager, accountRepository)
        val balanceService = BalanceService(accountService, transactionService)
        val accountController = AccountController(accountService, balanceService)

        val transferService = TransferService(transactionService)
        val transferController = TransferController(transferService)

        return app.routes {
            path("/api/v1/") {
                get("/accounts/:id", accountController::findAccountById)
                post("/accounts", accountController::createAccount)
                post("/accounts/:id/operations/deposits", accountController::deposit)
                post("/accounts/:id/operations/withdrawals", accountController::withdraw)
                get("/accounts/:id/operations", accountController::findChangeList)
                patch("/accounts/:id", accountController::close)

                post("/transfers", transferController::create)
                get("/transactions/:id", transactionController::findByTransactionId)
            }
        }
    }

    private fun applyDbSchema(queryRunner: QueryRunner) {
        // TODO: replace it by migration framework like liquibase
        val schemaPath = ClassLoader.getSystemClassLoader().getResource("schema.sql")?.path
            ?: throw Error("Database schema hasn't been found")
        val migrationContent = File(schemaPath).readText(UTF_8)

        queryRunner.update(migrationContent)
    }

    private fun createDataSource(): HikariDataSource {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:h2:mem:involut;DB_CLOSE_DELAY=-1"
        return HikariDataSource(config)
    }
}

fun main() {
    InvolutApplication().init().start(7000)
}
