package com.github.titovart.involut.controller

import com.github.titovart.involut.dto.CreateAccountRequest
import com.github.titovart.involut.dto.DateTimeRangeRequest
import com.github.titovart.involut.dto.OperationAmount
import com.github.titovart.involut.service.AccountService
import com.github.titovart.involut.service.BalanceService
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException


class AccountController(
    private val accountService: AccountService,
    private val balanceService: BalanceService
) {

    fun findAccountById(ctx: Context) {
        val accountId = ctx.pathParam("id").toLong()
        val account = (accountService.findById(accountId)
            ?: throw NotFoundResponse("Account[id=$accountId] hasn't been found"))
        ctx.json(account)
    }

    fun findChangeList(ctx: Context) {
        val accountId = ctx.pathParam("id").toLong()
        val from = parseOffsetDateTime(ctx.queryParam("from"))
        val to = parseOffsetDateTime(ctx.queryParam("to"))

        val res = balanceService.findChangeList(accountId, DateTimeRangeRequest(from, to))
        ctx.json(res)
    }

    fun createAccount(ctx: Context) {
        val account = ctx.body<CreateAccountRequest>()
        val created = accountService.create(account)
        ctx.json(created)
        ctx.status(201)
    }

    fun deposit(ctx: Context) {
        val accountId = ctx.pathParam("id").toLong()
        val operationAmount = ctx.body<OperationAmount>()
        val transaction = balanceService.deposit(accountId, operationAmount.amount)
        ctx.json(transaction)
        ctx.status(201)
    }

    fun withdraw(ctx: Context) {
        val accountId = ctx.pathParam("id").toLong()
        val operationAmount = ctx.body<OperationAmount>()
        val transaction = balanceService.withdraw(accountId, operationAmount.amount)
        ctx.json(transaction)
        ctx.status(201)
    }

    fun close(ctx: Context) {
        val id = ctx.pathParam("id").toLong()
        val account = accountService.close(id) ?: throw NotFoundResponse("Account[id=$id] hasn't been found")
        ctx.json(account)
        ctx.status(200)
    }

    private fun parseOffsetDateTime(paramValue: String?): OffsetDateTime {
        paramValue ?: throw BadRequestResponse("At least one datetime range parameter is null.")

        try {
            return OffsetDateTime.parse(paramValue)
        } catch (exc: DateTimeParseException) {
            throw BadRequestResponse("Range parameter='$paramValue' is not valid")
        }
    }
}
