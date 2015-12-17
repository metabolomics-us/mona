package curation.scoring.spectrum

import curation.scoring.ScoringRule
import moa.MetaDataValue
import moa.Spectrum

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/26/15
 * Time: 2:17 PM
 */
class FieldIsSuspectScoring extends ScoringRule {
    String fieldToHave

    FieldIsSuspectScoring(String field, Double impact = 1.0, Double successScore = -0.4, Double failureScore = 0) {
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
                if (v.suspect) {
                    result = true;
                }
            }
        }
        return result
    }

    @Override
    String getReasonForScore() {
        return "a suspect metadata field '${fieldToHave}' "
    }

    @Override
    String getDescription() {
        return "this rule decreases the score of a spectrum if the field '$fieldToHave' is suspect for some reason. This means it's validation most likely encounterned an error"
    }
}
