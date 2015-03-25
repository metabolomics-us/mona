package curation.scoring

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

    public HasAssociatedFieldsScoring(String firstFieldToHave, String secondFieldToHave) {
        this(firstFieldToHave, secondFieldToHave, 1.0)
    }

    public HasAssociatedFieldsScoring(String first, String second, Double impact) {
        this.firstFieldToHave = first
        this.secondFieldToHave = second
        this.scoreImpact = impact
    }

    @Override
    boolean scoreSpectrum(Spectrum spectrum) {

        boolean hasFirst = false;
        boolean hasSecond = false;

        spectrum.metaData.each { MetaDataValue v ->
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
