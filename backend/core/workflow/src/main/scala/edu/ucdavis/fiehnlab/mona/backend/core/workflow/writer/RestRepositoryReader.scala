package edu.ucdavis.fiehnlab.mona.backend.core.workflow.writer

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.batch.item.ItemReader

/**
  * Created by wohlgemuth on 3/17/16.
  */
class RestRepositoryReader extends ItemReader[Spectrum] {

  override def read(): Spectrum = {

    null
  }
}
