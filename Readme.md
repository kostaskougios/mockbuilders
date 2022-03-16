# Testing with mock builders

When writing scala test cases, one of the hardest parts is to get
the right data for the tests. There are a number of ways to create
data (i.e. instance of classes filled in with the right details for the test).
One way is test fixtures but those have a number of problems, i.e.
they make a test hard to read because you need to refer to the fixture
to actually see what the data are. Also, if the fixtures are shared, then
they are coupled to many tests which makes them even harder to read and
maintain. A change in the fixture may break a number of tests.

For some time now I've been using a mock builder pattern which makes
creating test data a lot easier, readable and maintainable.

## An example

For the sake of this example, lets say we have a domain model class
for transactions. Let's start with this Scala 3 class:

```scala
case class Transaction(
    id: String,
    amount: Int // for simplicity lets keep it an Int
):
  def reduceBy(howMuch: Int) = copy(amount = amount - howMuch)
```

We have a transaction that has an id and the amount. It also has a business logic
method `reduceBy` which will reduce the value of the transaction and it is something
we want to test.

A naive testing approach would be to create an instance of the class and then
test the `reduceBy` method:

```scala
test("reduce reduces the amount") {
    Transaction("",10).reduceBy(2) should be(Transaction("",8))
  }
```

But this has a number of problems. I.e. why do we need to provide an id for
the test, and why the id is blank? Is it even relevant? What if we add an
extra field in `Transaction`, our test won't compile and we will have to
add more irrelevant data for testing the `reduceBy` method.

Of course, we could extract the instance to a fixture but that complicates the code,
especially if we want to reuse the fixture. (Note that fixtures were meant to share
components like database connections etc. but many projects use fixtures to share
testing data)

```scala
trait TransactionFixture:
  val MyTransaction = Transaction("",10)

    ... later on ...

test("reduce reduces the amount") new TransactionFixture {
    MyTransaction.reduceBy(2) should be(MyTransaction.copy(amount=8))
  }
```

In the test itself, we can't anymore see what the amount is before it was `reducedBy`. Also the `TransactionFixture`
standalone seems to contain meaningless data. And it suffers from the maintenance issues we mentioned i.e. when adding
a field to the `Transaction` class.

Even more if we reuse the fixture, then it would be hard to change the fixture
data due to the coupling of many tests with 1 fixture.

## The mock builder pattern

To avoid the above issues and to be able to easily create test data, I can do
the following. I can create a utility test method that will allow me to easily
create `Transaction` instances.

```scala
object ModelBuilders:
  def transaction(
      id: String = "transaction-id",
      amount: Int = 1
  ) = Transaction(
    id,
    amount
  )

```

This can be easily done if I copy-paste the class twice and then modify the code.
The default values should be some correct values for the class but not coupled
with any test in particular. This way the method can be reused.

Having this utility in place I can now refactor my test:

```scala
  test("reduce reduces the amount") {
    transaction(amount = 10).reduceBy(2) should be(transaction(amount = 8))
  }
```

As we can see above, not only it is easy to create instances but also only the
relevant fields need to be populated and even better the values are visible
in the test. So the amount from 10 goes down to 8.

There is no coupling with any other test and if later on I need to refactor the 
`reduceBy` method, I can freely change the data for the above test.

Also I don't have the overhead of the fixture traits.

Creating the mock builder method is easy as it can be done by copy-pasting twice
the case class and then modifying the code and adding the default values. I've
done it in spark projects with 70+ fields in case class (tables) and it works
quite well. Maintenance is simple as well, i.e. adding 1 more field is not
a problem and doesn't break or affect existing tests - both when compiling or
running them.

## Adding fields to the domain model

Lets add 1 date-time field to `Transaction`:

```scala
case class Transaction(
    id: String,
    amount: Int, // for simplicity lets keep it an Int
    dateTime: ZonedDateTime
):
  def reduceBy(howMuch: Int) = copy(amount = amount - howMuch)
  def isAfter(other: Transaction) = dateTime.isAfter(other.dateTime)
```

We also added one more business logic method, `isAfter` to check if a transaction
is done after some other transaction - just a simple method to have to test.

We now can modify the mock builder:

```scala
object ModelBuilders:
  def transaction(
      id: String = "transaction-id",
      amount: Int = 1,
      dateTime: ZonedDateTime = zonedDateTime()
  ) = Transaction(
    id,
    amount,
    dateTime
  )
```

Well ok but we need an easy way to create `ZoneDateTime` instances. Let's use
the mock builder pattern, as we will see it will come handy in the tests.

```scala
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
```

Ok now we can easily create `Transaction` and `ZoneDateTime`. We are now ready
to test the `isAfter` method:

```scala
  test("isAfter positive") {
    transaction(dateTime = zonedDateTime(2022))
      .isAfter(
        transaction(dateTime = zonedDateTime(2021))
      ) should be(true)
  }
```

We can easily see the values required for our test. We have a transaction
with dateTime of 2022 and that is after a transaction done in 2021. Furthermore, 
the other test for `reduceBy` didn't require maintenance, and it wasn't affected
because of the new field. There is just no coupling despite both been for
the `Transaction` class.

## Summary

This is a nice and handy pattern I am using in a number of projects from testing data via spark case classes to mocking
domain model or DTO classes for api's. It makes the tests a lot easier to write, read and maintain. It also isolates the tests
from code changes.

## Sample code

All the code is available in this repository, feel free to examine it.