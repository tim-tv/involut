package com.github.titovart.involut.util

import java.math.BigDecimal

fun BigDecimal.isPositive() = this.signum() > 0

fun BigDecimal.isNotPositive() = !this.isPositive()
