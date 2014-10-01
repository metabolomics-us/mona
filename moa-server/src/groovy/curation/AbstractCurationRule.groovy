package curation

import curation.actions.IgnoreOnFailureAction

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:37 AM
 */
abstract class AbstractCurationRule implements CurationRule {

    protected CurationAction successAction

    protected CurationAction failureAction

    /**
     * default constructor
     * @param successAction
     * @param failureAction
     */
    public AbstractCurationRule(CurationAction successAction, CurationAction failureAction){
        this.successAction = successAction
        this.failureAction = failureAction
    }

    public AbstractCurationRule(){
        this.successAction = new IgnoreOnFailureAction()
        this.failureAction = new IgnoreOnFailureAction()
    }

    @Override
    final CurationAction getSuccessAction() {
        return successAction
    }

    @Override
    final CurationAction getFailureAction() {
        return failureAction
    }


    /**
     * should we fail by default
     * @return
     */
    protected boolean failByDefault() {
        return true;
    }
}
