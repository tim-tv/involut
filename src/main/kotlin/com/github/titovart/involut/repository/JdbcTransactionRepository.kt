package com.github.titovart.involut.repository

import com.github.titovart.involut.db.ConnectionHolder
import com.github.titovart.involut.dto.DateTimeRangeRequest
import com.github.titovart.involut.model.Transaction
import com.github.titovart.involut.model.TransactionChange
import com.github.titovart.involut.model.TransactionStatus
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.sql.ResultSet
import java.sql.SQLException
import java.time.OffsetDateTime

class JdbcTransactionRepository(
    private val queryRunner: QueryRunner,
    private val connectionHolder: ConnectionHolder
) : TransactionRepository {

    override
    fun findChangeListByAccountIdAndDateRange(accountId: Long, range: DateTimeRangeRequest): List<TransactionChange> {
        return queryRunner.query(
            connectionHolder.getConnection(),
            "SELECT change.* " +
                    "FROM change " +
                    "JOIN transaction ON change.transaction_id = transaction.id " +
                    "WHERE transaction.status = ? " +
                    "AND transaction.updated_at >= ? " +
                    "AND transaction.updated_at < ? " +
                    "AND change.account_id = ? " +
                    "ORDER BY transaction.updated_at " +
                    "LIMIT 100",
            TransactionChangeHandler(),
            TransactionStatus.COMPLETED.code, range.from, range.to, accountId
        )
    }

    override fun findTransactionById(id: Long): Transaction? {
        return queryRunner.query(
            connectionHolder.getConnection(),
            "SELECT transaction.*, change.* " +
                    "FROM transaction " +
                    "LEFT JOIN change ON transaction.id = change.transaction_id " +
                    "WHERE transaction.id = ? " +
                    "ORDER BY transaction.id",
            TransactionSetHandler(),
            id
        ).firstOrNull()
    }

    override fun createTransaction(transaction: Transaction): Long {
        return queryRunner.insert(
            connectionHolder.getConnection(),
            "INSERT INTO transaction(created_at, updated_at, status, error_reason) VALUES (?, ?, ?, ?)",
            ScalarHandler<Long>(),
            transaction.createdAt, transaction.createdAt, transaction.status.code, transaction.errorReason
        )
    }

    override fun createChanges(changeList: List<TransactionChange>): List<Long> {
        // TODO: use batch insert
        return changeList.map { change -> createChange(change) }.toList()
    }

    private fun createChange(change: TransactionChange): Long {
        return queryRunner.insert(
            connectionHolder.getConnection(),
            "INSERT INTO change(account_id, transaction_id, amount) VALUES (?, ?, ?)",
            ScalarHandler<Long>(),
            change.accountId, change.transactionId, change.amount
        )
    }

    private class TransactionChangeHandler : ResultSetHandler<List<TransactionChange>> {

        override fun handle(rs: ResultSet?): List<TransactionChange> {
            rs ?: throw SQLException("Result state is null")

            val changes = arrayListOf<TransactionChange>()

            while (rs.next()) {
                val change = TransactionChange(
                    rs.getLong("change.id"),
                    rs.getLong("change.account_id"),
                    rs.getLong("change.transaction_id"),
                    rs.getBigDecimal("change.amount")
                )

                changes.add(change)
            }

            return changes
        }
    }

    private class TransactionSetHandler : ResultSetHandler<List<Transaction>> {
        override fun handle(rs: ResultSet?): List<Transaction> {
            rs ?: throw SQLException("Result state is null")

            val transactions = ArrayList<Transaction>()

            while (rs.next()) {
                transactions.add(readTransaction(rs))
            }

            return transactions.groupBy { it.id }.map { (txId, transactions) ->
                val tx = transactions.first()
                val changeList = transactions.flatMap { it.changeList }
                Transaction(txId, tx.status, tx.createdAt, tx.updatedAt, changeList, tx.errorReason)
            }
        }

        private fun readTransaction(rs: ResultSet): Transaction {
            return Transaction(
                rs.getLong("transaction.id"),
                TransactionStatus.valueOf(rs.getInt("transaction.status")),
                rs.getObject("transaction.created_at", OffsetDateTime::class.java),
                rs.getObject("transaction.updated_at", OffsetDateTime::class.java),
                arrayListOf(readChange(rs)),
                rs.getString("transaction.error_reason")
            )
        }

        private fun readChange(rs: ResultSet): TransactionChange {
            return TransactionChange(
                rs.getLong("change.id"),
                rs.getLong("change.account_id"),
                rs.getLong("change.transaction_id"),
                rs.getBigDecimal("change.amount")
            )
        }
    }
}
