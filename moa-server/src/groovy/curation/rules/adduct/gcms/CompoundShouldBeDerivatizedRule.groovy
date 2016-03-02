package curation.rules.adduct.gcms
import curation.AbstractCurationRule
import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import moa.Spectrum
import org.openscience.cdk.Molecule
import util.chemical.Derivatizer
import util.chemical.Derivatizers

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/27/14
 * Time: 1:28 PM
 */
class CompoundShouldBeDerivatizedRule extends AbstractCurationRule {

    PredictGCMSCompoundRule predictGCMSCompoundRule

    double maximumNoneDerivatizedMass = 600

    CompoundShouldBeDerivatizedRule() {
        super()
        this.successAction =new RemoveTagAction("should be derivatized")
        this.failureAction = new AddTagAction("should be derivatized")
    }

    @Override
    boolean executeRule(CurationObject toValidate) {

        Spectrum spectrum = toValidate.getObjectAsSpectra()

        if (spectrum.chemicalCompound == null)
            return true

        //first check if chemical and biological compound are the same
        if (spectrum.biologicalCompound.id == spectrum.chemicalCompound.id) {

            //let's check if there is a predicted compound available
            if (spectrum.predictedCompound != null) {

                //success nothing todo
                return true

            } else {
                //predict a compound
                predictGCMSCompoundRule.executeRule(toValidate)
            }
        }

        //check if the mass of the chemical none derivatized compound is small the the max none derivatized mass
        if (calculateMolareMass(readMolecule(spectrum.chemicalCompound)) < maximumNoneDerivatizedMass) {
            return true
        }

        //check if the compound has a TMS group, which implies derivatization

        Molecule compound = readMolecule(spectrum.chemicalCompound)

        for(Derivatizer derivatizer : Derivatizers.getDerivatizers()){

            //if it's derivatized the rule is irrelevant
            if(derivatizer.isDerivatized(compound)){
                return true
            }
        }

        //add a new label, this compound should be derivatized!
        return false
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return isGCMSSpectra(toValidate)
    }

    @Override
    String getDescription() {
        return "should the biological compound be derivatized to be valid, this is the case of the mass is > ${maximumNoneDerivatizedMass}"
    }
}
