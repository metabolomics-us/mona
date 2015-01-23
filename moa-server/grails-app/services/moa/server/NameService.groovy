package moa.server

import grails.transaction.Transactional
import moa.Compound
import moa.Name

@Transactional
class NameService {

    def addNameToCompound(String name, Compound compound) {

        log.debug("checking if compound ${compound.inchiKey} has name ${name}")
        Name n = Name.findByNameAndCompound(name, compound)

        if (n != null) {
            log.debug("name was already attached to compound")
        } else {
            log.debug("adding name to compound")
            n = new Name(name: name, compound: compound)

            if (!n.validate()) {
                log.debug("${name} failed to validate ${n.errors}")
            }

            n.save(flush: true)

        }
    }
}
