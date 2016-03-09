package edu.ucdavis.fiehnlab.splash.resolver

import spray.json.{JsonFormat, DefaultJsonProtocol}


case class SpectraRetrievedResult(val inchiKey: String, val splash: String, val spectrum: String)

case class ArrayResult[T](val spectra: List[T])

/**
  * definition of the different mona objects
  */
object MonaJSONFormat extends DefaultJsonProtocol {

  implicit val metadataFormat = jsonFormat15(MetaData)
  implicit val namesFormat = jsonFormat5(Names)
  implicit val tagsFormat = jsonFormat2(Tags)
  implicit val compoundFormat = jsonFormat8(Compound)
  implicit val impactForma = jsonFormat2(Impacts)
  implicit val scoreFormat = jsonFormat4(Score)
  implicit val splashFormat = jsonFormat5(Splash)
  implicit val authorityFormat = jsonFormat1(Authorities)
  implicit val submitterFormat = jsonFormat10(Submitter)
  implicit val spectrumFormat = jsonFormat13(Spectrum)

  implicit val spectraResultFormat = jsonFormat3(SpectraRetrievedResult)

  implicit def spectraResultFormatArray[T: JsonFormat] = jsonFormat1(ArrayResult.apply[T])

}