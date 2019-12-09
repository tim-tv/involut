package com.github.titovart.involut.service

import com.github.titovart.involut.dto.DateTimeRangeRequest
import com.github.titovart.involut.model.Transaction
import com.github.titovart.involut.model.TransactionChange
import com.github.titovart.involut.util.isNotPositive
import io.javalin.http.NotFoundResponse
import java.math.BigDecimal

class BalanceService(private val accountService: AccountService,
                     private val transactionService: TransactionService) {

    fun findChangeList(accountId: Long, dateTimeRange: DateTimeRangeRequest): List<TransactionChange> {
        accountService.findById(accountId) ?: throw NotFoundResponse("Account[id=$accountId] hasn't been found")

        return transactionService.findChangeListByAccountIdAndDateRange(accountId, dateTimeRange)
    }

    fun deposit(accountId: Long, amount: BigDecimal): Transaction {
        validateAmount(amount)
        return createTransaction(accountId, amount = amount)
    }

    fun withdraw(accountId: Long, amount: BigDecimal): Transaction {
        validateAmount(amount)
        return createTransaction(accountId, amount = -amount)
    }

    private fun validateAmount(amount: BigDecimal) {
        if (amount.isNotPositive()) {
            throw IllegalArgumentException("Operation amount must be positive.")
        }
    }

    private fun createTransaction(accountId: Long, amount: BigDecimal): Transaction {
        val change = TransactionChange(accountId = accountId, amount = amount)

        val transaction = Transaction(
            changeList = listOf(change)
        )

        return transactionService.create(transaction)
    }
}