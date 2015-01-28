package moa.server

import moa.Compound
import moa.server.curation.CompoundCurationService

//@Transactional
class CompoundService {

    NameService nameService

    CompoundCurationService compoundCurationService
    /**
     * adds or updates this compound in the system
     * @param compound
     * @return
     */
    public Compound buildCompound(Map compound) {
        log.debug("trying to generate compound: ${compound.inchiKey} with ${compound.id}")

        //first get the compound we want
        Compound myCompound = null

        myCompound = Compound.findOrCreateByInchiKey(compound.inchiKey.trim())

        if (myCompound == null) {
            log.debug("compound not found -> adding it")
            myCompound = new Compound()
            myCompound.inchi = compound.inchi
            myCompound.inchiKey = compound.inchiKey
            myCompound.molFile = compound.molFile

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

        try {
            compoundCurationService.validate(myCompound.id)
        }
        catch (Exception e){
            //debugging enough not important errors normally
            log.debug(e.getMessage(),e)
        }
            return myCompound;

    }

}
