package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service

import java.nio.file.{Files, Paths}
import java.util.{Date, UUID}
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.{PredefinedQuery, QueryExport}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.StatisticsTagRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryRepository, QueryExportRepository}
import com.rabbitmq.client.Channel
import org.springframework.amqp.rabbit.core.{ChannelCallback, RabbitTemplate}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.jdk.CollectionConverters._
import scala.collection.mutable.ArrayBuffer


/**
  * Created by sajjan on 6/6/16.
  */
@Service
class DownloadSchedulerService extends LazyLogging {

  @Autowired
  @Qualifier("spectra-download-queue")
  val exportQueueName: String = null

  @Autowired
  @Qualifier("spectra-predefined-download-queue")
  val predefinedQueueName: String = null

  @Autowired
  val queryExportRepository: QueryExportRepository = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryRepository = null

  @Autowired
  private val statisticsTagRepository: StatisticsTagRepository = null

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  @Autowired
  val notifications: EventBus[Notification] = null

  // Same download directory the export writers use, so orphaned export files can be removed by name
  @Value("${mona.downloads:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_downloads")
  val exportDir: String = null


  /**
    * Sends a query to be scheduled for export to our dedicated queue
    *
    * @param query
    * @param format
    */
  def scheduleExport(query: String, format: String): QueryExport = {
    logger.info(s"Scheduling query $query as $format")
    val download: QueryExport = new QueryExport(UUID.randomUUID.toString, null, query, format, null, new Date, 0, 0, null, null)

    rabbitTemplate.convertAndSend(exportQueueName, download)
    notifications.sendEvent(Event(Notification(download, getClass.getName)))

    download
  }

  /**
    * Schedules an existing query export to be re-exported
    *
    * @param id
    * @return
    */
  def scheduleExport(id: String): QueryExport = {
    logger.info(s"Looking up query: $id")
    val download: QueryExport = queryExportRepository.findById(id).get()

    if (download != null) {
      logger.info(s"Rescheduling query: $id")
      rabbitTemplate.convertAndSend(exportQueueName, download)
      notifications.sendEvent(Event(Notification(download, getClass.getName)))
    }

    download
  }

  /**
    * Number of predefined export jobs still waiting in the queue. With prefetch set to 1 on the
    * downloader, unstarted jobs remain as ready messages, so a non-zero count means a regeneration
    * is still in progress. Returns 0 if the queue cannot be inspected so we never block legitimately
    */
  def pendingPredefinedMessages(): Int = {
    try {
      val count: Integer = rabbitTemplate.execute(new ChannelCallback[Integer] {
        override def doInRabbit(channel: Channel): Integer =
          channel.queueDeclarePassive(predefinedQueueName).getMessageCount
      })

      if (count == null) 0 else count.intValue()
    } catch {
      case e: Exception =>
        logger.warn(s"Could not inspect predefined download queue depth: ${e.getMessage}")
        0
    }
  }

  /**
    * True while a predefined export regeneration is still draining the queue
    */
  def isPredefinedExportInProgress: Boolean = pendingPredefinedMessages() > 0

  /**
    * The label prefix shared by every predefined library download
    */
  private val libraryLabelPrefix: String = "Libraries - "

  /**
    * Builds the set of library download labels justified by the libraries currently in the database.
    * Mirrors the creation logic in generatePredefinedExports (a label for every hierarchy level of
    * each library tag) so that reconciliation and creation never disagree and thrash
    */
  private def expectedLibraryLabels(): Set[String] = {
    statisticsTagRepository.findAll().asScala
      .filter(_.getCategory == "library")
      .flatMap { tag =>
        val components: Array[String] = tag.getText.split(" - ")
        (1 to components.length).map(i => s"$libraryLabelPrefix${components.slice(0, i).mkString(" - ")}")
      }.toSet
  }

  /**
    * Best-effort removal of the export files backing a predefined query. Filenames are taken straight
    * from the stored export rows so nothing is guessed, and failures are logged rather than thrown
    */
  private def deleteExportFiles(query: PredefinedQuery): Unit = {
    val exports: Array[QueryExport] = Array(query.getJsonExport, query.getMspExport, query.getSdfExport).filter(_ != null)
    val files: Array[String] = exports.flatMap(e => Array(e.getExportFile, e.getQueryFile)).filter(f => f != null && f.nonEmpty)

    files.distinct.foreach { file =>
      try {
        Files.deleteIfExists(Paths.get(exportDir, file))
      } catch {
        case e: Exception => logger.warn(s"Could not delete export file $file: ${e.getMessage}")
      }
    }
  }

