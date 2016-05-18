package edu.ucdavis.fiehnlab.mona.backend.services.repository.layout

import java.io.File

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum

/**
  * Created by wohlg_000 on 5/17/2016.
  */
trait FileLayout {

  /**
    * the base directory
    */
  val baseDir:File
  /**
    * defines a layout where to store the given spectrum
    * on a file system
 *
    * @param spectrum
    * @return
    */
  def layout(spectrum:Spectrum) : File
}

/**
  * generates a layout based on the inchi key of the spectrum
  *
  * so the file will be baseDir/inchiKey
  * 
  * @param baseDir
  */
class InchiKeyLayout(val baseDir:File) extends FileLayout{
  /**
    * defines a layout where to store the given spectrum
    * on a file system
    *
    * @param spectrum
    * @return
    */
  override def layout(spectrum: Spectrum): File = new File(baseDir,spectrum.compound(0).inchiKey)
}