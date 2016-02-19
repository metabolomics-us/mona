package edu.ucdavis.fiehnlab.splash.resolver

import java.util.concurrent.TimeUnit
import java.util.logging.Logger

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http.HttpRequest
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.{Await, Future}
import MonaJSONFormat._
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http.{MediaTypes, HttpRequest}
import spray.routing.SimpleRoutingApp
import spray.httpx.SprayJsonSupport._
import scala.collection.immutable.Nil
import MediaTypes._
import scala.concurrent._
import scala.concurrent.duration._

import MonaJSONFormat._

import scala.concurrent.duration.Duration

/**
  * Created by wohlgemuth on 2/18/16.
  */

class FindSpectraForInchi(val system: ActorSystem) extends SprayJsonSupport with AdditionalFormats  {

  implicit val timeout = Timeout(6000, TimeUnit.MINUTES)

  import system.dispatcher

  val pipeline: HttpRequest => Future[List[Spectrum]] = sendReceive(system, system.dispatcher, timeout) ~> unmarshal[List[Spectrum]]

  /**
    * finds all the spectra for our given inchi key and formats them in the correct way for us
    *
    * @param inchiKey
    * @return
    */
  def resolve(inchiKey: String): List[SpectraRetrievedResult] = {


    val response: Future[List[Spectrum]] = pipeline(Post("http://mona.fiehnlab.ucdavis.edu/rest/spectra/search",
      s"""
         {"compound":{"inchiKey":{"eq":"${inchiKey}"}},"metadata":[],"tags":[]}
      """.asJson.asJsObject))


    val result = Await.result(response, 900 seconds).collect({
      case a: Spectrum =>
        new SpectraRetrievedResult(inchiKey, a.hash.get, a.spectrum.get)
    })

    result
  }
}
