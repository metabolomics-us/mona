package edu.ucdavis.fiehnlab.mona.backend.core.domain.util.io.msp

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp.MSPReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.upload.RawParsedSpectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainReadEventHandler
import org.scalatest.wordspec.AnyWordSpec

import java.io.StringReader

/**
  * Characterization tests for MSPReader, ported directly from angular-msp-parser's
  * msp-parser-lib.service.spec.ts fixtures and assertions, so the Scala port is checked against
  * the exact same real-world inputs the original library was validated with
  */
class MSPReaderTest extends AnyWordSpec {

  private def parseAll(data: String): List[RawParsedSpectrum] = {
    val buffer = scala.collection.mutable.ListBuffer[RawParsedSpectrum]()
    new MSPReader().read(new StringReader(data), new DomainReadEventHandler[RawParsedSpectrum] {
      override def readEvent(event: RawParsedSpectrum): Unit = buffer += event
    })
    buffer.toList
  }

  "MSPReader" should {
    "parse a spectrum with unaccurate integer masses" in {
      val data =
        "Name: glutamate_RI 528609 \n" +
        "Synon: ##chromatogram=060121bylcs01 \n" +
        "Formula: n/a \n" +
        "CASNO: 56860 \n" +
        "ID: 483 \n" +
        "Comment: fiehn \n" +
        "Num peaks: 151 \n" +
        "85   78;  86   52;  87   24;  88   15;  89   18; \n" +
        "90    4;  91    3;  92    2;  93    6;  94    2; \n" +
        "95   11;  96    8;  97    9;  98   39;  99   33; \n" +
        "100  308; 101   64; 102   32; 103   52; 104    7; \n" +
        "105   12; 106    1; 107    1; 108    1; 110    7; \n" +
        "111    3; 112   45; 113   42; 114   70; 115   65; \n" +
        "116   28; 117   52; 118   11; 119   19; 120    2; \n" +
        "121    1; 124    2; 125    1; 126    8; 127    5; \n" +
        "128  928; 129  191; 130   81; 131   83; 132   60; \n" +
        "133  191; 134   32; 135   20; 136    2; 139    2; \n" +
        "140  101; 141   15; 142   13; 143   10; 144   11; \n" +
        "145    6; 146    6; 147  558; 148   99; 149  143; \n" +
        "150   20; 151    9; 152    1; 153    1; 154    8; \n" +
        "155    9; 156  498; 157   84; 158   93; 159   19; \n" +
        "160    9; 161    4; 162    2; 163    8; 164    1; \n" +
        "168    4; 169    2; 170    3; 171    1; 172   11; \n" +
        "173    5; 174   18; 175    4; 176    3; 177    5; \n" +
        "178    1; 182    1; 183    1; 184    3; 185    1; \n" +
        "186    5; 187    2; 188    8; 189    8; 190    4; \n" +
        "191    3; 192    1; 193    1; 198    2; 199    1; \n" +
        "200    2; 201    1; 202   20; 203   13; 204   53; \n" +
        "205   13; 206    5; 207    2; 214   21; 215    5; \n" +
        "216   10; 217    2; 218   56; 219   14; 220    5; \n" +
        "221   20; 222    4; 223    2; 228    3; 229    3; \n" +
        "230  187; 231   42; 232   21; 233    3; 244    4; \n" +
        "245   16; 246  999; 247  222; 248   94; 249   15; \n" +
        "250    3; 258   30; 259    7; 260    4; 272    1; \n" +
        "273    2; 274   11; 275    3; 276    1; 320   10; \n" +
        "321    3; 322    1; 332    1; 347    1; 348   42; \n" +
        "349   15; 350    7; 351    1; 363   17; 364    6; \n" +
        "365    2; \n"

      val results = parseAll(data)
      assert(results.length == 1)
      val res = results.head
      assert(res.names.head == "glutamate")
      assert(res.meta.length == 4)
      assert(!res.accurate)
      assert(res.spectrum.split(" ").length == 151)
    }

    "parse a spectrum with accurate double masses and structure metadata" in {
      val data =
        "Name: Carnosic Acid\n" +
        "InChI: InChI=1S/C20H28O4/c1-11(2)13-10-12-6-7-14-19(3,4)8-5-9-20(14,18(23)24)15(12)17(22)16(13)21/h10-11,14,21-22H,5-9H2,1-4H3,(H,23,24)/t14-,20+/m0/s1\n" +
        "InChIKey: QRYRORQUOLYVBU-VBKZILBWSA-N\n" +
        "Molecular Formula: C20H28O4\n" +
        "Exact Mass: 332.1987488\n" +
        "Instrument: Thermo Finnigan LTQ\n" +
        "Instrument Type: Linear Ion Trap\n" +
        "Ion Source: ESI Ion Max\n" +
        "Capillary Temperature: 275 C\n" +
        "Source Voltage: 3.50 kV\n" +
        "Sample Introduction: Direct Infusion\n" +
        "Collision Energy: 35%\n" +
        "Raw Data File: NP_C1_102_p2_E11_POS_iTree_14.raw\n" +
        "Ion Mode: Positive\n" +
        "Precursor Type: [M+K]+\n" +
        "NumScansAveraged: 39\n" +
        "PrecursorMZ: 371.07\n" +
        "Num Peaks: 11\n" +
        "184.970665 30.729010; 313.005361 16.370305; 327.068559 2393.361486; 329.050559 15.778810;\n" +
        "343.090702 32.622264; 344.077607 12.669677; 353.073218 24.342673; 355.086382 17.221813;\n" +
        "356.084138 41.796034; 357.067589 24.117027; 371.083264 13.116330;"

      val results = parseAll(data)
      assert(results.length == 1)
      val res = results.head
      assert(res.names.head == "Carnosic Acid")
      assert(res.meta.length == 14)
      assert(res.accurate)
      assert(res.spectrum.split(" ").length == 11)
      assert(res.inchi.contains("InChI=1S/C20H28O4/c1-11(2)13-10-12-6-7-14-19(3,4)8-5-9-20(14,18(23)24)15(12)17(22)16(13)21/h10-11,14,21-22H,5-9H2,1-4H3,(H,23,24)/t14-,20+/m0/s1"))
      assert(res.inchiKey.contains("QRYRORQUOLYVBU-VBKZILBWSA-N"))
    }

    "emit no spectrum for a block with zero peaks" in {
      val data =
        "NAME: Metamitron-desamino; LC-ESI-ITFT; MS2; CE: 35%; R=7500; [M+H]+\n" +
        "PRECURSORMZ: 188.0818\n" +
        "INSTRUMENTTYPE: LC-ESI-ITFT\n" +
        "INSTRUMENT: LTQ Orbitrap XL Thermo Scientific\n" +
        "License: CC BY-SA\n" +
        "COLLISIONENERGY: 35 % (nominal)\n" +
        "FORMULA: C10H9N3O1\n" +
        "RETENTIONTIME: -1\n" +
        "IONMODE: P\n" +
        "SearchID: MassBank: EA000401; KEGG: ; CAS: CAS 36993-94-9; ChemSpider: 157884; PubChem CID: 181502; PubChem SID:\n" +
        "Num Peaks: 0\n"

      assert(parseAll(data).isEmpty)
    }

    "parse external ids from SearchID into Database Identifier metadata" in {
      val data =
        "NAME: Metamitron-desamino; LC-ESI-ITFT; MS2; CE: 35%; R=7500; [M+H]+\n" +
        "PRECURSORMZ: 188.0818\n" +
        "INSTRUMENTTYPE: LC-ESI-ITFT\n" +
        "INSTRUMENT: LTQ Orbitrap XL Thermo Scientific\n" +
        "License: CC BY-SA\n" +
        "COLLISIONENERGY: 35 % (nominal)\n" +
        "FORMULA: C10H9N3O1\n" +
        "RETENTIONTIME: -1\n" +
        "IONMODE: P\n" +
        "SearchID: MassBank: EA000401; KEGG: ; CAS: CAS 36993-94-9; ChemSpider: 157884; PubChem CID: 181502; PubChem SID:\n" +
        "Num Peaks: 7\n" +
        "77.0385\t5\n" +
        "85.0396\t17\n" +
        "104.0495\t75\n" +
        "119.0604\t132\n" +
        "147.0555\t3\n" +
        "160.0871\t999\n" +
        "188.082\t86\n"

      val results = parseAll(data)
      assert(results.length == 1)
      val res = results.head
      assert(res.names.head == "Metamitron-desamino; LC-ESI-ITFT; MS2; CE: 35%; R=7500; [M+H]+")
      assert(res.meta.length == 12)
      assert(res.accurate)
      assert(res.spectrum.split(" ").length == 7)
      assert(res.meta.exists(_.category.contains("Database Identifier")))
    }

    "parse a library of thousands of concatenated spectra without a StackOverflowError" in {
      val block =
        "Name: glutamate_RI 528609 \n" +
        "Synon: ##chromatogram=060121bylcs01 \n" +
        "Num peaks: 3 \n" +
        "85   78;  86   52;  87   24; \n"
      val data = block * 5000

      val results = parseAll(data)
      assert(results.length == 5000)
      assert(results.forall(_.spectrum.split(" ").length == 3))
    }
  }
}
