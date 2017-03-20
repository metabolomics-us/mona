package edu.ucdavis.fiehnlab.mona.app.server.proxy.logging

import java.io.{File, InputStreamReader}
import java.net.InetAddress
import java.util.Date

import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.exception.AddressNotFoundException
import com.maxmind.geoip2.model.CityResponse
import com.maxmind.geoip2.record.Country
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.app.server.proxy.repository.LogMessageMongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.io.Source

/**
  * Created by sajjan on 12/20/16.
  */
@Service
class LoggingService extends LazyLogging {

  @Autowired
  val logMessageMongoRepository: LogMessageMongoRepository = null

  /**
    * Load the MaxMind GeoLite database
    * https://maxmind.github.io/GeoIP2-java/
    */
  private val geoLiteDb = new DatabaseReader.Builder(getClass.getResourceAsStream("/GeoLite2-City.mmdb")).build()


  def logRequest(httpStatus: Int, httpMethod: String, requestURI: String, requestQueryString: String,
                 postData: String, ipAddress: String, duration: Long): Unit = {

    // Start logging in background
    new Thread(new Runnable {
      override def run(): Unit = {
        // Get geolocation
        val response: CityResponse = getLocation(ipAddress)

        val country: String = if (response != null && response.getCountry != null) response.getCountry.getName else null
        val region: String = if (response != null && response.getMostSpecificSubdivision != null) response.getMostSpecificSubdivision.getName else null
        val city: String = if (response != null && response.getCity != null) response.getCity.getName else null

        // Create logging message
        val logMessage: LogMessage = LogMessage(
          null, httpStatus, httpMethod, requestURI, requestQueryString, postData,
          ipAddress, country, region, city,
          duration, new Date
        )

        logger.debug(logMessage.toString)
        logMessageMongoRepository.save(logMessage)
      }
    }).start()
  }

  /**
    *
    * @param ipAddress
    * @return
    */
  private def getLocation(ipAddress: String): CityResponse = {
    try {
      geoLiteDb.city(InetAddress.getByName(ipAddress.split(',')(0)))
    } catch {
      case e: AddressNotFoundException =>
        logger.debug(s"IP Address $ipAddress not in GeoLite2 database")
        null

      case e: Exception =>
        e.printStackTrace()
        null
    }
  }
}

