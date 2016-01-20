package curation.rules.ontology

import curation.AbstractCurationRule
import curation.CurationObject
import moa.MetaDataValue

/**
 * this will attempt to automatically curate specified metadata name to the ontology based name, using text similarity
 * User: wohlgemuth
 * Date: 10/8/14
 * Time: 3:11 PM
 */
class MetaDataNameCurationRule extends AbstractCurationRule{

    /**
     * the required string similarity to automatically correct the name to an ontology value
     */
    double similarityForAutoccuration = 0.95

    public MetaDataNameCurationRule() {}

    @Override
    boolean executeRule(CurationObject toValidate) {
        if(toValidate.isSpectra()) {
            toValidate.objectAsSpectra.listAvailableValues().each {MetaDataValue m ->
                checkValue(m)
            }
        } else if(toValidate.isMetaData()) {
            checkValue(toValidate.getObjectAsMetaDataValue())
        }

        //we always return true
        return true
    }

    private void checkValue(MetaDataValue m) {
        //TODO
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return true
    }


    @Override
    String getDescription() {
        return "this rule verifies MetaData names against an Ontology"
    }
}
