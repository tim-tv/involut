package com.github.titovart.involut.functional

import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.core.api.dataset.ExpectedDataSet
import com.github.kittinunf.fuel.httpPost
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class AccountOperationsEndpointFunctionalTest : AbstractFunctionalTest() {

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/deposit/accounts_with_second_deposited.xml")
    fun `should deposit on opened account`() {
        val requestBody = """{"amount": "100"}"""

        val (_, response, _) = "/accounts/2/operations/deposits".httpPost()
            .body(requestBody)
            .response()

        val expectedResponseBody =
            """
            {
                "id":1,
                "status":"COMPLETED",
                "createdAt":"2019-01-01T00:00:00Z",
                "updatedAt":"2019-01-01T00:00:00Z",
                "changeList":[{"id":1,"accountId":2,"transactionId":1,"amount":100.0000}],
                "errorReason":null
            }
            """.collapseIdent()

        val actualResponseBody = response.body().asString("application/json")

        assertEquals(201, response.statusCode)
        assertEquals(expectedResponseBody, actualResponseBody)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/accounts_without_close_at_column.xml")
    fun `shouldn't deposit negative value`() {
        val requestBody = """{"amount": "-1"}"""

        val (_, response, _) = "/accounts/2/operations/deposits".httpPost()
            .body(requestBody)
            .response()

        assertEquals(400, response.statusCode)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/accounts_without_close_at_column.xml")
    fun `shouldn't deposit zero value`() {
        val requestBody = """{"amount": "0"}"""

        val (_, response, _) = "/accounts/2/operations/deposits".httpPost()
            .body(requestBody)
            .response()

        assertEquals(400, response.statusCode)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/deposit/accounts_with_declined_deposit_transaction.xml")
    fun `shouldn't deposit on closed account`() {
        val requestBody = """{"amount": "100"}"""

        val (_, response, _) = "/accounts/3/operations/deposits".httpPost()
            .body(requestBody)
            .response()

        val expectedResponseBody =
            """
            {
                "id":1,
                "status":"FAILED",
                "createdAt":"2019-01-01T00:00:00Z",
                "updatedAt":"2019-01-01T00:00:00Z",
                "changeList":[{"id":1,"accountId":3,"transactionId":1,"amount":100.0000}],
                "errorReason":"Account[id=3] has been closed"
            }
            """.collapseIdent()

        val actualResponseBody = response.body().asString("application/json")

        assertEquals(201, response.statusCode)
        assertEquals(expectedResponseBody, actualResponseBody)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/withdraw/accounts_with_accepted_withdrawal_transaction.xml")
    fun `should withdraw on opened account with valid amount`() {
        val requestBody = """{"amount": "100"}"""

        val (_, response, _) = "/accounts/2/operations/withdrawals".httpPost()
            .body(requestBody)
            .response()

        val expectedResponseBody =
            """
            {
                "id":1,
                "status":"COMPLETED",
                "createdAt":"2019-01-01T00:00:00Z",
                "updatedAt":"2019-01-01T00:00:00Z",
                "changeList":[{"id":1,"accountId":2,"transactionId":1,"amount":-100.0000}],
                "errorReason":null
            }
            """.collapseIdent()

        val actualResponseBody = response.body().asString("application/json")

        assertEquals(201, response.statusCode)
        assertEquals(expectedResponseBody, actualResponseBody)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/withdraw/accounts_with_declined_withdrawal_transaction.xml")
    fun `shouldn't withdraw on opened account with excessive amount`() {
        val requestBody = """{"amount": "1000"}"""

        val (_, response, _) = "/accounts/2/operations/withdrawals".httpPost()
            .body(requestBody)
            .response()

        val expectedResponseBody =
            """
            {
                "id":1,
                "status":"FAILED",
                "createdAt":"2019-01-01T00:00:00Z",
                "updatedAt":"2019-01-01T00:00:00Z",
                "changeList":[{"id":1,"accountId":2,"transactionId":1,"amount":-1000.0000}],
                "errorReason":"Account[id=2] has insufficient funds."
            }
            """.collapseIdent()

        val actualResponseBody = response.body().asString("application/json")

        assertEquals(201, response.statusCode)
        assertEquals(expectedResponseBody, actualResponseBody)
    }
}
