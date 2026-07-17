package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service

import org.scalatest.flatspec.AnyFlatSpec
import software.amazon.awssdk.services.cloudwatchlogs.model._

/**
  * Unit tests for the CloudWatch paging/filtering logic in CloudWatchLogsService. The real AWS
  * client is swapped out via the protected filterLogEvents/getLogEvents seams so these run without
  * a live CloudWatch connection
  */
class CloudWatchLogsServiceSpec extends AnyFlatSpec {

  private def filteredEvent(timestamp: Long, message: String): FilteredLogEvent =
    FilteredLogEvent.builder().timestamp(timestamp).message(message).build()

  private def outputEvent(timestamp: Long, message: String): OutputLogEvent =
    OutputLogEvent.builder().timestamp(timestamp).message(message).build()

  /**
    * Stub service driven by a queue of canned filterLogEvents responses (one per page) and a
    * single canned getLogEvents response
    */
  private class TestCloudWatchLogsService(filterPages: Seq[FilterLogEventsResponse] = Seq.empty,
                                           getEventsResponse: GetLogEventsResponse = GetLogEventsResponse.builder().events(java.util.Collections.emptyList[OutputLogEvent]()).build())
    extends CloudWatchLogsService {
    private val pages = scala.collection.mutable.Queue(filterPages: _*)
    var filterRequests: Seq[FilterLogEventsRequest] = Seq.empty

    override protected def logGroup: String = "test-log-group"

    override protected def filterLogEvents(request: FilterLogEventsRequest): FilterLogEventsResponse = {
      filterRequests :+= request
      if (pages.isEmpty) throw ResourceNotFoundException.builder().message("no such stream").build()
      pages.dequeue()
    }

    override protected def getLogEvents(request: GetLogEventsRequest): GetLogEventsResponse = getEventsResponse
  }

  "fetchErrorLogs" should "return no entries when the stream has no matching events" in {
    val page = FilterLogEventsResponse.builder().events(java.util.Collections.emptyList[FilteredLogEvent]()).build()
    val service = new TestCloudWatchLogsService(Seq(page))

    val result = service.fetchErrorLogs("some-stream", 0L)

    assert(result.exists)
    assert(result.entries.isEmpty)
    assert(!result.truncated)
  }

  it should "sort entries newest first" in {
    val page = FilterLogEventsResponse.builder().events(
      filteredEvent(100L, "first"), filteredEvent(300L, "third"), filteredEvent(200L, "second")
    ).build()
    val service = new TestCloudWatchLogsService(Seq(page))

    val result = service.fetchErrorLogs("some-stream", 0L)

    assert(result.entries.map(_.timestamp) == Seq(300L, 200L, 100L))
  }

  it should "follow nextToken across pages and combine their events" in {
    val page1 = FilterLogEventsResponse.builder()
      .events(filteredEvent(1L, "a")).nextToken("token-1").build()
    val page2 = FilterLogEventsResponse.builder()
      .events(filteredEvent(2L, "b")).build()
    val service = new TestCloudWatchLogsService(Seq(page1, page2))

    val result = service.fetchErrorLogs("some-stream", 0L)

    assert(result.entries.map(_.timestamp).toSet == Set(1L, 2L))
    assert(!result.truncated)
    assert(service.filterRequests.length == 2)
    assert(service.filterRequests(1).nextToken() == "token-1")
  }

  it should "cap accumulated events at 500 and mark the result truncated once more pages remain" in {
    val firstPageEvents = (1 to 500).map(i => filteredEvent(i.toLong, s"event-$i"))
    val page1 = FilterLogEventsResponse.builder().events(firstPageEvents: _*).nextToken("token-1").build()
    val page2 = FilterLogEventsResponse.builder().events(filteredEvent(501L, "one-too-many")).build()
    val service = new TestCloudWatchLogsService(Seq(page1, page2))

    val result = service.fetchErrorLogs("some-stream", 0L)

    assert(result.entries.length == 500)
    assert(result.truncated)
  }

