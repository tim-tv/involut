package com.github.titovart.involut.db

import java.sql.Connection

class TransactionManager(private val connectionHolder: ConnectionHolder) {

    fun <T> runInReadOnlyTransaction(block: () -> T): T = runInTransaction(block = block, readOnly = true)

    fun <T> runInTransaction(block: () -> T): T = runInTransaction(block, null, false)

    fun <T> runInTransaction(block: () -> T, isolationLevel: Int? = null, readOnly: Boolean = false): T {
        val conn = connectionHolder.getConnection()
        val defaultConfig = ConnectionConfig.valueOf(conn)

        conn.autoCommit = false
        conn.transactionIsolation = isolationLevel ?: conn.transactionIsolation
        conn.isReadOnly = readOnly

        try {
            val res = block()
            conn.commit()
            return res
        } catch (exc: Throwable) {
            conn.rollback()
            throw exc
        } finally {
            closeConnection(conn, defaultConfig)
        }
    }

    private fun closeConnection(conn: Connection, config: ConnectionConfig) {
        try {
            config.restoreConnectionConfig(conn)
            conn.close()
        } finally {
            connectionHolder.clear()
        }
    }


    private class ConnectionConfig(
        val autoCommitMode: Boolean,
        val readOnly: Boolean,
        val transactionIsolationLevel: Int
    ) {
        companion object Factory {
            fun valueOf(conn: Connection): ConnectionConfig {
                return ConnectionConfig(conn.autoCommit, conn.isReadOnly, conn.transactionIsolation)
            }
        }

        fun restoreConnectionConfig(conn: Connection): Connection {
            conn.transactionIsolation = transactionIsolationLevel
            conn.isReadOnly = readOnly
            conn.autoCommit = autoCommitMode

            return conn
        }
    }
}
