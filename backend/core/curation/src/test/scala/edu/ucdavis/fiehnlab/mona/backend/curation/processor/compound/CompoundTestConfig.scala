package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire.ClassyfireProcessor
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts.FetchCTSCompoundData
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}

@Configuration
@ComponentScan(Array("edu/ucdavis/fiehnlab/mona/backend/curation/processor"))
class CompoundTestConfig {

  @Bean
  def classyfireProcessor: ClassyfireProcessor = new ClassyfireProcessor

  @Bean
  def ctsProcessor: FetchCTSCompoundData = new FetchCTSCompoundData
}