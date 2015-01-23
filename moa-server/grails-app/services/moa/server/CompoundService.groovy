package moa.server

import moa.Compound
//@Transactional
class CompoundService {

    NameService nameService
    /**
     * adds or updates this compound in the system
     * @param compound
     * @return
     */
    public Compound buildCompound(Compound compound) {
        log.debug("trying to generate compound: ${compound.inchiKey} with ${compound.id}")

        //first get the compound we want
        Compound myCompound = new Compound()

        def names = compound.names


        def myNames = []
        //merge new names
        names.each { name ->
            myNames.add(name.name)
        }

        compound.names = []

        myCompound = Compound.findOrCreateByInchiKey(compound.inchiKey.trim())

        if (myCompound == null) {
            log.debug("compound not found -> adding it")
            myCompound = new Compound(inchiKey: compound.inchiKey)

            log.info(" compound validation: ${myCompound.validate()} - ${myCompound.errors}")

            myCompound.save()
            log.debug("==> done: ${myCompound}")

        } else {
            log.debug("compound already existed")
        }


        myCompound.molFile = compound.molFile
        myCompound.inchi = compound.inchi

        myCompound.save(flush: true)


        myNames.each {
            nameService.addNameToCompound(it, myCompound)
        }

        myCompound.save(flush: true)
        return myCompound;

    }

}
