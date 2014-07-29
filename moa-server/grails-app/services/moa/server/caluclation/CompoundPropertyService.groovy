package moa.server.caluclation
import grails.transaction.Transactional
import moa.Compound
import moa.server.metadata.MetaDataPersistenceService
import org.openscience.cdk.Molecule
import org.openscience.cdk.interfaces.IMolecularFormula
import org.openscience.cdk.io.MDLV2000Reader
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
        if (molFile.startsWith("\n") == false) {
            molFile = "\n" + molFile
        }

        def reader = new MDLV2000Reader(new StringReader(molFile))

        Molecule molecule = reader.read(new Molecule())


        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
        //AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
        def naturalMass = AtomContainerManipulator.getNaturalExactMass(molecule)

        IMolecularFormula moleculeFormula = MolecularFormulaManipulator
                .getMolecularFormula(molecule);


        //check for duplicates todo!!!

        //assigns our natural mass with this compound
        metaDataPersistenceService.generateMetaDataObject(compound, [name: "natural mass", value: naturalMass, category: "computed"])
        metaDataPersistenceService.generateMetaDataObject(compound, [name: "natural exact mass", value: MolecularFormulaManipulator.getNaturalExactMass(moleculeFormula), category: "computed"])
        metaDataPersistenceService.generateMetaDataObject(compound, [name: "total exact mass", value: MolecularFormulaManipulator.getTotalExactMass(moleculeFormula), category: "computed"])
        metaDataPersistenceService.generateMetaDataObject(compound, [name: "molecule formula", value: MolecularFormulaManipulator.getString(moleculeFormula), category: "computed"])

        compound.save()

    }
}
