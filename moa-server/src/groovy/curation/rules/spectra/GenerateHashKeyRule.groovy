package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SplashUtil
import moa.Spectrum
import moa.splash.Splash

import javax.sql.DataSource

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 5/28/15
 * Time: 12:05 PM
 */
class GenerateHashKeyRule extends AbstractCurationRule {

    DataSource dataSource

    boolean executeRule(CurationObject toValidate) {

        Spectrum spectrum = toValidate.getObjectAsSpectra()

        Splash splash = spectrum.getSplash();

        if(splash == null){
            splash = new Splash();
            splash.spectrum = spectrum
            spectrum.splash = splash
        }

        splash.splash = (SplashUtil.splash(spectrum.getSpectrum(),SpectraType.MS))

        splash.save()

        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "generates the splash"
    }
}
