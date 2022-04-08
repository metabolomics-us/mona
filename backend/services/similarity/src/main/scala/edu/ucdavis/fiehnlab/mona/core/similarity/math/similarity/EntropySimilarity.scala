package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum
import edu.ucdavis.fiehnlab.mona.core.similarity.util.SpectrumUtils

class EntropySimilarity extends Similarity with LazyLogging{

  def compute(unknown: SimpleSpectrum, reference: SimpleSpectrum, removePrecursorIon: Boolean): Double = {
    if(removePrecursorIon) {
      val newUnknown: SimpleSpectrum = removePrecursor(unknown, unknown.precursorMZ)
      val newReference: SimpleSpectrum = removePrecursor(reference, unknown.precursorMZ)
      weighted_entropy_similarity(SpectrumUtils.toArray(newUnknown.ions), SpectrumUtils.toArray(newReference.ions), .05, 0)
    } else {
      weighted_entropy_similarity(SpectrumUtils.toArray(unknown.ions), SpectrumUtils.toArray(reference.ions), .05, 0)
    }
  }

  def weighted_entropy_similarity(spec_a: Array[Array[Double]], spec_b: Array[Array[Double]],
                                  ms2_da: Double = -1, ms2_ppm: Double = -1): Double = {
    val spec_a_cleaned = SpectrumUtils.clean_spectrum(spec_a, ms2_da = ms2_da, ms2_ppm = ms2_ppm)
    val spec_b_cleaned = SpectrumUtils.clean_spectrum(spec_b, ms2_da = ms2_da, ms2_ppm = ms2_ppm)

    // Match peaks in spectra
    val (a, b) = match_peaks_in_spectra(spec_a = spec_a_cleaned, spec_b = spec_b_cleaned, ms2_da = ms2_da, ms2_ppm = ms2_ppm)

    // Calculate similarity.
    val weighted_entropy_similarity_score: Double = weighted_entropy_similarity_(a, b)
    weighted_entropy_similarity_score
  }

  private def weighted_entropy_similarity_(a: Array[Double], b: Array[Double]): Double = {
    val aw = weight_intensity_for_entropy(a)
    val bw = weight_intensity_for_entropy(b)
    entropy_similarity_(aw, bw)
  }

  private def weight_intensity_for_entropy(x: Array[Double]): Array[Double] = {
    val weight_start: Double = 0.25
    val weight_slope: Double = 0.5
    val weight_e_max: Double = 1.5
    val e = entropy(x)
    if (e > weight_e_max) {
      x
    } else {
      val weight = weight_start + weight_slope * e
      var spec = x.clone()
      for (i <- spec.indices) {
        spec(i) = math.pow(spec(i), weight)
      }
      // Re-normalize to sum==1
      var spec_sum: Double = 0
      for (i <- spec) {
        spec_sum += i
      }
      for (i <- spec.indices) {
        spec(i) /= spec_sum
      }
      spec
    }
  }

  def entropy_similarity(spec_a: Array[Array[Double]], spec_b: Array[Array[Double]],
                         ms2_da: Double = -1, ms2_ppm: Double = -1): Double = {
    val spec_a_cleaned = SpectrumUtils.clean_spectrum(spec_a, ms2_da = ms2_da, ms2_ppm = ms2_ppm)
    val spec_b_cleaned = SpectrumUtils.clean_spectrum(spec_b, ms2_da = ms2_da, ms2_ppm = ms2_ppm)

    // Match peaks in spectra
    val (a, b) = match_peaks_in_spectra(spec_a = spec_a_cleaned, spec_b = spec_b_cleaned, ms2_da = ms2_da, ms2_ppm = ms2_ppm)

    // Calculate similarity.
    val entropy_similarity_score: Double = entropy_similarity_(a, b)
    entropy_similarity_score
  }

  private def match_peaks_in_spectra(spec_a: Array[Array[Double]], spec_b: Array[Array[Double]],
                                     ms2_da: Double, ms2_ppm: Double): (Array[Double], Array[Double]) = {
    var a = 0
    var b = 0
    var spec_merged_a: Array[Double] = Array()
    var spec_merged_b: Array[Double] = Array()
    var peak_b_int: Double = 0
    while (a < spec_a.length & b < spec_b.length) {
      var mz_delta_allowed: Double = ms2_da
      if (mz_delta_allowed < 0) {
        mz_delta_allowed = ms2_ppm * 1e-6 * spec_a(a)(0)
      }
      val mass_delta = spec_a(a)(0) - spec_b(b)(0)
      if (mass_delta < 0.0 - mz_delta_allowed) {
        // Peak only existed in spec a.
        spec_merged_a = spec_merged_a :+ spec_a(a)(1)
        spec_merged_b = spec_merged_b :+ peak_b_int
        peak_b_int = 0
        a += 1
      } else if (mass_delta > mz_delta_allowed) {
        // Peak only existed in spec b.
        spec_merged_a = spec_merged_a :+ 0.0
        spec_merged_b = spec_merged_b :+ spec_b(b)(1)
        b += 1
      } else {
        peak_b_int += spec_b(b)(1)
        b += 1
      }
    }
    if (peak_b_int > 0) {
      spec_merged_a = spec_merged_a :+ spec_a(a)(1)
      spec_merged_b = spec_merged_b :+ peak_b_int
      peak_b_int = 0.0
      a += 1
    }
    if (b < spec_b.length) {
      for (x <- b until spec_b.length) {
        spec_merged_a = spec_merged_a :+ 0.0
        spec_merged_b = spec_merged_b :+ spec_b(x)(1)
      }
    }
    if (a < spec_a.length) {
      for (x <- a until spec_a.length) {
        spec_merged_a = spec_merged_a :+ spec_a(x)(1)
        spec_merged_b = spec_merged_b :+ 0.0
      }
    }
    (spec_merged_a, spec_merged_b)
  }

  private def entropy_similarity_(a: Array[Double], b: Array[Double]): Double = {
    if (a.length == 0 | b.length == 0) {
      0.0
    } else {
      val ab = add_arrays(a, b)
      val entropy_distance: Double = 2 * entropy(ab) - entropy(a) - entropy(b)
      1 - entropy_distance / (math.log(4))
    }
  }

  private def entropy(x: Array[Double]): Double = {
    var s: Double = 0
    for (i <- x) {
      s += i
    }
    var h: Double = 0
    for (i <- x) {
      if (i > 0) {
        val is = i / s
        h += 0.0 - is * math.log(is)
      }
    }
    h
  }

  private def add_arrays(a: Array[Double], b: Array[Double]): Array[Double] = {
    val ab = a.clone()
    for (i <- a.indices) {
      ab(i) = (ab(i) + b(i)) / 2
    }
    ab
  }
}
