package com.mycompany.model

import com.mycompany.model.ModelBuilders.{transaction, zonedDateTime}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class TransactionTest extends AnyFunSuite:
  test("reduce reduces the amount") {
    transaction(amount = 10).reduceBy(2) should be(transaction(amount = 8))
  }

  test("isAfter positive") {
    transaction(dateTime = zonedDateTime(2022))
      .isAfter(
        transaction(dateTime = zonedDateTime(2021))
      ) should be(true)
  }

  test("isAfter negative") {
    transaction(dateTime = zonedDateTime(2021))
      .isAfter(
        transaction(dateTime = zonedDateTime(2022))
      ) should be(false)
  }
