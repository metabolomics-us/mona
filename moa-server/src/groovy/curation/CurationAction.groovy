package curation

import moa.Spectrum

/**
 *
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:23 AM
 */
interface CurationAction  extends CommonTags{

    /**
     * executes a given action
     */
    public void doAction(Spectrum spectrum);
}
