package validation

import validation.actions.IgnoreOnFailureAction

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:37 AM
 */
abstract class AbstractValidationRule implements SpectraValidationRule {

    protected ValidationAction successAction

    protected ValidationAction failureAction

    /**
     * default constructor
     * @param successAction
     * @param failureAction
     */
    public AbstractValidationRule(ValidationAction successAction, ValidationAction failureAction){
        this.successAction = successAction
        this.failureAction = failureAction
    }

    public AbstractValidationRule(){
        this.successAction = new IgnoreOnFailureAction()
        this.failureAction = new IgnoreOnFailureAction()
    }

    @Override
    final ValidationAction getSuccessAction() {
        return successAction
    }

    @Override
    final ValidationAction getFailureAction() {
        return failureAction
    }
}
