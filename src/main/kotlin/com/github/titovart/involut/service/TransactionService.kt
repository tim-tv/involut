package com.github.titovart.involut.service

import com.github.titovart.involut.model.Transaction
import com.github.titovart.involut.model.TransactionChange
import com.github.titovart.involut.model.TransactionStatus
import com.github.titovart.involut.repository.AccountRepository
import com.github.titovart.involut.repository.TransactionRepository
import com.github.titovart.involut.db.TransactionManager
import com.github.titovart.involut.dto.DateTimeRangeRequest
import com.github.titovart.involut.model.Account
import com.github.titovart.involut.util.isNotPositive
import java.sql.Connection
import java.sql.SQLException
import java.time.Clock
import java.time.OffsetDateTime

class TransactionService(
    private val clock: Clock,
    private val transactionManager: TransactionManager,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {

    fun findChangeListByAccountIdAndDateRange(accountId: Long, range: DateTimeRangeRequest): List<TransactionChange> {
        return transactionManager.runInReadOnlyTransaction {
            transactionRepository.findChangeListByAccountIdAndDateRange(accountId, range)
        }
    }

    fun create(transaction: Transaction): Transaction {
        validateRequest(transaction)
        val enrichedTx = enrichByCreatedAtTimestamp(transaction)

        val transactionId: Long = transactionManager.runInTransaction(
            block = { createTransactionWithAccountsCharging(enrichedTx) },
            isolationLevel = Connection.TRANSACTION_REPEATABLE_READ,
            readOnly = false
        )
        return findById(transactionId) ?: throw IllegalStateException("Can't find created entity")
    }

    fun findById(txId: Long): Transaction? {
        return transactionManager.runInReadOnlyTransaction { transactionRepository.findTransactionById(txId) }
    }

    private fun enrichByCreatedAtTimestamp(tx: Transaction): Transaction {
        val createdAt = OffsetDateTime.now(clock)
        return Transaction(tx.id, tx.status, createdAt, createdAt, tx.changeList)
    }

    private fun validateRequest(transaction: Transaction): Transaction {
        transaction.changeList.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Transaction must have at least one change.")

        val accountIds = transaction.changeList.map { it.accountId }.toSet()
        val accounts = transactionManager.runInTransaction { accountRepository.findByIds(accountIds) }

        val diff = accountIds.subtract(accounts.map { it.id })
        if (diff.isNotEmpty()) {
            throw IllegalArgumentException("Couldn't execute transaction for undefined accounts: $diff")
        }

        val currencies = accounts.map { it.currency }.toSet()
        if (currencies.size > 1) {
            throw IllegalArgumentException("Couldn't execute transaction for accounts with different currencies: $currencies")
        }

        if (accountIds.size < transaction.changeList.size) {
            throw IllegalArgumentException("Transaction mustn't have changes with the same accountId")
        }

        return transaction
    }

    private fun validateAccountState(account: Account, change: TransactionChange?): String? {
        change ?: throw IllegalStateException("Couldn't find change for Account[id=${account.id}.")

        if (account.closedAt != null) {
            return "Account[id=${account.id}] has been closed"
        }

        val amountAfterTransaction = account.balance + change.amount
        if (amountAfterTransaction.isNotPositive()) {
            return "Account[id=${account.id}] has insufficient funds."
        }

        return null
    }

    private fun validateCorrectnessOfChanges(changeList: List<TransactionChange>): String? {
        val changesByAccountId = changeList.map { it.accountId to it }.toMap()
        val accounts = accountRepository.findByIds(changesByAccountId.keys)

        return accounts
            .map { account -> validateAccountState(account, changesByAccountId[account.id]) }
            .firstOrNull { it != null }
    }

    private fun createTransactionWithAccountsCharging(transaction: Transaction): Long {
        val validationError = validateCorrectnessOfChanges(transaction.changeList)
        if (validationError != null) {
            return createTransaction(transaction, TransactionStatus.FAILED, validationError)
        }

        return updateAmountOrCreateFailedTransaction(transaction) ?: createTransaction(transaction)
    }

    private fun updateAmountOrCreateFailedTransaction(transaction: Transaction): Long? {
        val updated = try {
            val changesToUpdate = transaction.changeList.map { it.accountId to it.amount }.toMap()
            accountRepository.updateBalance(changesToUpdate)
        } catch (exc: SQLException) {
            return createTransaction(transaction, TransactionStatus.FAILED, "Server error")
        }

        if (!updated) {
            throw SQLException("At least one account haven't been updated.")
        }

        return null
    }

    private fun createTransaction(
        transaction: Transaction,
        status: TransactionStatus = TransactionStatus.COMPLETED,
        errorReason: String? = null
    ): Long {

        transaction.errorReason = errorReason
        transaction.status = status
        val transactionId = transactionRepository.createTransaction(transaction)

        val changeList = transaction.changeList.map { change ->
            TransactionChange(
                accountId = change.accountId,
                transactionId = transactionId,
                amount = change.amount
            )
        }.toList()

        transactionRepository.createChanges(changeList)
        return transactionId
    }

}