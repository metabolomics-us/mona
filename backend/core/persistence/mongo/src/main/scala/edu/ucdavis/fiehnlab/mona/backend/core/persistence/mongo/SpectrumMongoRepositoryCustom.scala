package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import java.util

import com.github.rutledgepaulv.qbuilders.visitors.MongoVisitor
import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import org.springframework.data.domain.{Pageable, Page}
import org.springframework.data.mongodb.core.query.{BasicQuery, Query}
import org.springframework.data.repository.NoRepositoryBean
import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 2/26/16.
  */
trait SpectrumMongoRepositoryCustom extends RSQLRepositoryCustom[Spectrum,Query]