package edu.ucdavis.fiehnlab.mona.backend.curation

import edu.ucdavis.fiehnlab.mona.backend.curation.reader.RestRepositoryReader
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}

/**
  * Created by wohlg on 3/20/2016.
  */
@Configuration
@ComponentScan(Array("edu/ucdavis/fiehnlab/mona/backend/curation"))
class TestConfig {
  @Bean
  def restRepositoryReaderAll = new RestRepositoryReader()

  @Bean
  def restRepositoryReaderWithQuery = new RestRepositoryReader("""metaData=q='name=="ion mode" and value==negative'""")

}
