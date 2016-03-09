package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic

import java.util

import com.github.rutledgepaulv.qbuilders.visitors.{ElasticsearchVisitor, MongoVisitor}
import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.elasticsearch.index.query.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.{CriteriaQuery, NativeSearchQueryBuilder, NativeSearchQuery}
import org.springframework.data.mongodb.core.query.Query

/**
  * Created by wohlg_000 on 3/3/2016.
  */
class ISpectrumElasticRepositoryCustomImpl extends SpectrumElasticRepositoryCustom{

  @Autowired
  val elasticsearchTemplate:ElasticsearchTemplate = null

  /**
    * @param query
    * @return
    */
  override def nativeQuery(query: String): util.List[Spectrum] = ???

  /**
    *
    * @param query
    * @return
    *
    */
  override def nativeQuery(query: String, pageable: Pageable): Page[Spectrum] = ???

  /**
    * converts the RSQL String for us to a Query Object
    *
    * @param query
    * @return
    */
  override def buildRSQLQuery(query: String): String = ???

  /**
    * executes a query against the system and returns the count
    *
    * @param query
    * @return
    */
  override def nativeQueryCount(query: String): Long = ???
}
