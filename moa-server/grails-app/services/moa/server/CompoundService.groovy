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
import grails.transaction.Transactional
import util.chemical.MolHelper


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

        //new mona format has inchi key optional
        if(compound.inchiKey == null){
            if(compound.inchi == null){
                throw new exception.ValidationException("sorry you need to provide an InChI or an InChI Key for a compound!")
            }
            else{
                // Fix for InChIs without the InChI= prefix
                if (compound.inchi.indexOf("InChI=") != 0) {
                    compound.inchi = "InChI="+ compound.inchi
                }

                compound.inchiKey = MolHelper.newInstance().convertToInChIKey(compound.inchi.trim())
            }
        }
        myCompound = Compound.findByInchiKey(compound.inchiKey.trim()/*, [lock: true]*/)

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
                log.debug("generating inchi code from mol file...")

                myCompound.inchi = MolHelper.newInstance().convertToInChIKey(MolHelper.newInstance().readMolecule(myCompound))

            }
        } else if (myCompound.inchi != null) {
            log.debug("no molFile provided, need to generated one!")
            myCompound.molFile = generateMol(compound.inchi)
            log.debug("generated: ${myCompound.molFile}")
        } else {
            //give up and toss an exception in the next step
        }

        if (myCompound.validate()) {

            Compound existing = Compound.findByInchiKey(myCompound.inchiKey/*, [lock: true]*/)

            if (existing == null) {
                myCompound.save(flush:true)
            }
            else{
                log.info("compound colission between ${existing.inchiKey} and ${myCompound.inchiKey}, merging properties!")

                if(existing.molFile == null && myCompound.molFile != null){
                    existing.molFile = myCompound.molFile
                }
                if(existing.inchi == null && myCompound.inchi !=null){
                    existing.inchi = myCompound.inchi
                }
                existing.save()
                myCompound = existing

            }
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
