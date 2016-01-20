package curation
import curation.actions.IgnoreOnFailureAction
import moa.Spectrum
import moa.server.UpdateSpectrumTimestampService
import moa.server.query.SpectraQueryService
import org.apache.log4j.Logger
/**
 * workflows are rules them self, so we can chain one workflow to the next and so on
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:24 AM
 */
class CurationWorkflow extends AbstractCurationRule implements Workflow {

    Logger logger = Logger.getLogger(getClass())

    UpdateSpectrumTimestampService updateSpectrumTimestampService
    SpectraQueryService spectraQueryService

    /**
     * list of our curation to execute
     */
    List rules = []

    def CurationWorkflow(CurationAction successAction, CurationAction failureAction) {
        super(successAction, failureAction)
    }

    def CurationWorkflow() {
        super(new IgnoreOnFailureAction(), new IgnoreOnFailureAction())
    }

    /**
     * runs the complete workflow
     * @param toValidate
     * @return ˜
     */
    final boolean runWorkflow(CurationObject toValidate) {
        Date startTime = new Date()

        if (rules.isEmpty()) {
            throw new Exception("please add at least 1 rule to be executed!")
        }

        logger.debug("running ${rules.size()} rules")

        rules.each { CurationRule rule ->
            logger.debug("having to run: ${rule.getClass().getName()}:${rule.getDescription()}")
        }

        boolean workflowResult = true;

        toValidate.refreshObject()

        for (CurationRule rule : rules) {
            long time = System.currentTimeMillis();
            Date lastUpdated = toValidate.isSupportsMetaDataObject() ? toValidate.getObjectAsSupportsMetaData().lastUpdated : null;

            logger.info("STARTED - executing rule: ${rule.getClass().getName()}")

            try {
                if(rule.ruleAppliesToObject(toValidate)) {
                    boolean result = rule.executeRule(toValidate)

                    if (result) {
                        logger.info("\t=> success, execution action ${rule.successAction.getClass().getName()} for rule ${rule.getClass().getName()}")
                        rule.getSuccessAction().doAction(toValidate)
                    } else {
                        logger.info("\t=> failed, execution action ${rule.failureAction.getClass().getName()} for rule ${rule.getClass().getName()}")
                        try {
                            rule.getFailureAction().doAction(toValidate)
                            if (abortOnFailure()) {
                                logger.info("\t=> ${this.getClass().getName()} is designed to break on failure, exciting loop")
                                return false;
                            }
                        }
                        catch (Exception e) {
                            logger.warn(e.getMessage(),e)
                            return false
                        }
                    }
                } else {
                    logger.debug("rule doesn't apply to this kind of object, so it's valid by default!")
                    return true;
                }

                // Handle updates to compound objects
                if (toValidate.isCompound() && toValidate.getObjectAsCompound().lastUpdated.after(lastUpdated)) {
                    updateSpectrumTimestampService.updateSpectrumTimestampsByCompound(toValidate.getObjectAsCompound())
                }
            }
            catch (Exception e) {
                throw e;
            }
            finally {
                logger.info("FINSIHED - executing rule: ${rule.getClass().getName()}, duration:" + ((System.currentTimeMillis() - time) / 1000))
            }
        }


        return determineResultBasedOnWorkflowStatus(workflowResult)
    }

    /**
     * determine the final result status of the workflow and if it fails be default or not
     * @param workflowResult
     * @return
     */
    protected boolean determineResultBasedOnWorkflowStatus(boolean workflowResult) {
        if (workflowResult) {
            return true
        }

        return failByDefault();
    }

    /**
     * executes the given workflow
     * @param spectrum
     * @return
     */
    @Override
    boolean ruleAppliesToObject(CurationObject curationObject) {
        return true
    }

    @Override
    String getDescription() {
        return "this is a rule, which can contain further rules"
    }

    @Override
    boolean executeRule(CurationObject spectrum) {
        return runWorkflow(spectrum)
    }

    /**
     * do we abort the complete workflow on failure, this means no further internal rules should be executed
     * @return
     */
    protected boolean abortOnFailure() {
        return false
    }
}
