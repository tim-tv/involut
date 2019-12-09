package com.github.titovart.involut.model

import java.math.BigDecimal

data class TransactionChange(
    val id: Long = -1,
    val accountId: Long,
    val transactionId: Long = -1,
    val amount: BigDecimal
)
