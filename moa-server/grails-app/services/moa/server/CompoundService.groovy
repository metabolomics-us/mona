package moa.server

import grails.converters.JSON
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import moa.Compound
import net.sf.jniinchi.INCHI_RET
import org.openscience.cdk.AtomContainer
import org.openscience.cdk.DefaultChemObjectBuilder
import org.openscience.cdk.Molecule
import org.openscience.cdk.MoleculeSet
import org.openscience.cdk.exception.CDKException
import org.openscience.cdk.graph.ConnectivityChecker
import org.openscience.cdk.inchi.InChIGeneratorFactory
import org.openscience.cdk.inchi.InChIToStructure
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IMolecule
import org.openscience.cdk.io.MDLV2000Writer
import org.openscience.cdk.layout.StructureDiagramGenerator
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.springframework.transaction.annotation.Transactional

import java.util.concurrent.atomic.AtomicBoolean

@Transactional
class CompoundService {

    NameService nameService

    /**
     * adds or updates this compound in the system
     * @param compound
     * @return
     */
    public Compound buildCompound(Map compound) {
        log.debug("trying to generate compound: ${compound.inchiKey}")

        //first get the compound we want
        Compound myCompound = null

        myCompound = Compound.findOrCreateByInchiKey(compound.inchiKey.trim())

        if (myCompound == null) {
            log.debug("compound not found -> adding it")
            myCompound = new Compound()
            myCompound.inchiKey = compound.inchiKey

        } else {
            log.debug("compound already existed")
        }

        myCompound.inchi = compound.inchi

        //validate the mol file or try to generate it
        if (compound.molFile != null && compound.molFile.toString().size() > 0) {
            log.debug("molFile was provided!\n ${compound.molFile}")
            myCompound.molFile = compound.molFile.trim()

            if (compound.inchi == null) {
                log.debug("generating inchi code from mold file...")

            }
        } else if (myCompound.inchi != null) {
            log.debug("no molFile provided, need to generated one!")
            myCompound.molFile = generateMol(compound.inchi)
            log.debug("generated: ${myCompound.molFile}")
        } else {
            //give up and toss an exception in the next step
        }

        if (myCompound.validate()) {
            myCompound.save(flush: true)
        } else {
            throw new ValidationException("sorry this compound is not valid", myCompound.errors)
        }


        compound.names.each {
            if (it instanceof String) {
                nameService.addNameToCompound(it, myCompound)
            } else if (it instanceof Map) {
                if (it.name != null) {
                    nameService.addNameToCompound(it.name, myCompound)
                }
            } else {
                log.error("unsupported name object! Ignoring it ${it}:${it.class}")
            }
        }

        myCompound.save(flush: true)

        log.debug("added or updated compound with id: ${myCompound.id}")
        //CompoundCurationJob.triggerNow(compoundId: myCompound.id)
        return myCompound;

    }

    private String generateMol(String inchi) {

        log.debug("creating mol file for inchi: ${inchi}")
        InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance()
        InChIToStructure structureGen = factory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance())

        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(structureGen.atomContainer)

        def INCHI_RET ret = structureGen.getReturnStatus();
        if (ret == INCHI_RET.WARNING) {
            // Structure generated, but with warning message
            log.warn("InChI warning: ${structureGen.getMessage()}")
        } else if (ret != INCHI_RET.OKAY) {
            // Structure generation failed
            throw new CDKException("Structure generation failed failed: ${ret.toString()}\n[ ${structureGen.getMessage()}\t${structureGen.warningFlags} ]")
        }


        IAtomContainer molecule = new Molecule(structureGen.atomContainer)
        //sdg.setUseTemplates(true);

        MoleculeSet set = ConnectivityChecker.partitionIntoMolecules(molecule)

        if (set.getMoleculeCount() == 1) {
            StructureDiagramGenerator sdg = new StructureDiagramGenerator();

            sdg.setMolecule(molecule, true)
            sdg.generateCoordinates();
            molecule = sdg.getMolecule()

        } else {
            log.warn("disconnected molecule might not look nice!")

            Iterator<IAtomContainer> iterator = set.molecules().iterator()

            AtomContainer result = new AtomContainer()
            while (iterator.hasNext()) {
                StructureDiagramGenerator sdg = new StructureDiagramGenerator();

                sdg.setMolecule(iterator.next(), true)
                sdg.generateCoordinates();

                result.add(sdg.getMolecule())


            }

            molecule = result
        }

        //ModelBuilder3D builder = ModelBuilder3D.getInstance()
        //molecule = builder.generate3DCoordinates(molecule, false)

        StringWriter stringWriter = new StringWriter()
        MDLV2000Writer writer = new MDLV2000Writer(stringWriter)

        writer.writeMolecule(molecule)
        writer.close()

        String molFile = stringWriter.buffer.toString()
        stringWriter.close()

        return molFile

    }

}
