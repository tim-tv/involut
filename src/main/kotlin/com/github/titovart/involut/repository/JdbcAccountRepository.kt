package com.github.titovart.involut.repository

import com.github.titovart.involut.db.ConnectionHolder
import com.github.titovart.involut.model.Account
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.SQLException
import java.time.OffsetDateTime


class JdbcAccountRepository(
    private val queryRunner: QueryRunner,
    private val connectionHolder: ConnectionHolder
) :
    AccountRepository {

    override fun findByIds(ids: Set<Long>): List<Account> {
        // TODO: replace it by "SELECT * FROM id IN (:ids)" query
        return ids.mapNotNull { findById(it) }
    }

    override fun findById(id: Long): Account? {
        return queryRunner.execute(
            connectionHolder.getConnection(),
            "SELECT id, balance, currency, created_at, closed_at FROM account WHERE id = ?",
            AccountResultSetHandler(),
            id
        ).firstOrNull()
    }

    override fun create(account: Account): Long {
        return queryRunner.insert(
            connectionHolder.getConnection(),
            "INSERT INTO account(currency, created_at) VALUES (?, ?)",
            ScalarHandler<Long>(),
            account.currency, account.createdAt
        )
    }

    override fun updateBalance(amountsByAccountIds: Map<Long, BigDecimal>): Boolean {
        // TODO: replace by batch update
        return amountsByAccountIds
            .map { (accountId, amount) -> updateBalance(accountId, amount) }
            .firstOrNull { !it } ?: true
    }

    override fun updateBalance(accountId: Long, amount: BigDecimal): Boolean {
        val updatedRows = queryRunner.update(
            connectionHolder.getConnection(),
            "UPDATE account SET balance = balance + ? WHERE id = ?",
            amount, accountId
        )

        return updatedRows > 0
    }

    override fun close(accountId: Long, closedAt: OffsetDateTime): Boolean {
        val updatedRows = queryRunner.update(
            connectionHolder.getConnection(),
            "UPDATE account SET closed_at = ? WHERE id = ?",
            closedAt, accountId
        )

        return updatedRows > 0
    }

    private class AccountResultSetHandler : ResultSetHandler<Account> {
        override fun handle(rs: ResultSet?): Account? {
            rs ?: throw SQLException("Result state is null")

            if (rs.next()) {
                return Account(
                    rs.getLong("id"),
                    rs.getBigDecimal("balance"),
                    rs.getString("currency"),
                    rs.getObject("created_at", OffsetDateTime::class.java),
                    rs.getObject("closed_at", OffsetDateTime::class.java)
                )
            }
            return null
        }
    }
}