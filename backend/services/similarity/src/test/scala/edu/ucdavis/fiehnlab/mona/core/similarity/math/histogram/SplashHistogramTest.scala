package edu.ucdavis.fiehnlab.mona.core.similarity.math.histogram

import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by sajjan on 12/28/16.
  */
class SplashHistogramTest extends FlatSpec with Matchers {

  "Histogram" should "match SPLASH spectra with one ion" in {
    // MassBank EA034401
    val s: SimpleSpectrum = new SimpleSpectrum("", "169.9232:3169812.7")
    s.ions.length shouldEqual 1

    val histogram = SplashHistogram.create().generate(s)
    histogram shouldEqual s.histogram
  }

  it should "match SPLASH spectra with multiple ions" in {
    val s: SimpleSpectrum = new SimpleSpectrum("", "141.0114:5064461 155.0269:44809.4 199.0169:282429")
    s.ions.length shouldEqual 3

    val histogram = SplashHistogram.create().generate(s)
    histogram shouldEqual s.histogram
  }

  it should "match SPLASH for complex spectra" in {
    val s: SimpleSpectrum = new SimpleSpectrum("", "27:1.810181 30:12.321232 31:2.710271 37:2.550255 38:2.550255 39:3.120312 41:1.970197 43:2.380238 50:15.021502 51:8.210821 57:9.690969 61:2.550255 62:2.460246 63:3.040304 68:6.080608 69:11.251125 73:1.890189 74:9.850985 75:69.29693 76:5.750575 83:20.362036 93:2.30023 94:5.910591 95:100 96:6.730673 111:27.012701 125:3.780378 141:77.59776 142:7.140714")
    s.ions.length shouldEqual 29

    val histogram = SplashHistogram.create().generate(s)
    histogram shouldEqual s.histogram
  }
}
