package com.github.titovart.involut.db

import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.sql.Connection
import java.sql.SQLException

internal class TransactionManagerTest {

    @Test
    fun `should run in transaction with commit`() {
        val connectionMock = createDefaultConnectionMock()
        val connectionHolder = spy(ProxyConnectionHolder(connectionMock))
        val transactionManager = TransactionManager(connectionHolder)

        val res = transactionManager.runInTransaction { "42" }

        assertEquals("42", res)

        verify(connectionMock, times(0)).rollback()
        verify(connectionMock, times(1)).commit()
        verify(connectionMock, times(1)).close()
        verify(connectionMock, times(2)).isReadOnly = false
        verify(connectionMock, times(2)).transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        verify(connectionMock, times(1)).autoCommit = false
        verify(connectionMock, times(1)).autoCommit = true
        verify(connectionHolder, times(1)).clear()
    }

    @Test
    fun `should rollback in case of exception in transactional block`() {
        val connectionMock = createDefaultConnectionMock()
        val connectionHolder = spy(ProxyConnectionHolder(connectionMock))
        val transactionManager = TransactionManager(connectionHolder)

        assertThrows<SQLException> {
            transactionManager.runInTransaction { throw SQLException("Error") }
        }

        verify(connectionMock, times(1)).rollback()
        verify(connectionMock, times(0)).commit()
        verify(connectionMock, times(1)).close()
        verify(connectionMock, times(2)).isReadOnly = false
        verify(connectionMock, times(2)).transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        verify(connectionMock, times(1)).autoCommit = false
        verify(connectionMock, times(1)).autoCommit = true
        verify(connectionHolder, times(1)).clear()
    }

    @Test
    fun `should close connection in case of exception on rollback`() {
        val connectionMock = createDefaultConnectionMock()
        doThrow(SQLException("Error")).`when`(connectionMock).rollback()

        val connectionHolder = spy(ProxyConnectionHolder(connectionMock))
        val transactionManager = TransactionManager(connectionHolder)

        assertThrows<SQLException> {
            transactionManager.runInTransaction { throw RuntimeException() }
        }

        verify(connectionMock, times(1)).rollback()
        verify(connectionMock, times(0)).commit()
        verify(connectionMock, times(1)).close()
        verify(connectionMock, times(2)).isReadOnly = false
        verify(connectionMock, times(2)).transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        verify(connectionMock, times(1)).autoCommit = false
        verify(connectionMock, times(1)).autoCommit = true
        verify(connectionHolder, times(1)).clear()
    }

    @Test
    fun `should rollback and close connection in case of exception on commit`() {
        val connectionMock = createDefaultConnectionMock()
        doThrow(SQLException("Error")).`when`(connectionMock).commit()

        val connectionHolder = spy(ProxyConnectionHolder(connectionMock))
        val transactionManager = TransactionManager(connectionHolder)

        assertThrows<SQLException> {
            transactionManager.runInTransaction { "42" }
        }

        verify(connectionMock, times(1)).rollback()
        verify(connectionMock, times(1)).commit()
        verify(connectionMock, times(1)).close()
        verify(connectionMock, times(2)).isReadOnly = false
        verify(connectionMock, times(2)).transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        verify(connectionMock, times(1)).autoCommit = false
        verify(connectionMock, times(1)).autoCommit = true
        verify(connectionHolder, times(1)).clear()
    }

    @Test
    fun `should clear holder in case of exception on close`() {
        val connectionMock = createDefaultConnectionMock()
        doThrow(SQLException("Error")).`when`(connectionMock).close()

        val connectionHolder = spy(ProxyConnectionHolder(connectionMock))
        val transactionManager = TransactionManager(connectionHolder)

        assertThrows<SQLException> {
            transactionManager.runInTransaction { "42" }
        }

        verify(connectionMock, times(0)).rollback()
        verify(connectionMock, times(1)).commit()
        verify(connectionMock, times(1)).close()
        verify(connectionMock, times(2)).isReadOnly = false
        verify(connectionMock, times(2)).transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        verify(connectionMock, times(1)).autoCommit = false
        verify(connectionMock, times(1)).autoCommit = true
        verify(connectionHolder, times(1)).clear()
    }

    private fun createDefaultConnectionMock(): Connection {
        val connectionMock = mock(Connection::class.java)
        doNothing().`when`(connectionMock).close()
        doNothing().`when`(connectionMock).commit()
        doNothing().`when`(connectionMock).rollback()
        doNothing().`when`(connectionMock).isReadOnly = ArgumentMatchers.anyBoolean()
        doNothing().`when`(connectionMock).transactionIsolation = ArgumentMatchers.anyInt()
        doNothing().`when`(connectionMock).autoCommit = ArgumentMatchers.anyBoolean()
        `when`(connectionMock.isReadOnly).thenReturn(false)
        `when`(connectionMock.transactionIsolation).thenReturn(Connection.TRANSACTION_READ_COMMITTED)
        `when`(connectionMock.autoCommit).thenReturn(true)

        return connectionMock
    }

    private open class ProxyConnectionHolder(private val conn: Connection) : ConnectionHolder {
        override fun getConnection(): Connection {
            return conn
        }

        override fun clear() {
            // do nothing
        }
    }
}
