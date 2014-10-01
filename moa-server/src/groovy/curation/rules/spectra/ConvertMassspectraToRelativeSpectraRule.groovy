package curation.rules.spectra

import curation.AbstractCurationRule
import moa.Spectrum

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/1/14
 * Time: 1:15 PM
 */
class ConvertMassspectraToRelativeSpectraRule extends AbstractCurationRule{
    @Override
    boolean executeRule(Spectrum spectrum) {

        String massSpec = spectrum.spectrum


        massSpec.split(" ").each { s ->
            s.split(":").each { i ->

                if(i.size() > 1){
                    double d = Double.parseDouble(i[1])
                }
            }
        }
        int max = 0

        return true
    }
}
