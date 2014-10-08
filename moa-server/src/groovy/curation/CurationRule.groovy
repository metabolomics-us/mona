package curation
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:23 AM
 */
public interface CurationRule extends CommonTags {

    /**
     * executes the rule for the given spectrum
     * @param spectrum
     * @return
     */
    boolean executeRule(CurrationObject toValidate)

    /**
     * returns the success action
     * @return
     */
    CurationAction getSuccessAction()

    /**
     * returns the failure action
     * @return
     */
    CurationAction getFailureAction()

    /**
     * does this rule apply to the given object
     * @param CurrationObject
     * @return
     */
    boolean ruleAppliesToObject(CurrationObject toValidate);

}