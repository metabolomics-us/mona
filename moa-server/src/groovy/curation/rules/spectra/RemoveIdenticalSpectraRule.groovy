package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import moa.Spectrum
import moa.server.scoring.ScoringService

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 5/13/15
 * Time: 3:45 PM
 */
class RemoveIdenticalSpectraRule extends AbstractCurationRule{

    ScoringService scoringService

    RemoveIdenticalSpectraRule(){
        this.successAction = new RemoveTagAction(REQUIRES_DELETE)
        this.failureAction = new AddTagAction(REQUIRES_DELETE)
    }
    @Override
    boolean executeRule(CurationObject toValidate) {

        Spectrum spectrum = toValidate.getObjectAsSpectra()

        def identical = Spectrum.findAllBySplashAndChemicalCompound(spectrum.splash,spectrum.biologicalCompound)

        identical.each {Spectrum compare ->
            if(compare.score == null) {
                scoringService.score(compare)
            }
        }

        Collections.sort(identical,new Comparator<Spectrum>() {
            @Override
            int compare(Spectrum o1, Spectrum o2) {
                return o2.score.getRelativeScore().compareTo(o1.score.getRelativeScore())
            }
        })

        //ours is the best hit
        if(identical[0].id.equals(spectrum.id)){
            return true
        }
        else{
           return false
        }

    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "this rule flags identical spectra for removal from the system and keeps the one, with the highest score. The identity is ensure by the spectra hash"
    }
}
