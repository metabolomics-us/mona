package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{MetaDataDAO, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData

import scala.math._
import scala.util.control.Breaks._
import org.springframework.batch.item.ItemProcessor
import scala.jdk.CollectionConverters._
import scala.collection.mutable.{Buffer}
@Step(description = "this step will calculate entropy for a given spectrum and update the metadata")
class SpectralEntropy extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  override def process(spectrum: Spectrum): Spectrum = {
    val spec_a: String = spectrum.getSpectrum
    val array_a: Array[Array[Double]] = toArray(spec_a)

    val clean_a: Array[Array[Double]] = clean_spectrum(array_a)
    var intensity_a: Array[Double] = Array()
    val peak_a: Int = clean_a.length
    for (x <- clean_a) {
      intensity_a = intensity_a :+ x(1)
    }
    logger.info(s"Peak number is: ${peak_a}")

    val entropy_a: Double = entropy(intensity_a)
    val normalized_entropy_a: Double = entropy_a/log(peak_a)

    logger.info(s"Calculated Spectral Entropy is: ${entropy_a}")
    logger.info(s"Calculated Normalized Entropy is: ${normalized_entropy_a}")

    val updatedMetaData: Buffer[MetaDataDAO] = spectrum.getMetaData.asScala :+
      new MetaDataDAO(null, CommonMetaData.SPECTRAL_ENTROPY, entropy_a.toString, false, "computed", true, null) :+
      new MetaDataDAO(null, CommonMetaData.NORMALIZED_ENTROPY, normalized_entropy_a.toString, false, "computed", true, null)

    spectrum.setMetaData(updatedMetaData.asJava)
    spectrum
  }

  private def toArray(spec: String): Array[Array[Double]] = {
    spec.trim.split(" ").collect {
      case ion: String if ion.nonEmpty =>
        val peak = ion.split(":")
        Array(peak(0).toDouble, peak(1).toDouble)
    }
  }

  private def clean_spectrum(spec: Array[Array[Double]], ms2_da: Double = 0.05, ms2_ppm: Double = -1): Array[Array[Double]] = {
    var intensity_sum: Double = 0
    for (peak <- spec) {
      intensity_sum += peak(1)
    }
    // Add non-zero peak and normalize it.
    var spec_new: Array[Array[Double]] = Array()
    for (peak <- spec) {
      if (peak(1) > 0.0) {
        peak(1) /= intensity_sum
        spec_new = spec_new :+ peak
      }
    }
    // Centroid the spectrum
    spec_new = centroid_spectrum(spec_new, ms2_da, ms2_ppm)
    spec_new
  }

  private def centroid_spectrum(spec_ori: Array[Array[Double]], ms2_da: Double, ms2_ppm: Double): Array[Array[Double]] = {
    // Sort the spectrum by m/z
    var spec = spec_ori.map(_.clone)
    spec = spec.sortBy(x => x(0))

    // Get intensity order
    var intensity_order: Array[Array[Double]] = Array()
    var i = 0
    for (peak <- spec) {
      intensity_order = intensity_order :+ Array(i, 0.0 - peak(1))
      i += 1
    }
    intensity_order = intensity_order.sortBy(x => x(1))

    var spec_new: Array[Array[Double]] = Array()
    for (i_order <- intensity_order) {
      var i = i_order(0).toInt
      var mz_delta_allowed: Double = ms2_da
      if (mz_delta_allowed < 0) {
        mz_delta_allowed = ms2_ppm * 1e-6 * spec(i)(0)
      }

      if (spec(i)(1) > 0) {
        // Find left board for current peak
        var i_left = i - 1
        breakable {
          while (i_left >= 0) {
            var mz_delta_left = spec(i)(0) - spec(i_left)(0)
            if (mz_delta_left <= mz_delta_allowed) {
              i_left -= 1
            } else {
              break
            }
          }
        }
        i_left += 1

        //Find right board for current peak
        var i_right = i + 1
        breakable {
          while (i_right < spec.length) {
            var mz_delta_right = spec(i_right)(0) - spec(i)(0)
            if (mz_delta_right <= mz_delta_allowed) {
              i_right += 1
            } else {
              break
            }
          }
        }

        // Merge those peaks
        var intensity_sum: Double = 0
        var intensity_weighted_sum: Double = 0
        for (j <- i_left until i_right) {
          intensity_sum += spec(j)(1)
          intensity_weighted_sum += spec(j)(0) * spec(j)(1)
        }
        spec_new = spec_new :+ Array(intensity_weighted_sum / intensity_sum, intensity_sum)

        for (j <- i_left until i_right) {
          spec(j)(1) = 0
        }
      }
    }

    // Sort again the spectrum by m/z
    spec_new.sortBy(x => x(0))
  }

  private def entropy(x: Array[Double]): Double = {
    var s: Double = 0
    for (i <- x) {
      s += i
    }

    var h: Double = 0
    for (i <- x) {
      if (i > 0) {
        var is = i / s
        h += 0.0 - is * math.log(is)
      }
    }
    h
  }

}
