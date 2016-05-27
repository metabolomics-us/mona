package edu.ucdavis.fiehnlab.mona.backend.services.repository.layout

import java.io.File
import java.util.Calendar

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CurationUtilities
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SplashUtil

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
  override def layout(spectrum: Spectrum): File = new File(baseDir, spectrum.compound(0).inchiKey)
}

/**
  * generates a layout based on year\month\day\inchikey
  *
  * @param baseDir
  */
class YearMonthDayInchiKeyLayout(val baseDir: File) extends FileLayout {
  /**
    * defines a layout where to store the given spectrum
    * on a file system
    *
    * @param spectrum
    * @return
    */
  override def layout(spectrum: Spectrum): File = {
    val calendar = Calendar.getInstance()
    calendar.setTime(spectrum.lastUpdated)

    val year = new File(baseDir, calendar.get(Calendar.YEAR).toString)
    val yearMonth = new File(year, calendar.get(Calendar.MONTH).toString)
    val yearMonthDay = new File(yearMonth, calendar.get(Calendar.DAY_OF_MONTH).toString)

    new File(yearMonthDay, spectrum.compound(0).inchiKey)
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

    var institution = spectrum.submitter.institution

    if(institution == null){
      institution = "None"
    }

    institution = institution.replace(' ', '_')


    val submitterDir = new File(baseDir, institution)


    var compound: Compound = CurationUtilities.getFirstBiologicalCompound(spectrum)

    if (compound == null) {
      compound = spectrum.compound.head
    }

    val inchiKeyDir = new File(submitterDir, compound.inchiKey)
    val splashDir = new File(inchiKeyDir, SplashUtil.splash(spectrum.spectrum, SpectraType.MS))

    splashDir
  }
}