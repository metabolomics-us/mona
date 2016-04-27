package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.types

import org.scalatest._

class PeakDataTest extends WordSpec with Matchers {
  "`PeakData` types" should {
    "correctly compute for relative intensities given absolute intensities only" in {
      PeakData(List(
        PeakTriple(646.3223, 64380108, 64380108),
        PeakTriple(647.3252, 26819201, 26819201),
        PeakTriple(648.3309, 7305831, 7305831)
      )).peaks shouldBe
        List(
          PeakTriple(646.3223, 64380108, 999),
          PeakTriple(647.3252, 26819201, 416),
          PeakTriple(648.3309, 7305831, 113)
        )

      PeakData(List(
        PeakTriple(78.9612, 22.14, 22.14),
        PeakTriple(96.9706, 68.24, 68.24),
        PeakTriple(242.0826, 6.547, 6.547),
        PeakTriple(454.0890, 10.7, 10.7),
        PeakTriple(455.1043, 16.62, 16.62)
      )).peaks shouldBe
        List(
          PeakTriple(78.9612, 22.14, 324),
          PeakTriple(96.9706, 68.24, 999),
          PeakTriple(242.0826, 6.547, 96),
          PeakTriple(454.0890, 10.7, 157),
          PeakTriple(455.1043, 16.62, 243)
        )
    }
  }

  "leave complete peak triples untouched" in {
    PeakData(List(
      PeakTriple(78.9612, 22.14, 324),
      PeakTriple(96.9706, 68.24, 999),
      PeakTriple(242.0826, 6.547, 96),
      PeakTriple(454.0890, 10.7, 157),
      PeakTriple(455.1043, 16.62, 243)
    )).peaks shouldBe
      List(
        PeakTriple(78.9612, 22.14, 324),
        PeakTriple(96.9706, 68.24, 999),
        PeakTriple(242.0826, 6.547, 96),
        PeakTriple(454.0890, 10.7, 157),
        PeakTriple(455.1043, 16.62, 243)
      )

    PeakData(List(
      PeakTriple(646.3223, 999, 999),
      PeakTriple(647.3252, 416, 416),
      PeakTriple(648.3309, 113, 113)
    )).peaks shouldBe
      List(
        PeakTriple(646.3223, 999, 999),
        PeakTriple(647.3252, 416, 416),
        PeakTriple(648.3309, 113, 113)
      )
  }

  "be able to hold empty peaks" in {
    PeakData.empty.peaks shouldBe List.empty
  }
}
