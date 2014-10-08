package curation
/**
 * defines a simple curation workflow
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/8/14
 * Time: 2:43 PM
 */
public interface Workflow {

    /**
     * runs our specified workflow
     * @param toValidate
     * @return
     */
    boolean runWorkflow(CurrationObject toValidate)
}