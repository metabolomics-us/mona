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
        Compound myCompound = null

        myCompound = Compound.findOrCreateByInchiKey(compound.inchiKey.trim())

        if (myCompound == null) {
            log.debug("compound not found -> adding it")
            myCompound = Compound.deepClone(compound)

            log.info(" compound validation: ${myCompound.validate()} - ${myCompound.errors}")

            myCompound.save()
            log.debug("==> done: ${myCompound}")

        } else {
            log.debug("compound already existed")
        }


        myCompound.molFile = compound.molFile
        myCompound.inchi = compound.inchi

        myCompound.save()

        compound.names.each {
                nameService.addNameToCompound(it.name, myCompound)
        }

        log.info("compound valid: ${myCompound.validate()}")
        myCompound.save()
        return myCompound;

    }

}
