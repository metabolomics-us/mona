package edu.ucdavis.fiehnlab.mona.core.similarity.config

import java.util.concurrent.ConcurrentHashMap

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.core.similarity.index.cache.{MapBasedSpectrumCache, SpectrumCache}
import edu.ucdavis.fiehnlab.mona.core.similarity.math.binning.{BinByRoundingMethod, BinningMethod}
import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * Created by sajjan on 12/29/16.
  */
@Configuration
class SimilarityConfig extends LazyLogging {

  @Bean
  def binningMethod: BinningMethod = new BinByRoundingMethod

  @Bean
  def spectrumCache: SpectrumCache = new MapBasedSpectrumCache(new ConcurrentHashMap[String, SimpleSpectrum](1000000, 0.7f))
}
