package curation.scoring

import moa.MetaDataValue
import moa.Spectrum

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/25/15
 * Time: 2:05 PM
 */
class HasFieldScoring extends curation.scoring.ScoringRule {
    String fieldToHave

    HasFieldScoring(String field){
        this(field, 1)
    }

    HasFieldScoring(String field,Double impact){
        fieldToHave = field
        this.scoreImpact = impact
    }
    @Override
    boolean scoreSpectrum(Spectrum spectrum) {

        boolean result = false;
        spectrum.metaData.each { MetaDataValue v ->
            if (v.getName().toLowerCase().equals(fieldToHave.toLowerCase())) {
                result = true;
            }
        }
        return result
    }

    @Override
    String getReasonForScore() {
        return "metadata field '${fieldToHave}'"
    }

    @Override
    String getDescription() {
        return "this rule increases the score of a spectrum if the field '$fieldToHave' is provided or computed as metadata field"
    }
}
