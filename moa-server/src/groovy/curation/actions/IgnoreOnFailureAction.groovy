package curation.actions
import curation.CurationAction
import curation.CurrationObject
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:42 AM
 */
class IgnoreOnFailureAction implements CurationAction{
    @Override
    void doAction(CurrationObject spectrum) {

        //nothing
    }

    @Override
    boolean actionAppliesToObject(CurrationObject toValidate) {
        return true
    }
}
