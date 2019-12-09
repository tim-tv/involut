package com.github.titovart.involut.controller

import com.github.titovart.involut.service.TransferService
import io.javalin.http.Context

class TransferController(private val transferService: TransferService) {

    fun create(ctx: Context) {
        val transfer = ctx.body<com.github.titovart.involut.dto.Transfer>()
        val transaction = transferService.transfer(transfer)
        ctx.json(transaction)
        ctx.status(201)
    }
}