  it should "keep the 500 newest events, not the 500 oldest, when a stream has more than 500 errors" in {
    // FilterLogEvents pages oldest-first with no reverse option: page 1 holds the oldest
    // 500 errors (timestamps 1-500), page 2 holds 200 more recent ones (timestamps 501-700).
    // The 500 newest overall are timestamps 201-700, so the oldest 200 of page 1 must be
    // dropped once the newer events from page 2 arrive.
    val page1Events = (1 to 500).map(i => filteredEvent(i.toLong, s"event-$i"))
    val page2Events = (501 to 700).map(i => filteredEvent(i.toLong, s"event-$i"))
    val page1 = FilterLogEventsResponse.builder().events(page1Events: _*).nextToken("token-1").build()
    val page2 = FilterLogEventsResponse.builder().events(page2Events: _*).build()
    val service = new TestCloudWatchLogsService(Seq(page1, page2))

    val result = service.fetchErrorLogs("some-stream", 0L)

    assert(result.entries.length == 500)
    assert(result.truncated)
    assert(result.entries.map(_.timestamp).max == 700L)
    assert(result.entries.map(_.timestamp).min == 201L)
    assert(!result.entries.exists(_.timestamp < 201L), "the oldest events from page 1 should have been dropped in favor of the newer events from page 2")
  }

  it should "fetch every page until nextToken is exhausted, even after exceeding the cap, so later pages can still evict older events" in {
    val page1Events = (1 to 500).map(i => filteredEvent(i.toLong, s"event-$i"))
    val page2Events = (501 to 900).map(i => filteredEvent(i.toLong, s"event-$i"))
    val page3Events = (901 to 1000).map(i => filteredEvent(i.toLong, s"event-$i"))
    val page1 = FilterLogEventsResponse.builder().events(page1Events: _*).nextToken("token-1").build()
    val page2 = FilterLogEventsResponse.builder().events(page2Events: _*).nextToken("token-2").build()
    val page3 = FilterLogEventsResponse.builder().events(page3Events: _*).build()
    val service = new TestCloudWatchLogsService(Seq(page1, page2, page3))

    val result = service.fetchErrorLogs("some-stream", 0L)

    assert(service.filterRequests.length == 3, "should have paged through all three responses instead of stopping once the cap was first reached")
    assert(result.entries.length == 500)
    assert(result.entries.map(_.timestamp).toSet == (501 to 1000).toSet)
  }

  it should "treat a missing stream as no logs rather than an error" in {
    val service = new TestCloudWatchLogsService(filterPages = Seq.empty)

    val result = service.fetchErrorLogs("missing-stream", 0L)

    assert(!result.exists)
    assert(result.entries.isEmpty)
  }

  "fetchLogContext" should "return the continuation lines following the anchor entry" in {
    val response = GetLogEventsResponse.builder().events(
      outputEvent(1000L, "2026-07-16 10:00:00.000 ERROR something broke"),
      outputEvent(1001L, "  at com.example.Foo.bar(Foo.scala:10)"),
      outputEvent(1002L, "  at com.example.Foo.baz(Foo.scala:20)")
    ).build()
    val service = new TestCloudWatchLogsService(getEventsResponse = response)

    val trace = service.fetchLogContext("some-stream", 1000L)

    assert(trace == "  at com.example.Foo.bar(Foo.scala:10)\n  at com.example.Foo.baz(Foo.scala:20)")
  }

  it should "stop at the next timestamped log line" in {
    val response = GetLogEventsResponse.builder().events(
      outputEvent(1000L, "2026-07-16 10:00:00.000 ERROR something broke"),
      outputEvent(1001L, "  at com.example.Foo.bar(Foo.scala:10)"),
      outputEvent(1002L, "2026-07-16 10:00:00.100 INFO unrelated next line")
    ).build()
    val service = new TestCloudWatchLogsService(getEventsResponse = response)

    val trace = service.fetchLogContext("some-stream", 1000L)

    assert(trace == "  at com.example.Foo.bar(Foo.scala:10)")
  }

  it should "return an empty string when the anchor entry has no continuation lines" in {
    val response = GetLogEventsResponse.builder().events(
      outputEvent(1000L, "2026-07-16 10:00:00.000 ERROR something broke")
    ).build()
    val service = new TestCloudWatchLogsService(getEventsResponse = response)

    val trace = service.fetchLogContext("some-stream", 1000L)

    assert(trace == "")
  }
}
