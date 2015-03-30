package curation.scoring

import curation.CurationObject
import curation.CurationWorkflow
import moa.server.scoring.ScoringService

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/25/15
 * Time: 12:23 PM
 */
class ScoringWorkflow extends CurationWorkflow {

    @Override
    protected boolean determineResultBasedOnWorkflowStatus(boolean workflowResult) {
        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject curationObject) {
        return curationObject.isSpectra()
    }

    @Override
    String getDescription() {
        return "generates a score for the provided spectrum object"
    }

    @Override
    boolean executeRule(CurationObject spectrum) {
        return super.executeRule(spectrum)
    }

    @Override
    protected boolean abortOnFailure() {
        return false
    }
}
