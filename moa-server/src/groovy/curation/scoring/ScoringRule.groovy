package curation.scoring

import curation.CurationAction
import curation.CurationObject
import curation.CurationRule
import moa.Spectrum
import moa.scoring.Impact
import moa.server.scoring.ScoringService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.Resource

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/25/15
 * Time: 11:40 AM
 */
@Component
abstract class ScoringRule implements CurationRule {

    /**
     * impact of the scoring
     */
    double scoreImpact = 1.0

    /**
     * score adjustment in case of success
     */
    double successScore = 0.1;

    /**
     * score adjustment in case of failure
     */
    double failureScore = -0.1;

    @Autowired
    ScoringService scoringService


    @Override
    boolean executeRule(CurationObject toValidate) {
        return scoreSpectrum(toValidate.objectAsSpectra)
    }

    /**
     * does the actual scoring
     * @param spectrum
     * @return
     */
    abstract boolean scoreSpectrum(Spectrum spectrum);

    /**
     * what was the reason for the received score
     * @return
     */
    abstract String getReasonForScore();

    @Override
    final CurationAction getSuccessAction() {
        return new ScoringAction(getSuccessScore() * getScoreImpact(), this, scoringService);
    }

    @Override
    final CurationAction getFailureAction() {
        return new ScoringAction(getFailureScore() * getScoreImpact(), this, scoringService)
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

}

