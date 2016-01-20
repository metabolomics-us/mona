package moa.server

import grails.transaction.Transactional
import moa.Compound
import moa.CompoundLink
import moa.MetaDataValue
import moa.Spectrum
import moa.SupportsMetaData

@Transactional
class UpdateSpectrumTimestampService {

    def METADATA_IGNORE_FIELDS = [
            "validation date", "validation time", "scoring date", "scoring time"
    ]

    /**
     * update of the lastUpdated field in a Spectrum object
     */
    def updateTimestampByMetaData(MetaDataValue m) {
        if (!(m.getName() in METADATA_IGNORE_FIELDS)) {
            updateSpectrum(m.owner)
        }
    }

    def updateSpectrumTimestampsByCompound(Compound compound) {
        CompoundLink.findAllByCompound(compound).each {
            updateSpectrum(it.spectrum)
        }
    }

    private def updateSpectrum(SupportsMetaData s) {
        log.debug("Updating lastUpdated timestamp of $s")
        s.lastUpdated = new Date()
        s.save()
    }
}
