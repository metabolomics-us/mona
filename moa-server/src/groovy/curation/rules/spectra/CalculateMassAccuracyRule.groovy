package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SplashUtil
import moa.Spectrum
import moa.splash.Splash

import javax.sql.DataSource

/**
 * computes the mass accuracy of the spectra,
 * based on the exactmass and theoretical mass
 */
class CalculateMassAccuracyRule extends AbstractCurationRule {


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
        return "calculates the mass accuracy of the given spectra and add's this as metadata field"
    }
}