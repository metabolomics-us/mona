package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum

/**
  * specific client to work with MoNA spectrums and there MetaData
  */
class MonaSpectrumRestClient(val server:String) extends GenericRestClient[Spectrum,String](s"rest/spectrum"){

}
