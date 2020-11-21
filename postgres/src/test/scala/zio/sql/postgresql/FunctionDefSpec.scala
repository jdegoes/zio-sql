package zio.sql.postgresql

import zio.Cause
import zio.test._
import zio.test.Assertion._

object FunctionDefSpec extends PostgresRunnableSpec with ShopSchema {

  import Customers._
  import FunctionDefStandard._
  import PostgresFunctionDef._

  val spec = suite("Postgres FunctionDef")(
    testM("sin") {
      val query = select(Sin(1.0)) from customers

      val expected = 0.8414709848078965

      val testResult = execute(query).to[Double, Double](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("sind") {
      val query = select(Sind(30.0)) from customers

      val expected = 0.5

      val testResult = execute(query).to[Double, Double](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("extract") {
      val century      = Extract(Century, dob) as "century_of_birth"
      val day          = Extract(Day, dob) as "day_of_birth"
      val decade       = Extract(Decade, dob) as "decade_of_birth"
      val dow          = Extract(Dow, dob) as "dow_of_birth"
      val doy          = Extract(Doy, dob) as "doy_of_birth"
      val epoch        = Extract(Epoch, dob) as "epoch_of_birth"
      val hour         = Extract(Hour, dob) as "hour_of_birth"
      val isoDow       = Extract(IsoDow, dob) as "iso_dow_of_birth"
      val isoYear      = Extract(IsoYear, dob) as "iso_year_of_birth"
      val microseconds = Extract(Microseconds, dob) as "microseconds_of_birth"
      val millennium   = Extract(Millennium, dob) as "millennium_of_birth"
      val milliseconds = Extract(Milliseconds, dob) as "milliseconds_of_birth"
      val minute       = Extract(Minute, dob) as "minute_of_birth"
      val month        = Extract(Month, dob) as "month_of_birth"
      val quarter      = Extract(Quarter, dob) as "quarter_of_birth"
      val second       = Extract(Second, dob) as "second_of_birth"
      val week         = Extract(Week, dob) as "week_of_birth"
      val year         = Extract(Year, dob) as "year_of_birth"

      case class DateOfBirth(
        century: Double,
        day: Double,
        decade: Double,
        dow: Double,
        doy: Double,
        epoch: Double,
        hour: Double,
        isoDow: Double,
        isoYear: Double,
        microseconds: Double,
        millennium: Double,
        milliseconds: Double,
        minute: Double,
        month: Double,
        quarter: Double,
        second: Double,
        week: Double,
        year: Double
      )

      val query = select(
        century ++ day ++ decade ++ dow ++ doy ++ epoch ++ hour ++ isoDow ++ isoYear ++ microseconds ++ millennium ++ milliseconds ++ minute ++ month ++ quarter ++ second ++ week ++ year
      ) from customers

      println(renderRead(query))

      val expected =
        DateOfBirth(
          century = 20.0,
          day = 5.0,
          decade = 198.0,
          dow = 3.0,
          doy = 5.0,
          epoch = 410572800.0,
          hour = 0.0,
          isoDow = 3.0,
          isoYear = 1983.0,
          microseconds = 0.0,
          millennium = 2.0,
          milliseconds = 0.0,
          minute = 0.0,
          month = 1.0,
          quarter = 1.0,
          second = 0.0,
          week = 1.0,
          year = 1983.0
        )

      val testResult =
        execute(query).to[
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          Double,
          DateOfBirth
        ] { case row =>
          DateOfBirth(
            row._1,
            row._2,
            row._3,
            row._4,
            row._5,
            row._6,
            row._7,
            row._8,
            row._9,
            row._10,
            row._11,
            row._12,
            row._13,
            row._14,
            row._15,
            row._16,
            row._17,
            row._18
          )
        }

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    }
  )
}
