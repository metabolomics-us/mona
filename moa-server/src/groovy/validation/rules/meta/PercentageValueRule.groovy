package validation.rules.meta
import moa.MetaDataValue
import validation.actions.MetaDataSuspectAction
import validation.rules.AbstractMetaDataCentricRule
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 2:08 PM
 */
class PercentageValueRule  extends AbstractMetaDataCentricRule {

    private String field

    double maxPercentage = 100
    double minPercentage = 0

    PercentageValueRule(String field) {
        super(new MetaDataSuspectAction(field,false), new MetaDataSuspectAction(field,true))
        this.field = field
    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue value) {

        if(value.name.toLowerCase().equals(field)){

            if(value.unit != null && value.unit.toLowerCase().equals("%")){
                try{
                    Double val = value.toString() as double

                    return (val >= minPercentage && val <= maxPercentage)

                }
                catch(NumberFormatException e){
                    return false;
                }
            }
        }
        return false
    }
}
