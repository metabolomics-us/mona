package edu.ucdavis.fiehnlab.mona.backend.services.repository.utility

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 6/24/16.
  */
@Component
class FindDirectory {

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}")
  private val exportDirectory: String = null

  /**
    * computes our corrected directory for usage
    * @return
    */
  def dir: String = {
    if(exportDirectory.endsWith(System.getProperty("file.separator"))) {
      s"${exportDirectory}mona_repository"
    } else {
      s"${exportDirectory}${System.getProperty("file.separator")}mona_repository"
    }
  }
}
