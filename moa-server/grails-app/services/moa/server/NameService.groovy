package moa.server

import grails.transaction.Transactional
import moa.Compound
import moa.Name
import org.apache.xpath.operations.String

@Transactional
class NameService {

    def addNameToCompound(String name, Compound compound) {

        log.debug("checking if compound ${compound.inchiKey} has name ${name}")
        Name.findOrCreateByNameAndCompound(name, compound).save()


    }
}
