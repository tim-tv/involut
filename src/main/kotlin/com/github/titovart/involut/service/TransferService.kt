package com.github.titovart.involut.service

import com.github.titovart.involut.dto.Transfer
import com.github.titovart.involut.model.Transaction
import com.github.titovart.involut.model.TransactionChange
import com.github.titovart.involut.util.isNotPositive
import java.time.Clock
import java.time.OffsetDateTime

class TransferService(
    private val transactionService: TransactionService
) {

    fun transfer(transfer: Transfer): Transaction {
        validate(transfer)

        val sourceChange = TransactionChange(accountId = transfer.sourceAccountId, amount = -transfer.amount)
        val targetChange = TransactionChange(accountId = transfer.targetAccountId, amount = transfer.amount)

        val transaction = Transaction(
            changeList = listOf(sourceChange, targetChange)
        )

        return transactionService.create(transaction)
    }

    private fun validate(transfer: Transfer) {
        if (transfer.sourceAccountId == transfer.targetAccountId) {
            throw IllegalArgumentException("Account's id mustn't be the same.")
        }

        if (transfer.amount.isNotPositive()) {
            throw IllegalArgumentException("Amount value to transfer must be positive.")
        }
    }
}
