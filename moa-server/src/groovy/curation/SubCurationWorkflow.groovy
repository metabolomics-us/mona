package curation

import curation.actions.AddTagAction
import curation.actions.IgnoreOnFailureAction
import curation.actions.RemoveTagAction
import org.apache.log4j.Logger
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 1:45 PM
 */
class SubCurationWorkflow extends CurationWorkflow implements CurationRule {
    private Logger logger = Logger.getLogger(getClass())

    private boolean abortOnFailure
    private String tag

    SubCurationWorkflow(boolean abortOnFailure) {
        super(new IgnoreOnFailureAction(),new IgnoreOnFailureAction())
        this.abortOnFailure = abortOnFailure
    }

    SubCurationWorkflow(String tag, boolean abortOnFailure, String description) {
        super(new RemoveTagAction(tag), new AddTagAction(tag))

        logger.debug("Starting subcuration workflow "+ description)
        this.abortOnFailure = abortOnFailure
        this.tag = tag
    }

    @Override
    protected boolean determineResultBasedOnWorkflowStatus(boolean workflowResult) {
        return workflowResult;
    }

    @Override
    protected boolean failByDefault() {
        return true;
    }

    @Override
    protected boolean abortOnFailure() {
        return abortOnFailure
    }
}
