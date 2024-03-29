package edu.ucdavis.fiehnlab.mona.core.similarity.types

import edu.ucdavis.fiehnlab.mona.core.similarity.util.SpectrumUtils

/**
  * Created by sajjan on 10/11/16.
  */

/**
  *
  * @param id
  * @param spectrum
  */
case class StoredSpectrum(id: String, spectrum: String)

/**
  *
  * @param mz
  * @param intensity
  */
case class Ion(mz: Double, intensity: Double) {
  override def toString: String = s"$mz:$intensity"
}

/**
  *
  * @param id
  * @param ions
  * @param public
  */
class SimpleSpectrum(val id: String, val ions: Array[Ion], val precursorMZ: Double, val tags: Array[String], val public: Boolean, val adducts: Array[Double]) {

  /**
    * Constructors
    */

  def this(id: String, ions: Array[Ion], precursorMZ: Double, tags: Array[String]) = this(id, ions, precursorMZ, tags, true, Array())

  def this(id: String, ions: Array[Ion], tags: Array[String]) = this(id, ions, -1, tags, true, Array())

  def this(id: String, ions: Array[Ion]) = this(id, ions, -1, Array(), true, Array())

  def this(id: String, spectrumString: String, precursorMZ: Double, tags: Array[String]) = this(id, SpectrumUtils.stringToIons(spectrumString), precursorMZ, tags, true, Array())

  def this(id: String, spectrumString: String, precursorMZ: Double, tags: Array[String], adducts: Array[Double]) = this(id, SpectrumUtils.stringToIons(spectrumString), precursorMZ, tags, true, adducts)

  def this(id: String, spectrumString: String, tags: Array[String]) = this(id, SpectrumUtils.stringToIons(spectrumString), -1, tags, true, Array())

  def this(id: String, spectrumString: String, tags: Array[String], adducts: Array[Double]) = this(id, SpectrumUtils.stringToIons(spectrumString), -1, tags, true, adducts)

  def this(id: String, spectrumString: String) = this(id, SpectrumUtils.stringToIons(spectrumString), -1, Array(), true, Array())

  def this(id: String, spectrumString: String, precursorMZ: Double) = this(id, SpectrumUtils.stringToIons(spectrumString), precursorMZ, Array(), true, Array())

  def this(id: String, spectrumString: String, precursorMZ: Double, adducts: Array[Double]) = this(id, SpectrumUtils.stringToIons(spectrumString), precursorMZ, Array(), true, adducts)

  def this(id: String, spectrumString: String, adducts: Array[Double]) = this(id, SpectrumUtils.stringToIons(spectrumString), -1, Array(), true, adducts)

  def this(spectrum: StoredSpectrum) = this(spectrum.id, spectrum.spectrum)


  lazy val fragments: Map[Double, Ion] = ions.sortBy(_.mz).map(ion => (ion.mz, ion)).toMap


  lazy val basePeak: Ion = ions.maxBy(_.intensity)

  lazy val minimumIon: Ion = ions.minBy(_.mz)

  lazy val maximumIon: Ion = ions.maxBy(_.mz)

  lazy val topIons: Array[Ion] = ions.sortBy(-_.intensity).take(10)

  lazy val topBinnedIons: Array[Ion] = topIons.map(SpectrumUtils.roundMZ)

  lazy val highIntensityIons: Array[Ion] = ions.filter(_.intensity > 0.5 * basePeak.intensity).sortBy(-_.intensity)

  lazy val highIntensityBinnedIons: Array[Ion] = highIntensityIons.map(SpectrumUtils.roundMZ)

  lazy val norm: Double = math.sqrt(ions.map(x => x.intensity * x.intensity).sum)


  lazy val splash: String = SpectrumUtils.splashSpectrum(this)

  lazy val histogram: String = splash.split("-")(2)


  override def equals(obj: scala.Any): Boolean =
    obj match {
      case obj: SimpleSpectrum => obj.splash.equals(this.splash)
      case _ => false
    }

  override def toString: String = SpectrumUtils.ionsToString(ions)

  override def hashCode(): Int = splash.hashCode
}


trait BinnedSimpleSpectrum extends SimpleSpectrum