  /**
    * Removes auto-generated library downloads whose library no longer exists. Only entries that follow
    * the generated "Libraries - X" label with a matching "tags.text:'X'" query are considered, leaving
    * hand-curated bootstrap library exports untouched. Returns the entries that were removed
    */
  private def pruneOrphanedLibraryQueries(): Array[PredefinedQuery] = {
    val expected: Set[String] = expectedLibraryLabels()

    // Never prune when there are no live library tags to compare against. An empty set almost always
    // means statistics have not been built yet rather than every library having been deleted, and
    // pruning against it would wipe every generated library download
    if (expected.isEmpty) {
      logger.warn("No library statistics tags present, skipping library reconciliation to avoid removing every download")
      return Array.empty[PredefinedQuery]
    }

    val orphaned: Array[PredefinedQuery] = predefinedQueryRepository.findAll().asScala
      .filter { query =>
        val label: String = query.getLabel
        label != null && label.startsWith(libraryLabelPrefix) &&
          query.getQuery == s"tags.text:'${label.substring(libraryLabelPrefix.length)}'" &&
          !expected.contains(label)
      }.toArray

    orphaned.foreach { query =>
      logger.info(s"Removing predefined download for deleted library: ${query.getLabel}")
      deleteExportFiles(query)
      // The cascade on PredefinedQuery removes the associated json/msp/sdf query_export rows
      predefinedQueryRepository.delete(query)
    }

    orphaned
  }

  /**
    * Reconciles the predefined library downloads against the libraries currently in the database,
    * removing any whose library has been deleted. Exposed for the library deletion flow so orphaned
    * downloads are pruned immediately instead of waiting for the next regeneration
    */
  def reconcilePredefinedLibraryQueries(): Array[PredefinedQuery] = {
    // Skip while a regeneration is draining, the in-flight listener re-saves each predefined query as
    // it finishes an export and would otherwise resurrect anything pruned here
    if (pendingPredefinedMessages() > 0) {
      logger.info("Predefined export in progress, skipping library reconciliation")
      Array.empty[PredefinedQuery]
    } else {
      pruneOrphanedLibraryQueries()
    }
  }

  /**
    * Generates the downloads of all export formats for each predefined query download
    */
  def generatePredefinedExports(): Array[PredefinedQuery] = {

    // Skip if a previous regeneration is still draining the queue, which also guards the cron trigger
    if (pendingPredefinedMessages() > 0) {
      logger.info("Predefined export already in progress, skipping regeneration")
      Array.empty[PredefinedQuery]
    } else {

    // Drop downloads for libraries that no longer exist so we never re-export deleted libraries
    pruneOrphanedLibraryQueries()

    // Update the list of pre-generated downloads based on libraries present in the database
    statisticsTagRepository.findAll().asScala
      .filter(_.getCategory == "library")
      .foreach { tag =>
        val tagComponents: Array[String] = tag.getText.split(" - ")

        // Create each level of the tag if it contains separators
        // For example, a library tag of "Test - A" would create libraries "Test" and "Test - A"
        (1 to tagComponents.length).foreach { i =>
          val tagLabel: String = tagComponents.slice(0, i).mkString(" - ")

          if (predefinedQueryRepository.findByQuery(s"tags.text:'$tagLabel'").isEmpty) {
            logger.info(s"Creating new predefined download for ${tag.getText}: $tagLabel")

            predefinedQueryRepository.save(new PredefinedQuery(s"Libraries - $tagLabel", tagLabel, s"tags.text:'$tagLabel'", 0, null, null, null))
          }
        }
      }

    // Predefined downloads to schedule
    predefinedQueryRepository.findAll().asScala.toArray.map { predefinedQuery: PredefinedQuery =>
      rabbitTemplate.convertAndSend(predefinedQueueName, predefinedQuery)
      notifications.sendEvent(Event(Notification(predefinedQuery, getClass.getName)))
      predefinedQuery
    }
    }
  }

  /**
    * Schedules the generation of predefined exports once a day
    */
  @Scheduled(cron = "0 0 0 ? * SAT")
  private def schedulePredefinedExports(): Unit = {
    logger.info("Scheduling predefined export generation")
    generatePredefinedExports()
  }
}
