package edu.ucdavis.fiehnlab.mona.backend.services.repository.layout

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Spectrum}

import java.io.File
import java.util.{Calendar, Locale}
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CurationUtilities
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SplashUtil

import java.text.SimpleDateFormat
import scala.jdk.CollectionConverters._

/**
  * Created by wohlg_000 on 5/17/2016.
  */
trait FileLayout {

  /**
    * the base directory
    */
  val baseDir: File

  /**
    * defines a layout where to store the given spectrum
    * on a file system
    *
    * @param spectrum
    * @return
    */
  def layout(spectrum: Spectrum): File
}

/**
  * generates a layout based on the inchi key of the spectrum
  *
  * so the file will be baseDir/inchiKey
  *
  * @param baseDir
  */
class InchiKeyLayout(val baseDir: File) extends FileLayout {
  /**
    * defines a layout where to store the given spectrum
    * on a file system
    *
    * @param spectrum
    * @return
    */
  override def layout(spectrum: Spectrum): File = new File(baseDir, spectrum.getCompound.get(0).getInchiKey)
}

/**
  * generates a layout based on year\month\day\inchikey
  *
  * @param baseDir
  */
class YearMonthDayInchiKeyLayout(val baseDir: File) extends FileLayout with LazyLogging{
  /**
    * defines a layout where to store the given spectrum
    * on a file system
    *
    * @param spectrum
    * @return
    */
  override def layout(spectrum: Spectrum): File = {
    val calendar = Calendar.getInstance()
    val formatter: SimpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
    logger.info(s"${spectrum.getLastUpdated}")
    calendar.setTime(formatter.parse(spectrum.getLastUpdated))
    logger.info(s"${formatter.parse(spectrum.getLastUpdated)}")

    val year = new File(baseDir, calendar.get(Calendar.YEAR).toString)
    val yearMonth = new File(year, calendar.get(Calendar.MONTH).toString)
    val yearMonthDay = new File(yearMonth, calendar.get(Calendar.DAY_OF_MONTH).toString)

    logger.info(s"Year month day is: ${yearMonthDay}")

    new File(yearMonthDay, spectrum.getCompound.get(0).getInchiKey)
  }
}

class SubmitterInchiKeySplashId(val baseDir: File) extends FileLayout {
  /**
    * defines a layout where to store the given spectrum
    * on a file system
    *
    * @param spectrum
    * @return
    */
  override def layout(spectrum: Spectrum): File = {

    var institution = spectrum.getSubmitter.getInstitution

    if (institution == null) {
      institution = "None"
    }

    institution = institution.replace(' ', '_')


    val submitterDir = new File(baseDir, institution)


    var compound: Compound = CurationUtilities.getFirstBiologicalCompound(spectrum)

    if (compound == null) {
      compound = spectrum.getCompound.asScala.head
    }

    val inchiKey: String =
      if (compound.getInchiKey != null) compound.getInchiKey
      else compound.getMetaData.asScala.filter(_.getName == "InChIKey").map(_.getValue.toString).headOption.getOrElse("none")

    val inchiKeyDir = new File(submitterDir, inchiKey)
    val splashDir = new File(inchiKeyDir, SplashUtil.splash(spectrum.getSpectrum, SpectraType.MS))

    splashDir
  }
}
