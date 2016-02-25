package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.{Query, BasicQuery}
import org.springframework.stereotype.Service

/**
  * Created by wohlg_000 on 2/24/2016.
  */
@Service
class SpectrumService @Autowired()(mongoTemplate: MongoTemplate) {

  def save(spectrum: Spectrum) = {
    mongoTemplate.save(spectrum)
  }

  def delete(spectrum: Spectrum) = {
    mongoTemplate.remove(spectrum)
  }

  def get(key: String): Spectrum = {
    mongoTemplate.findById(key, classOf[Spectrum])
  }

  def query(query: Query): java.util.List[Spectrum] = {
    mongoTemplate.find(query, classOf[Spectrum])
  }
}
