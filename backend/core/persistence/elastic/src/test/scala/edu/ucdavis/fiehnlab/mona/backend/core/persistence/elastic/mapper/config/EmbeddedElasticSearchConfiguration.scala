package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config.ElasticsearchConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(Array(classOf[ElasticsearchConfig]))
class TestConfig{

}