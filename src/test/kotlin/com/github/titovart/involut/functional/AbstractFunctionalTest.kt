package com.github.titovart.involut.functional

import com.github.database.rider.core.api.configuration.DBUnit
import com.github.database.rider.core.api.configuration.Orthography
import com.github.database.rider.core.api.connection.ConnectionHolder
import com.github.database.rider.core.connection.ConnectionHolderImpl
import com.github.database.rider.core.replacers.NullReplacer
import com.github.database.rider.junit5.DBUnitExtension
import com.github.kittinunf.fuel.core.FuelManager
import com.github.titovart.involut.InvolutApplication
import org.apache.commons.dbutils.QueryRunner
import org.dbunit.dataset.datatype.DataType
import org.dbunit.dataset.datatype.DataTypeException
import org.dbunit.ext.h2.H2DataTypeFactory
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset


@ExtendWith(DBUnitExtension::class)
@RunWith(JUnitPlatform::class)
@DBUnit(
    caseSensitiveTableNames = false,
    caseInsensitiveStrategy = Orthography.LOWERCASE,
    dataTypeFactoryClass = AbstractFunctionalTest.CustomH2DataTypeFactory::class,
    replacers = [NullReplacer::class]
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractFunctionalTest {

    private lateinit var connectionHolder: ConnectionHolder

    private lateinit var app: InvolutApplication

    @BeforeAll
    fun setUp() {
        app = InvolutApplication()
        app.clock = Clock.fixed(LocalDateTime.of(2019, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))
        app.init().start()

        FuelManager.instance.basePath = "http://localhost:${app.instance().port()}/api/v1/"
        connectionHolder = ConnectionHolderImpl(app.dataSource.connection)
    }

    @AfterAll
    fun tearDown() {
        app.stop()
    }

    @BeforeEach
    fun setUpEach() {
        resetSequences()
    }

    private fun resetSequences() {
        val sqlQuery = """
            ALTER SEQUENCE account_id_seq RESTART WITH 1;
            ALTER SEQUENCE transaction_id_seq RESTART WITH 1;
            ALTER SEQUENCE change_id_seq RESTART WITH 1;
        """.trimIndent()

        QueryRunner(app.dataSource).update(sqlQuery)
    }

    class CustomH2DataTypeFactory : H2DataTypeFactory() {

        @Throws(DataTypeException::class)
        override fun createDataType(sqlType: Int, sqlTypeName: String): DataType {
            return if (sqlTypeName.equals("TIMESTAMP WITH TIME ZONE", ignoreCase = true)) {
                DataType.TIMESTAMP
            } else super.createDataType(sqlType, sqlTypeName)
        }
    }
}