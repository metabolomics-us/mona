package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

/**
  * Created by wohlg_000 on 3/3/2016.
  */
trait ISpectrumElasticRepositoryCustom  extends ElasticsearchRepository[Spectrum, String]  with SpectrumElasticRepositoryCustom{

}
