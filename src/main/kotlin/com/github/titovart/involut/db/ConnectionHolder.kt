package com.github.titovart.involut.db

import java.sql.Connection

interface ConnectionHolder {

    fun getConnection(): Connection

    fun clear()
}
