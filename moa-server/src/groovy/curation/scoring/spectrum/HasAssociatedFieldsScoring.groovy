package curation.scoring.spectrum

import moa.MetaDataValue
import moa.Spectrum

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/25/15
 * Time: 2:53 PM
 */
class HasAssociatedFieldsScoring extends curation.scoring.ScoringRule {
    String firstFieldToHave

    String secondFieldToHave

    public HasAssociatedFieldsScoring(String first, String second, Double impact = 1.0, Double successScore = 0.1, Double failureScore = -0.1) {
        this.firstFieldToHave = first
        this.secondFieldToHave = second

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

        boolean hasFirst = false;
        boolean hasSecond = false;

        spectrum.listAvailableValues().each { MetaDataValue v ->
            if (v.getName().toLowerCase().equals(firstFieldToHave.toLowerCase())) {
                hasFirst = true;
            } else if (v.getName().toLowerCase().equals(secondFieldToHave.toLowerCase())) {
                hasSecond = true
            }
        }
        return hasFirst && hasSecond
    }

    @Override
    String getReasonForScore() {
        return "associated metadata fields '${firstFieldToHave}' and ${secondFieldToHave}"
    }

    @Override
    String getDescription() {
        return "this rule increases the score of a spectrum if the field '$firstFieldToHave' and the field '$secondFieldToHave' are provided or computed as metadata field"
    }
}
