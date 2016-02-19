package edu.ucdavis.fiehnlab.splash.resolver

/**
  * Created by wohlgemuth on 2/18/16.
  */

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http.{MediaTypes, HttpRequest}
import spray.json.{JsonFormat, DefaultJsonProtocol}
import spray.routing.SimpleRoutingApp
import spray.httpx.SprayJsonSupport._
import scala.collection.immutable.Nil
import scala.concurrent.Future
import MediaTypes._


import MonaJSONFormat._

/**
  * the actual server
  */
object Boot extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("my-system")

  val findSpectraForInchI = new FindSpectraForInchi(system)

  /**
    * server part
    */
  startServer(interface = "0.0.0.0", port = 8080) {

    get {

      pathSingleSlash {
        respondWithMediaType(`text/html`)
        complete(index)
      } ~
        path("rest" / "spectra" / Segment) { value =>
          complete(findSpectraForInchI.resolve(value))
        }
    }
  }

  lazy val index =
    <html>
      <head>
        <title>MoNA InChI Resolver</title>
      </head>

      <body>
        <h3>Welcome</h3>
        <p>This tool is a simple MoNA spectra resolver and exspects you to request it the following way</p>
        <p>
          http:/servicehost/rest/spectra/INCHI-KEY
        </p>
        <p>The response will be the following, a JSON array, with one entry for each row. This entry will contain the properties</p>
        <ol>
          <li>
            InChI Key
          </li>
          <li>
            Splash
          </li>
          <li>
            Spectra
          </li>
        </ol>

      </body>
    </html>

}
