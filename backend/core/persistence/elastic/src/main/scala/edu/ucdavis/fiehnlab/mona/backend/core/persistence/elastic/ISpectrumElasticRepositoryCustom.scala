package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

/**
  * Created by wohlg_000 on 3/3/2016.
  */
@Repository("spectrumElasticRepository")
trait ISpectrumElasticRepositoryCustom  extends ElasticsearchRepository[Spectrum, String]  with SpectrumElasticRepositoryCustom{

}
