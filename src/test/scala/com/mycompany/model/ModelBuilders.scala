package com.mycompany.model

import java.time.{ZoneId, ZonedDateTime}

object ModelBuilders:
  def zonedDateTime(
      year: Int = 2010,
      month: Int = 1,
      dayOfMonth: Int = 1,
      hour: Int = 13,
      minute: Int = 30,
      second: Int = 0
  ) = ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, 0, ZoneId.of("UTC"))

  def transaction(
      id: String = "transaction-id",
      amount: Int = 1,
      dateTime: ZonedDateTime = zonedDateTime()
  ) = Transaction(
    id,
    amount,
    dateTime
  )
