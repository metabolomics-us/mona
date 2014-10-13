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

    public MetaDataNameCurationRule(){

    }
    /**
     * the required string similarity to automatically correct the name to an ontology value
     */
    double similarityForAutoccuration = 0.95


    @Override
    boolean executeRule(CurationObject toValidate) {
        if(toValidate.isSpectra()){

            toValidate.objectAsSpectra.metaData.each {MetaDataValue m ->
                checkValue(m)
            }
        }
        else if(toValidate.isMetaData()){
            checkValue(toValidate.getObjectAsMetaDataValue())
        }
        //we always return true
        return true
    }

    private void checkValue(MetaDataValue m){

    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return true
    }
}
