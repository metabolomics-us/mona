package moa.server.caluclation

import grails.transaction.Transactional
import moa.Compound
import moa.server.metadata.MetaDataPersistenceService
import org.openscience.cdk.DefaultChemObjectBuilder
import org.openscience.cdk.Molecule
import org.openscience.cdk.interfaces.IMolecularFormula
import org.openscience.cdk.io.MDLV2000Reader
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator

@Transactional
class CompoundPropertyService {

    MetaDataPersistenceService metaDataPersistenceService

    /**
     * calculates the chemical properties for this compound
     * @param compound
     */
    def calculateMetaData(Compound compound) {
        String molFile = compound.molFile

        try {
            log.debug("calculate properties for compound: ${compound}")
            if (molFile.startsWith("\n") == false) {
                molFile = "\n" + molFile
            }

            log.debug("mol file: ${molFile}")
            def reader = new MDLV2000Reader(new StringReader(molFile))

            Molecule molecule = reader.read(new Molecule())

            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
            CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.newInstance()).addImplicitHydrogens(molecule)
            AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);

            IMolecularFormula moleculeFormula = MolecularFormulaManipulator
                    .getMolecularFormula(molecule);


            log.debug("persisting properties")
            metaDataPersistenceService.generateMetaDataObject(compound, [name: "total exact mass", value: MolecularFormulaManipulator.getTotalExactMass(moleculeFormula), category: "computed", computed: true])
            metaDataPersistenceService.generateMetaDataObject(compound, [name: "molecule formula", value: MolecularFormulaManipulator.getString(moleculeFormula), category: "computed", computed: true])

        }
        catch (Exception e){
            log.warn("error in compound service...")
            log.warn(molFile)
            log.warn(e.getMessage(),e)
        }

        log.debug("done")
        compound.save()

    }
}
