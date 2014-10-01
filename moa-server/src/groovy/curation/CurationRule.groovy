package curation

import moa.Spectrum

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
    boolean executeRule(Spectrum spectrum)

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

}