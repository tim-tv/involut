package com.github.titovart.involut.model

import java.math.BigDecimal
import java.time.OffsetDateTime

data class Account(
    val id: Long = -1,
    val balance: BigDecimal = BigDecimal.ZERO,
    val currency: String = "RUR",
    val createdAt: OffsetDateTime = OffsetDateTime.MIN,
    var closedAt: OffsetDateTime? = null
)
