package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.CompoundClassStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.CompoundClassStatistics
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.mapreduce.MapReduceOptions
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 9/27/16.
  */
@Service
class CompoundClassStatisticsService {

  @Autowired
  private val mongoOperations: MongoOperations = null

  @Autowired
  @Qualifier("compoundClassStatisticsMongoRepository")
  private val compoundClassStatisticsRepository: CompoundClassStatisticsMongoRepository = null


  /**
    * Get all data in the compound class statistics repository
    * @return
    */
  def getCompoundClassStatistics: Iterable[CompoundClassStatistics] = compoundClassStatisticsRepository.findAll().asScala

  /**
    * Get data for the given compound class from the metadata statistics repository
    * @return
    */
  def getCompoundClassStatistics(compoundClass: String): CompoundClassStatistics = compoundClassStatisticsRepository.findOne(compoundClass)

  /**
    * Count the data in the compound class statistics repository
    * @return
    */
  def countCompoundClassStatistics: Long = compoundClassStatisticsRepository.count()


  /**
    * JavaScript map function for MapReduce operation
    */
  private val mapFunction: String =
    """function() {
      |    for (var i = 0; i < this.compound.length; i++) {
      |        // Find all InChIKey first blocks
      |        var inchikeys = this.compound[i].metaData
      |          .filter(function(x) { return x.name == "InChIKey"; })
      |          .map(function(x) { return x.value.slice(0, 14); });
      |
      |        // Continue if no InChIKey is found or no classification is present
      |        if (inchikeys.length == 0 || !("classification" in this.compound[i]) || this.compound[i].classification.length == 0)
      |            continue;
      |
      |        // Get compound classes
      |        var compoundClasses = {};
      |        var compoundClassString = [];
      |
      |        for (var j = 0; j < this.compound[i].classification.length; j++)
      |            compoundClasses[this.compound[i].classification[j].name] = this.compound[i].classification[j].value;
      |
      |        if ("kingdom" in compoundClasses && compoundClasses.kingdom != "Chemical entities")
      |            compoundClassString.push(compoundClasses.kingdom)
      |        if ("superclass" in compoundClasses)
      |            compoundClassString.push(compoundClasses.superclass)
      |        if ("class" in compoundClasses)
      |            compoundClassString.push(compoundClasses.class)
      |        if ("subclass" in compoundClasses)
      |            compoundClassString.push(compoundClasses.subclass)
      |
      |        // Emit each level of the compound class as the key and an object
      |        // consisting of the spectrum id and inchikeys as the value
      |        if (compoundClassString.length > 0) {
      |            for (var j = 0; j < compoundClassString.length; j++)
      |                emit(compoundClassString.slice(0, j + 1).join("|"), {spectra: [this._id], compounds: inchikeys});
      |        }
      |    }
      |};""".stripMargin

  /**
    * JavaScript reduce function for MapReduce operation
    */
  private val reduceFunction: String =
    """function(compoundClass, values) {
      |    var result = {spectra: [], compounds: []};
      |
      |    // Concatenate arrays containing spectrum ids and inchikeys
      |    for (var i = 0; i < values.length; i++) {
      |        result.spectra = result.spectra.concat(values[i].spectra);
      |        result.compounds = result.compounds.concat(values[i].compounds);
      |    }
      |
      |    return result;
      |};""".stripMargin

  /**
    * JavaScript finalize function for MapReduce operation
    */
  private val finalizeFunction: String =
    """function(compoundClass, reducedValue) {
      |    // Return the number of distinct spectrum ids and inchikeys
      |    reducedValue.spectra = new Set(reducedValue.spectra).size;
      |    reducedValue.compounds = new Set(reducedValue.compounds).size;
      |
      |    return reducedValue;
      |};""".stripMargin


  /**
    * Collect a list of compound class groups with spectrum and compound counts
    * @return
    */
  def updateCompoundClassStatistics(): Unit = {
    mongoOperations.mapReduce("SPECTRUM", mapFunction, reduceFunction,
      new MapReduceOptions().outputCollection("STATISTICS_COMPOUNDCLASS").finalizeFunction(finalizeFunction),
      classOf[CompoundClassAggregation])
      .asScala
      .foreach(x => compoundClassStatisticsRepository.save(CompoundClassStatistics(x._id, x.value.spectra, x.value.compounds)))
  }
}

private case class CompoundClassAggregation(_id: String, value: CompoundClassCount)
private case class CompoundClassCount(spectra: Int, compounds: Int)
