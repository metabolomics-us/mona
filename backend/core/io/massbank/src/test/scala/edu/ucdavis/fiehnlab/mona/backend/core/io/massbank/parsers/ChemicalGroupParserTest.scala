package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.groups._
import org.scalatest._

class ChemicalGroupParserTest extends WordSpec with Matchers with ChemicalGroupParser {
  "A `ChemicalGroupParser`" should {
    "correctly parse complete base metadata" in {
      val input = """CH$NAME: D-beta-Homophenylalanine
                    |CH$NAME: (R)-3-Amino-4-phenylbutyric acid
                    |CH$COMPOUND_CLASS: Amino acids
                    |CH$FORMULA: C10H13NO2
                    |CH$EXACT_MASS: 179.09463
                    |CH$SMILES: OC(=O)CC(N)Cc(c1)cccc1
                    |CH$IUPAC: InChI=1S/C10H13NO2/c11-9(7-10(12)13)6-8-4-2-1-3-5-8/h1-5,9H,6-7,11H2,(H,12,13)/t9-/m1/s1
                    |CH$LINK: CAS 145149-50-4""".stripMargin

      val expected = ChemicalGroup(
        List(
          "D-beta-Homophenylalanine",
          "(R)-3-Amino-4-phenylbutyric acid"
        ),
        Some("Amino acids"),
        Some("C10H13NO2"),
        Some("179.09463"),
        Some("OC(=O)CC(N)Cc(c1)cccc1"),
        Some("InChI=1S/C10H13NO2/c11-9(7-10(12)13)6-8-4-2-1-3-5-8/h1-5,9H,6-7,11H2,(H,12,13)/t9-/m1/s1"),
        Map("CAS" -> "145149-50-4")
      )

      val result = parse(chemicalGroup, input)
      result shouldBe a[Success[_]]
      result.get shouldBe expected
    }

    "accept iterative lines on the 'CH$NAME' field as a list and 'CH$LINK' as a map" in {
      val input = """CH$NAME: Indole-3-acetyl-L-phenylalanine
                    |CH$NAME: IAA-L-Phe
                    |CH$NAME: Indole-3-acetylphenylalanine
                    |CH$NAME: L-phenylalanine, N-(1H-indol-3-ylacetyl)-
                    |CH$NAME: L-Phenylalanine, N-(1H-indol-3-ylacetyl)- (9CI)
                    |CH$LINK: CAS 57105-50-7
                    |CH$LINK: PUBCHEM SID:841899 CID:644227""".stripMargin

      val expected = ChemicalGroup(
        List(
          "Indole-3-acetyl-L-phenylalanine",
          "IAA-L-Phe",
          "Indole-3-acetylphenylalanine",
          "L-phenylalanine, N-(1H-indol-3-ylacetyl)-",
          "L-Phenylalanine, N-(1H-indol-3-ylacetyl)- (9CI)"
        ),
        None, None, None, None, None,
        Map(
          "CAS" -> "57105-50-7",
          "PUBCHEM" -> "SID:841899 CID:644227"
        )
      )

      val result = parse(chemicalGroup, input)
      result shouldBe a[Success[_]]
      result.get shouldBe expected
    }
  }

}
