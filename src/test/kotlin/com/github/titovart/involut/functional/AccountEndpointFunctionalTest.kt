package com.github.titovart.involut.functional

import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.core.api.dataset.ExpectedDataSet
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPatch
import com.github.kittinunf.fuel.httpPost
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class AccountEndpointFunctionalTest : AbstractFunctionalTest() {

    @Test
    @DataSet("fixtures/account/empty.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/create_new/single_created.xml")
    fun `should create new account successfully`() {
        val requestBody = """{"currency": "RUR"}"""

        val (_, response, _) = "/accounts".httpPost()
            .body(requestBody)
            .response()

        val expectedResponseBody =
            """{"id":1,"balance":0.0000,"currency":"RUR","createdAt":"2019-01-01T00:00:00Z","closedAt":null}"""
        val actualResponseBody = response.body().asString("application/json")

        assertEquals(201, response.statusCode)
        assertEquals(expectedResponseBody, actualResponseBody)
    }

    @Test
    @DataSet("fixtures/account/empty.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/empty.xml")
    fun `shouldn't create account with invalid currency`() {
        val requestBody = """{"currency": "CHZ"}"""

        val (_, response, _) = "/accounts".httpPost()
            .body(requestBody)
            .response()

        assertEquals(400, response.statusCode)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    fun `should find account by id`() {
        val (_, response, _) = "accounts/1".httpGet()
            .response()

        val expectedResponseBody =
            """{"id":1,"balance":100.0000,"currency":"RUR","createdAt":"2018-04-18T10:23:52+03:00","closedAt":null}"""

        val actualResponseBody = response.body().asString("application/json")

        assertEquals(200, response.statusCode)
        assertEquals(expectedResponseBody, actualResponseBody)
    }

    @Test
    @DataSet("fixtures/account/close/single_opened.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/close/single_closed.xml")
    fun `should close opened account`() {

        val (_, response, _) = "/accounts/1".httpPatch()
            .response()

        assertEquals(200, response.statusCode)
    }

    @Test
    @DataSet("fixtures/account/close/single_closed.xml", cleanBefore = true)
    @ExpectedDataSet("fixtures/account/close/single_closed.xml")
    fun `should return ok on closing already closed account`() {

        val (_, response, _) = "/accounts/1".httpPatch()
            .response()

        assertEquals(200, response.statusCode)
    }

    @Test
    @DataSet("fixtures/account/accounts.xml", cleanBefore = true)
    fun `shouldn't find account by invalid id`() {
        val (_, response, _) = "accounts/47".httpGet()
            .response()

        assertEquals(404, response.statusCode)
    }

    @Test
    @DataSet("fixtures/account/empty.xml", cleanBefore = true)
    fun `shouldn't find any account on empty database`() {
        val (_, response, _) = "accounts/1".httpGet()
            .response()

        assertEquals(404, response.statusCode)
    }
}
