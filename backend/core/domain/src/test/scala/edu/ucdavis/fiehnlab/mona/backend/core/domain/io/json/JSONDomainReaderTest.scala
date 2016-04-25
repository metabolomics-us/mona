package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.scalatest.WordSpec

/**
  * Created by wohlgemuth on 2/25/16.
  */
class JSONDomainReaderTest extends WordSpec {

  "we should be able to create an instance of the reader" when {

    val reader = JSONDomainReader.create[Spectrum]

    "which should be of the tpye spectrum" should {

      "check type" in {
        assert(reader.isInstanceOf[JSONDomainReader[Spectrum]])
      }

    }

    "it should be able to read a spectrum" should {

      val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

      val spectrum: Spectrum = reader.read(input)

      "its splash key should equal the exspectations" in {
        assert(spectrum.splash.splash == "splash10-0z50000000-9c8c58860a0fadd33800")
      }

      "its compounds inchi key should be also equal the exspectations" in {
        assert(spectrum.compound(1).inchiKey == "QASFUMOKHFSJGL-LAFRSMQTSA-N")
      }

      "it should be possible to access it's metatadata" in {
        assert(spectrum.metaData != null)
        assert(spectrum.metaData.length > 0)
      }

      "we should be able to access it's compounds metadata" in {
        assert(spectrum.compound(0).metaData != null)
        assert(spectrum.compound(0).metaData.size > 0)
      }

        val metaDatas = spectrum.compound(0).metaData

        metaDatas.foreach { metaData =>

          metaData.name match {
            case "total exact mass" =>

              "the compounds total exact mass should be of type double" in {

                assert(metaData.value.isInstanceOf[Double])
                assert(metaData.value.asInstanceOf[Double] === 411.31372955200004)
              }

            case "molecule formula" =>

              "the compounds molecule formula should be of type String" in {
                assert(metaData.value.isInstanceOf[String])
                assert(metaData.value.asInstanceOf[String] ==="C27H41NO2")

              }

            case "Tocris Bioscience" =>

              "the Tocris Bioscience should be of type Integer" in {
                assert(metaData.value.isInstanceOf[Int])
                assert(metaData.value.asInstanceOf[Int] === 1623)

              }
            case _ => //nada

          }
        }
      }


    }


  "we should be able to read an array of spectrum as well" when {

    val reader: JSONDomainReader[Array[Spectrum]] = JSONDomainReader.create[Array[Spectrum]]

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json"))

    "it should cause no erros" should {
      val result: Array[Spectrum] = reader.read(input)

      "it's lenght of reqd objects should fit the expsectations" in {
        assert(result.length == 58)
      }
    }
  }

}
