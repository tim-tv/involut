package com.github.titovart.involut.model

import java.lang.IllegalArgumentException

enum class TransactionStatus(val code: Int) {
    CREATED(0),
    COMPLETED(1),
    FAILED(2);

    companion object Factory {
        fun valueOf(code: Int): TransactionStatus {
            return values().first { status -> status.code == code }
        }
    }
}
