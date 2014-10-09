package curation.actions
import curation.CurationAction
import curation.CurationObject
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:42 AM
 */
class IgnoreOnFailureAction implements CurationAction{
    @Override
    void doAction(CurationObject spectrum) {

        //nothing
    }

    @Override
    boolean actionAppliesToObject(CurationObject toValidate) {
        return true
    }
}
