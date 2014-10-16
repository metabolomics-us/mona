package curation.rules.meta

import curation.actions.MetaDataSuspectAction
import curation.rules.AbstractMetaDataCentricRule
import moa.MetaDataValue
import moa.Spectrum
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/15/14
 * Time: 3:10 PM
 */
class ProvidedExactMassIsPossibleRule extends AbstractMetaDataCentricRule{

    String field

    double accuracyInDalton = 5

    ProvidedExactMassIsPossibleRule(String field) {
        super(new MetaDataSuspectAction(field, false), new MetaDataSuspectAction(field, true))
        this.field = field
    }

    ProvidedExactMassIsPossibleRule(){
        this("exact mass")
    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue value) {

        Spectrum spectrum = value.owner

        if(value.name == field){
            String val = value.getValue().toString()

            double mass = Double.parseDouble(val)

            double bioMass = calculateMolareMass(readMolecule(spectrum.getBiologicalCompound()))
            double chemMass = calculateMolareMass(readMolecule(spectrum.getChemicalCompound()))

            logger.debug("bioMass: ${bioMass}")
            logger.debug("chemMass: ${chemMass}")
            logger.debug("exactMass: ${mass}")


            if(Math.abs(bioMass-mass) <= accuracyInDalton){
                return true
            }

            if(Math.abs(chemMass-mass) <= accuracyInDalton){
                return true
            }

        }
        return false
    }

    @Override
    protected boolean isCorrectMetaDataField(MetaDataValue field) {
        return field.name == this.field
    }

    @Override
    String getDescription() {
        return "this rule checks, if the provided field ${field} is actually possible for the biological or chemical compound"
    }
}
