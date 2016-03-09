package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import org.springframework.data.domain.{Page, Pageable}

/**
  * Created by wohlg_000 on 3/3/2016.
  */
trait SpectrumElasticRepositoryCustom  extends RSQLRepositoryCustom[Spectrum]  {

}
