package curation.scoring.spectrum

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

    HasFieldScoring(String field,Double impact = 1.0, Double successScore = 0.1, Double failureScore = -0.1){
        fieldToHave = field
        this.scoreImpact = impact

        if (impact != null) {
            this.scoreImpact = impact
        }
        if (successScore != null) {
            this.successScore = successScore
        }

        if (failureScore != null) {
            this.failureScore = failureScore
        }
    }

    @Override
    boolean scoreSpectrum(Spectrum spectrum) {

        boolean result = false;
        spectrum.listAvailableValues().each { MetaDataValue v ->
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
