package curation.scoring

import curation.CurationAction
import curation.CurationObject
import moa.Spectrum
import moa.scoring.Impact
import moa.server.scoring.ScoringService
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/26/15
 * Time: 12:46 PM
 */
class ScoringAction implements CurationAction {

    Logger logger = Logger.getLogger(getClass())

    double modifier = 1.0;
    ScoringRule rule;
    ScoringService service;

    ScoringAction(
            double modifier, ScoringRule
                    rule,
            ScoringService service
    ) {
        this.modifier = modifier
        this.rule = rule;
        this.service = service;
    }

    @Override
    void doAction(CurationObject toValidate) {
        Scoreable s = toValidate.getObjectAsScoreable()

        Impact impact = new Impact()
        impact.impactValue = modifier;
        impact.reason = getDescription()
        impact.scoringClass = rule.getClass().getName()

        logger.info(getDescription());
        service.adjustScore(s, impact);
    }

    @Override
    boolean actionAppliesToObject(CurationObject toValidate) {
        return toValidate.isScoreable()
    }

    @Override
    String getDescription() {
        if (modifier > 0) {
            return "score was adjusted by '${modifier}' because object did have ${rule.getReasonForScore()}"
        } else {
            return "score was adjusted by '${modifier}' because object failed to have ${rule.getReasonForScore()}"
        }
    }
}
