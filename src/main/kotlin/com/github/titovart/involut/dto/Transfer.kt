package com.github.titovart.involut.dto

import java.math.BigDecimal

data class Transfer(val sourceAccountId: Long, val targetAccountId: Long, val amount: BigDecimal)
