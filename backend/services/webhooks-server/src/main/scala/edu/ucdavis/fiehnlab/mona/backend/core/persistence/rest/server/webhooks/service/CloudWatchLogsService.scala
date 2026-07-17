package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service

import com.typesafe.scalalogging.LazyLogging
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient
import software.amazon.awssdk.services.cloudwatchlogs.model.{FilterLogEventsRequest, FilterLogEventsResponse, GetLogEventsRequest, GetLogEventsResponse, ResourceNotFoundException}

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._

case class LogEntry(timestamp: Long, message: String)
case class LogQueryResult(exists: Boolean, entries: Seq[LogEntry], truncated: Boolean = false)

/**
  * reads recent error log lines per service out of CloudWatch Logs, so admins can
  * see what's going wrong across containers without opening the AWS console
  */
@Service
class CloudWatchLogsService extends LazyLogging {

  // maps a friendly service key (matching the docker-compose service block name)
  // to the static awslogs-stream name that service's container logs to
  val serviceStreams: Map[String, String] = Map(
    "postgresql" -> "postgres",
    "rabbitmq" -> "rabbitmq",
    "nginx" -> "nginx",
    "discovery" -> "discovery-service",
    "config-server" -> "config-service",
    "bootstrap" -> "bootstrap-service",
    "webhooks" -> "webhooks-service",
    "curationScheduler" -> "curation-scheduler-service",
    "persistence" -> "persistence-service",
    "statistics" -> "statistics-service",
    "auth" -> "auth-service",
    "similarity" -> "similarity-service",
    "proxy" -> "proxy-service",
    "downloader" -> "downloader-service",
    "curationRunner" -> "curation-runner-service"
  )

  private lazy val client: CloudWatchLogsClient = CloudWatchLogsClient.builder()
    .region(Region.US_WEST_2)
    .credentialsProvider(StaticCredentialsProvider.create(
      AwsBasicCredentials.create(
        System.getenv("MONA_AWS_ACCESS_KEY_ID"),
        System.getenv("MONA_AWS_SECRET_ACCESS_KEY")
      )
    ))
    .build()

  protected def logGroup: String = System.getenv("MONA_DIAGNOSTICS_LOG_GROUP")

  // seams overridden by tests to stand in for the real AWS calls, so the paging/filtering
  // logic below can be exercised without a live CloudWatch client
  protected def filterLogEvents(request: FilterLogEventsRequest): FilterLogEventsResponse = client.filterLogEvents(request)

  protected def getLogEvents(request: GetLogEventsRequest): GetLogEventsResponse = client.getLogEvents(request)

  // hard cap on how many matching events a single request will page through
  private val maxEntries = 500

  private val logLineTimestampPattern = """^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}""".r

  /**
    * fetches recent ERROR-level log lines for the given stream, newest first, capped at
    * [[maxEntries]] events
    *
    * @param streamName the awslogs-stream name to query
    * @param sinceMillis only return events at or after this epoch millis timestamp
    */
  def fetchErrorLogs(streamName: String, sinceMillis: Long): LogQueryResult = {
    logger.debug(s"fetching error logs for stream $streamName in group $logGroup since $sinceMillis")

    try {
      val (events, truncated) = fetchErrorLogPages(streamName, sinceMillis)
      val entries = events
        .map(event => LogEntry(event.timestamp(), event.message()))
        .sortBy(-_.timestamp)
      LogQueryResult(exists = true, entries, truncated)
    } catch {
      case _: ResourceNotFoundException =>
        // the stream (or the group) doesn't exist in this environment, e.g. a service
        // that only runs in prod (nginx) being queried against the dev log group
        logger.info(s"log stream $streamName not found in group $logGroup, treating as no logs")
        LogQueryResult(exists = false, Seq.empty)
    }
  }

  @tailrec
  private def fetchErrorLogPages(streamName: String, sinceMillis: Long,
                                  nextToken: Option[String] = None,
                                  accumulated: Seq[software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent] = Seq.empty,
                                  truncated: Boolean = false
                                 ): (Seq[software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent], Boolean) = {
    val requestBuilder = FilterLogEventsRequest.builder()
      .logGroupName(logGroup)
      .logStreamNames(streamName)
      .filterPattern("ERROR")
      .startTime(sinceMillis)
    nextToken.foreach(requestBuilder.nextToken)

    val response = filterLogEvents(requestBuilder.build())
    val events = accumulated ++ response.events().asScala.toSeq

    // FilterLogEvents always returns matching events oldest-first, this ensures we get the newest maxEntries instead of oldest
    val windowed = if (events.size > maxEntries) events.takeRight(maxEntries) else events
    val stillTruncated = truncated || events.size > maxEntries

    Option(response.nextToken()) match {
      case Some(token) => fetchErrorLogPages(streamName, sinceMillis, Some(token), windowed, stillTruncated)
      case None => (windowed, stillTruncated)
    }
  }

  /**
    * best-effort fetch of the lines immediately following an error entry that don't start
    * with their own timestamp - i.e. the stack trace/"Caused by:" continuation lines a
    * multi-line exception log produces, since each line is ingested as its own CloudWatch
    * event rather than one combined event
    *
    * @param streamName     the awslogs-stream name to query
    * @param timestampMillis the timestamp of the anchor error entry, as returned by [[fetchErrorLogs]]
    */
  def fetchLogContext(streamName: String, timestampMillis: Long): String = {
    val request = GetLogEventsRequest.builder()
      .logGroupName(logGroup)
      .logStreamName(streamName)
      .startTime(timestampMillis)
      .endTime(timestampMillis + 5000)
      .startFromHead(true)
      .limit(100)
      .build()

    val events = getLogEvents(request).events().asScala.toSeq.sortBy(_.timestamp())

    // the first event at the anchor timestamp is the error line itself, which the caller
    // already has - only return the continuation lines that follow it
    events.drop(1)
      .takeWhile(e => logLineTimestampPattern.findFirstIn(e.message()).isEmpty)
      .map(_.message())
      .mkString("\n")
  }
}
