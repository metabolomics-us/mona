package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import org.scalacheck.Gen

object Generators {

  val length = 50

  def chars: Gen[Char] = Gen.oneOf(Gen.alphaNumChar, Gen.oneOf(" -!\"#$%&'()-^\\@[;:,./=~|`{+*<>?_"))

  val validString = for (cs <- Gen.listOfN(length, chars)) yield cs.mkString

  val validInt = Gen.chooseNum(Int.MinValue, Int.MaxValue)

  val validDouble = Gen.chooseNum(Double.MinValue, Double.MaxValue)

  val validDate = for {
    year <- Gen.chooseNum(1900, 9999)
    month <- Gen.chooseNum(1, 12)
    day <- {
      if (month == 2) Gen.chooseNum(1, 28)
      else if (List(4, 6, 9, 11).contains(month)) Gen.chooseNum(1, 30)
      else Gen.chooseNum(1, 31)
    }
  } yield s"$year.$month.$day"

  val validTag = for {
    start <- Gen.alphaChar
    subtag <- Gen.listOfN(length, Gen.oneOf(Gen.alphaNumChar, Gen.oneOf("-_/")))
    value <- validString
  } yield start + subtag.mkString + " " + value

  val completePeak = for {
    mz <- Gen.posNum[Double]
    int <- Gen.posNum[Double]
    relInt <- Gen.posNum[Double]
  } yield s"$mz $int $relInt"

}
