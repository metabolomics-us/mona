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
        return new ScoreAction(0.1 * getScoreImpact(), this, scoringService);
    }

    @Override
    final CurationAction getFailureAction() {
        return new ScoreAction(-0.1 * getScoreImpact(), this, scoringService)
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

/**
 * modify the object
 */
    protected class ScoreAction implements CurationAction {

        double modifier = 1.0;
        ScoringRule rule;
        ScoringService service;

        ScoreAction(double modifier, ScoringRule rule, ScoringService service) {
            this.modifier = modifier
            this.rule = rule;
            this.service = service;
        }

        @Override
        void doAction(CurationObject toValidate) {
            Spectrum s = toValidate.getObjectAsSpectra()

            Impact impact = new Impact()
            impact.impactValue = modifier;
            impact.reason = getDescription()
            impact.scoringClass = rule.getClass().getName()

            service.adjustScore(s, impact);
        }

        @Override
        boolean actionAppliesToObject(CurationObject toValidate) {
            return toValidate.isSpectra()
        }

        @Override
        String getDescription() {
            if(modifier > 0){
                return "score was adjusted by '${modifier}' because object did have ${rule.getReasonForScore()}"
            }
            else{
                return "score was adjusted by '${modifier}' because object failed to have ${rule.getReasonForScore()}"
            }
        }
    }
}

