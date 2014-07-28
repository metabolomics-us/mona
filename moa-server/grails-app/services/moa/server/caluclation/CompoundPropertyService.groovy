package moa.server.caluclation

import grails.transaction.Transactional
import moa.Compound
import moa.server.metadata.MetaDataPersistenceService
import org.openscience.cdk.Molecule
import org.openscience.cdk.io.MDLV2000Reader
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator

@Transactional
class CompoundPropertyService {

    MetaDataPersistenceService metaDataPersistenceService

    /**
     * calculates the chemical properties for this compound
     * @param compound
     */
    def calculateMetaData(Compound compound) {

        log.info("molecule:\n\n ${compound.molFile}")
        def reader = new MDLV2000Reader(new StringReader(compound.molFile))

        Molecule molecule = reader.read(new Molecule())

        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);

        //assigns our natural mass with this compound
        metaDataPersistenceService.generateMetaDataObject(compound, [name: "natural mass", value: AtomContainerManipulator.getNaturalExactMass(molecule), category: "computed"])

        compound.save()

    }
}
