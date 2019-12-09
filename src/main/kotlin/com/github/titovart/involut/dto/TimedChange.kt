package com.github.titovart.involut.dto

import com.github.titovart.involut.model.TransactionChange
import java.time.OffsetDateTime

data class TimedChange(val completedAt: OffsetDateTime, val change: TransactionChange)
