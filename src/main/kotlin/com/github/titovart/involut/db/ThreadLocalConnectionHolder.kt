package com.github.titovart.involut.db

import java.sql.Connection
import javax.sql.DataSource

class ThreadLocalConnectionHolder(
    private val dataSource: DataSource,
    private val threadLocal: ThreadLocal<Connection> = ThreadLocal()
) : ConnectionHolder {

    override fun getConnection(): Connection {
        var conn = threadLocal.get()
        if (conn == null) {
            conn = dataSource.connection
            threadLocal.set(conn)
        }
        return conn
    }

    override fun clear() {
        threadLocal.remove()
    }
}
