package com.mycompany.model

import java.time.ZonedDateTime

case class Transaction(
    id: String,
    amount: Int, // for simplicity lets keep it an Int
    dateTime: ZonedDateTime
):
  def reduceBy(howMuch: Int) = copy(amount = amount - howMuch)
  def isAfter(other: Transaction) = dateTime.isAfter(other.dateTime)
