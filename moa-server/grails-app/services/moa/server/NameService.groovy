package moa.server

import grails.transaction.Transactional
import moa.Compound
import moa.Name

@Transactional
class NameService {

    def addNameToCompound(String name, Compound compound, boolean computed = false, String source = "user provided") {

        log.debug("checking if compound ${compound.inchiKey} has name ${name}")
        Name n = Name.findOrCreateByNameAndCompound(name, compound)

        n.computed = computed
        n.source = source

        if(!n.validate()) {
            log.error(n.errors)
        }
        n.save(flush:true)


    }
}
