package validation

import moa.Spectrum
import moa.Tag
import org.apache.log4j.Logger
import validation.actions.IgnoreOnFailureAction

/**
 * workflows are rules them self, so we can chain one workflow to the next and so on
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:24 AM
 */
class ValidationWorkflow extends AbstractValidationRule{

    Logger logger = Logger.getLogger(getClass())

    /**
     * list of our validation to execute
     */
    List rules = []

    def ValidationWorkflow(ValidationAction successAction, ValidationAction failureAction) {
        super(successAction, failureAction)
    }

    def ValidationWorkflow(){
        super(new IgnoreOnFailureAction(),new IgnoreOnFailureAction())
    }
/**
     * runs the complete workflow
     * @param toValidate
     * @return               ˜
     */
    boolean runWorkflow(Spectrum toValidate) {

        toValidate.attach()

        Tag.findAllByRuleBased(true){ Tag it ->
            if(toValidate.getTags().contains(it)){
                toValidate.removeFromTags(it)
                toValidate.save(flush:true)
            }
        }

        for (SpectraValidationRule rule : rules) {

            logger.info("executing rule: ${rule.getClass().getName()}")
            try {
                if (rule.executeRule(toValidate)) {
                    logger.info("\t=> success")
                    rule.getSuccessAction().doAction(toValidate)
                } else {
                    logger.info("\t=> failed")
                    try {
                        rule.getFailureAction().doAction(toValidate)
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
        return true;
    }

    @Override
    boolean executeRule(Spectrum spectrum) {
        return runWorkflow(spectrum)
    }
}
