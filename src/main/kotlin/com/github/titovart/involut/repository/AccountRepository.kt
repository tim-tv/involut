package com.github.titovart.involut.repository

import com.github.titovart.involut.model.Account
import java.math.BigDecimal
import java.time.OffsetDateTime

interface AccountRepository {

    fun findById(id: Long): Account?

    fun findByIds(ids: Set<Long>): List<Account>

    fun create(account: Account): Long

    fun close(accountId: Long, closedAt: OffsetDateTime): Boolean

    fun updateBalance(accountId: Long, amount: BigDecimal): Boolean

    fun updateBalance(amountsByAccountIds: Map<Long, BigDecimal>): Boolean
}
