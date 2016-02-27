package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.data.repository.CrudRepository

/**
  * Created by wohlgemuth on 2/26/16.
  */
trait ISpectrumRepositoryCustom extends CrudRepository[Spectrum, String] with SpectrumRepositoryCustom {


}
