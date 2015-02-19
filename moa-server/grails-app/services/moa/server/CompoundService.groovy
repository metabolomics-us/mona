package moa.server

import moa.Compound
import net.sf.jniinchi.INCHI_RET
import org.openscience.cdk.DefaultChemObjectBuilder
import org.openscience.cdk.Molecule
import org.openscience.cdk.exception.CDKException
import org.openscience.cdk.inchi.InChIGeneratorFactory
import org.openscience.cdk.inchi.InChIToStructure
import org.openscience.cdk.interfaces.IMolecule
import org.openscience.cdk.io.MDLV2000Writer
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator

//@Transactional
class CompoundService {

    NameService nameService

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

            log.info(" compound validation: ${myCompound.validate()} - ${myCompound.errors}")

            myCompound.save()
            log.debug("==> done: ${myCompound}")

        } else {
            log.debug("compound already existed")
        }

        //validate the mol file

        if (compound.molFile != null && compound.molFile.toString().size() > 0) {
            myCompound.molFile = compound.molFile.trim()
        } else {
            myCompound.molFile = generateMol(compound.inchi)
        }

        myCompound.save()

        compound.names.each {
            nameService.addNameToCompound(it, myCompound)
        }

        log.info("compound valid: ${myCompound.validate()}")
        myCompound.save()

        CompoundCurationJob.triggerNow(compoundId: myCompound.id)
        return myCompound;

    }

    private String generateMol(String inchi) {

        InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance()
        InChIToStructure structureGen = factory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance())

        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(structureGen.atomContainer)

        IMolecule molecule = new Molecule(structureGen.atomContainer)

        def INCHI_RET ret = structureGen.getReturnStatus();
        if (ret == INCHI_RET.WARNING) {
            // Structure generated, but with warning message
            log.warn("InChI warning: ${structureGen.getMessage()}")
        } else if (ret != INCHI_RET.OKAY) {
            // Structure generation failed
            throw new CDKException("Structure generation failed failed: ${ret.toString()}\n[ ${structureGen.getMessage()}\t${structureGen.warningFlags} ]")
        }


        StringWriter stringWriter = new StringWriter()
        MDLV2000Writer writer = new MDLV2000Writer(stringWriter)

        writer.writeMolecule(molecule)
        writer.close()

        String molFile = stringWriter.buffer.toString()
        stringWriter.close()

        return molFile

    }

}
