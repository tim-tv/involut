package com.github.titovart.involut.controller

import com.github.titovart.involut.service.TransactionService
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse

class TransactionController(private val transactionService: TransactionService) {

    fun findByTransactionId(ctx: Context) {
        val requestedId = ctx.pathParam("id").toLong()
        val tx = (transactionService.findById(requestedId)
            ?: throw NotFoundResponse("Transaction[id=$requestedId] hasn't been found"))
        ctx.json(tx)
    }
}
