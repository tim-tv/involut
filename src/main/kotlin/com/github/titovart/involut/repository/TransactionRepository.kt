package com.github.titovart.involut.repository

import com.github.titovart.involut.dto.DateTimeRangeRequest
import com.github.titovart.involut.model.Transaction
import com.github.titovart.involut.model.TransactionChange

interface TransactionRepository {

    fun findChangeListByAccountIdAndDateRange(accountId: Long, range: DateTimeRangeRequest): List<TransactionChange>

    fun createTransaction(transaction: Transaction): Long

    fun createChanges(changeList: List<TransactionChange>): List<Long>

    fun findTransactionById(id: Long): Transaction?
}
