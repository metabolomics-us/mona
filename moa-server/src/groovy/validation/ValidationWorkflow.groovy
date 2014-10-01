package validation

import moa.Spectrum
import org.apache.log4j.Logger
import validation.actions.IgnoreOnFailureAction

/**
 * workflows are rules them self, so we can chain one workflow to the next and so on
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:24 AM
 */
class ValidationWorkflow extends AbstractValidationRule {

    Logger logger = Logger.getLogger(getClass())

    /**
     * list of our validation to execute
     */
    List rules = []

    def ValidationWorkflow(ValidationAction successAction, ValidationAction failureAction) {
        super(successAction, failureAction)
    }

    def ValidationWorkflow() {
        super(new IgnoreOnFailureAction(), new IgnoreOnFailureAction())
    }
/**
 * runs the complete workflow
 * @param toValidate
 * @return ˜
 */
    final boolean runWorkflow(Spectrum toValidate) {

        if (rules.isEmpty()) {
            throw new Exception("please add at least 1 rule to be executed!")
        }

        boolean result = false;

        toValidate.attach()

        for (SpectraValidationRule rule : rules) {

            logger.info("executing rule: ${rule.getClass().getName()}")
            try {

                result = rule.executeRule(toValidate)

                if (result) {
                    logger.info("\t=> success, execution action ${rule.successAction.getClass().getName()} for rule ${rule.getClass().getName()}")
                    rule.getSuccessAction().doAction(toValidate)
                } else {
                    logger.info("\t=> failed, execution action ${rule.failureAction.getClass().getName()} for rule ${rule.getClass().getName()}")
                    try {
                        rule.getFailureAction().doAction(toValidate)
                        if(abortOnFailure()){
                            logger.info("\t=> ${this.getClass().getName()} is designed to break on failure, exciting loop")
                            return false;
                        }
                    }
                    catch (Exception e) {
                        return false
                    }
                }


            }
            catch (Exception e) {
                throw e;
            }

        }

        //we always return true by default


        if (result) {
            return true
        }

        return failByDefault();
    }

    @Override
    boolean executeRule(Spectrum spectrum) {
        return runWorkflow(spectrum)
    }

    /**
     * do we abort the complete workflow on failure
     * @return
     */
    protected boolean abortOnFailure() {
        return false
    }
}
