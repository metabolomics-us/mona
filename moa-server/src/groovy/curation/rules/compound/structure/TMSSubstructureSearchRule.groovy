package curation.rules.compound.structure

import curation.AbstractCurationRule
import curation.CurationObject
import moa.Compound
import moa.Spectrum
import org.openscience.cdk.Molecule
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator

import static util.chemical.AdductBuilder.*

/**
 *
 * searches for TMS substructures for the given compound and tries to calculate the correct original biological compound out of it
 *
 * User: wohlgemuth
 * Date: 10/14/14
 * Time: 12:11 PM
 */
class TMSSubstructureSearchRule extends AbstractCurationRule {
    @Override
    boolean executeRule(CurationObject toValidate) {

        Spectrum spec = toValidate.getObjectAsSpectra()

        //we only work on the chemical compound
        Compound compound = spec.chemicalCompound

        Molecule mol = readMolecule(compound)

        //only continue if we got tms to begin with
        def structuresToTest = [makeTMSBoundToPhosphor(), makeTMSBoundToOxygen(), makeTMSBoundToNitrogen(), makeTMSBoundToSulfur()]

        def tms = makeTMS()

        double tmsMolareMass = MolecularFormulaManipulator.getTotalExactMass(calculateFormula(tms))

        boolean canContinue = UniversalIsomorphismTester.isSubgraph(mol, tms)

        //aslong as the structure has tms in it loop
        while (canContinue) {

            logger.info("searching for further TMS substructures...")

            structuresToTest.each { Molecule sub ->

                if (UniversalIsomorphismTester.isSubgraph(mol, sub)) {

                    logger.info("Current TMS group: ${MolecularFormulaManipulator.getString(calculateFormula(sub))}")
                    logger.info("=> molare mass: ${MolecularFormulaManipulator.getTotalExactMass(calculateFormula(sub))}")
                    logger.info("=> molare mass of tms: ${tmsMolareMass}")
                    logger.info("=> molare mass of compound: ${MolecularFormulaManipulator.getTotalExactMass(calculateFormula(mol))}")
                    logger.info("=> molare mass of compound without tms: ${MolecularFormulaManipulator.getTotalExactMass(calculateFormula(mol)) - tmsMolareMass}")


                    //TODO check against exact mass or ions in table to make sure they exist
                }
            }


            canContinue = UniversalIsomorphismTester.isSubgraph(mol, tms)
            canContinue = false
        }

        return true
    }

    /**
     * constructs a tms molecule
     * @return
     */
    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

}
