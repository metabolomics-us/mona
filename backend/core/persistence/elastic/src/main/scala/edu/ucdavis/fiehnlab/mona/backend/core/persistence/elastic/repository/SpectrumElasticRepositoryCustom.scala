package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import org.elasticsearch.index.query.QueryBuilder

/**
  * Created by wohlg_000 on 3/3/2016.
  */
trait SpectrumElasticRepositoryCustom  extends RSQLRepositoryCustom[Spectrum,QueryBuilder]  {

}
