package com.github.titovart.involut.functional

import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.core.api.dataset.ExpectedDataSet
import com.github.kittinunf.fuel.httpPost
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class TransferEndpointFunctionalTest : AbstractFunctionalTest() {

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/transfers/completed_transfer.xml")
    fun `should complete transfer`() {
        val requestBody = """{"sourceAccountId":1, "targetAccountId":2, "amount": "50"}"""

        val (_, response, _) = "/transfers".httpPost()
            .body(requestBody)
            .response()

        val expectedResponseBody =
            """
            {
                "id":1,
                "status":"COMPLETED",
                "createdAt":"2019-01-01T00:00:00Z",
                "updatedAt":"2019-01-01T00:00:00Z",
                "changeList":[
                    {"id":1,"accountId":1,"transactionId":1,"amount":-50.0000},
                    {"id":2,"accountId":2,"transactionId":1,"amount":50.0000}
                ],
                "errorReason":null
            }
            """.collapseIdent()

        val actualResponseBody = response.body().asString("application/json")

        assertEquals(201, response.statusCode)
        assertEquals(expectedResponseBody, actualResponseBody)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/transfers/failed_transfer_from_closed_account.xml")
    fun `shouldn't transfer from closed account`() {
        val requestBody = """{"sourceAccountId":3, "targetAccountId":2, "amount": "5"}"""

        val (_, response, _) = "/transfers".httpPost()
            .body(requestBody)
            .response()

        val expectedResponseBody =
            """
            {
                "id":1,
                "status":"FAILED",
                "createdAt":"2019-01-01T00:00:00Z",
                "updatedAt":"2019-01-01T00:00:00Z",
                "changeList":[
                    {"id":1,"accountId":3,"transactionId":1,"amount":-5.0000},
                    {"id":2,"accountId":2,"transactionId":1,"amount":5.0000}
                ],
                "errorReason":"Account[id=3] has been closed"
            }
            """.collapseIdent()

        val actualResponseBody = response.body().asString("application/json")

        assertEquals(201, response.statusCode)
        assertEquals(expectedResponseBody, actualResponseBody)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/transfers/failed_transfer_to_closed_account.xml")
    fun `shouldn't transfer to closed account`() {
        val requestBody = """{"sourceAccountId":1, "targetAccountId":3, "amount": "5"}"""

        val (_, response, _) = "/transfers".httpPost()
            .body(requestBody)
            .response()

        val expectedResponseBody =
            """
            {
                "id":1,
                "status":"FAILED",
                "createdAt":"2019-01-01T00:00:00Z",
                "updatedAt":"2019-01-01T00:00:00Z",
                "changeList":[
                    {"id":1,"accountId":1,"transactionId":1,"amount":-5.0000},
                    {"id":2,"accountId":3,"transactionId":1,"amount":5.0000}
                ],
                "errorReason":"Account[id=3] has been closed"
            }
            """.collapseIdent()

        val actualResponseBody = response.body().asString("application/json")

        assertEquals(201, response.statusCode)
        assertEquals(expectedResponseBody, actualResponseBody)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/transfers/failed_transfer_with_excessive_amount.xml")
    fun `shouldn't transfer excessive amount`() {
        val requestBody = """{"sourceAccountId":1, "targetAccountId":2, "amount": "500"}"""

        val (_, response, _) = "/transfers".httpPost()
            .body(requestBody)
            .response()

        val expectedResponseBody =
            """
            {
                "id":1,
                "status":"FAILED",
                "createdAt":"2019-01-01T00:00:00Z",
                "updatedAt":"2019-01-01T00:00:00Z",
                "changeList":[
                    {"id":1,"accountId":1,"transactionId":1,"amount":-500.0000},
                    {"id":2,"accountId":2,"transactionId":1,"amount":500.0000}
                ],
                "errorReason":"Account[id=1] has insufficient funds."
            }
            """.collapseIdent()

        val actualResponseBody = response.body().asString("application/json")

        assertEquals(201, response.statusCode)
        assertEquals(expectedResponseBody, actualResponseBody)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/accounts_without_close_at_column.xml")
    fun `shouldn't transfer to unknown account`() {
        val requestBody = """{"sourceAccountId":1, "targetAccountId":47, "amount": "5"}"""

        val (_, response, _) = "/transfers".httpPost()
            .body(requestBody)
            .response()

        assertEquals(400, response.statusCode)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/accounts_without_close_at_column.xml")
    fun `shouldn't transfer to account with different currency`() {
        val requestBody = """{"sourceAccountId":1, "targetAccountId":4, "amount": "5"}"""

        val (_, response, _) = "/transfers".httpPost()
            .body(requestBody)
            .response()

        assertEquals(400, response.statusCode)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/accounts_without_close_at_column.xml")
    fun `shouldn't transfer between the same accounts`() {
        val requestBody = """{"sourceAccountId":1, "targetAccountId":1, "amount": "5"}"""

        val (_, response, _) = "/transfers".httpPost()
            .body(requestBody)
            .response()

        assertEquals(400, response.statusCode)
    }

}

