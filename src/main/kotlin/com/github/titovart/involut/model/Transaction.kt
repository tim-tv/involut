package com.github.titovart.involut.model

import java.time.OffsetDateTime


data class Transaction(
    val id: Long = -1,
    var status: TransactionStatus = TransactionStatus.CREATED,
    val createdAt: OffsetDateTime = OffsetDateTime.MIN,
    val updatedAt: OffsetDateTime = OffsetDateTime.MIN,
    val changeList: List<TransactionChange> = listOf(),
    var errorReason: String? = null
)
