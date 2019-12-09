package com.github.titovart.involut.service

import com.github.titovart.involut.db.TransactionManager
import com.github.titovart.involut.dto.CreateAccountRequest
import com.github.titovart.involut.model.Account
import com.github.titovart.involut.model.Currency
import com.github.titovart.involut.repository.AccountRepository
import java.time.Clock
import java.time.OffsetDateTime

class AccountService(
    private val clock: Clock,
    private val transactionManager: TransactionManager,
    private val accountRepository: AccountRepository
) {

    fun create(request: CreateAccountRequest): Account {
        validateCurrency(request.currency)

        val createdAt = OffsetDateTime.now(clock)
        val account = Account(createdAt = createdAt, currency = request.currency)

        val accountId = transactionManager.runInReadOnlyTransaction { accountRepository.create(account) }
        return findById(accountId) ?: throw IllegalStateException("Can't find created entity")
    }

    fun findById(accountId: Long): Account? {
        return transactionManager.runInReadOnlyTransaction {
            accountRepository.findById(accountId)
        }
    }

    fun close(accountId: Long): Account? {
        return transactionManager.runInTransaction {
            accountRepository.close(accountId, OffsetDateTime.now(clock))
            accountRepository.findById(accountId)
        }
    }

    private fun validateCurrency(currency: String) {
        Currency.valueOf(currency)
    }
}