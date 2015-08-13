package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import moa.Spectrum
import moa.server.query.SpectraQueryService
import moa.server.scoring.ScoringService

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 5/13/15
 * Time: 3:45 PM
 */
class RemoveIdenticalSpectraRule extends AbstractCurationRule {

    ScoringService scoringService

    SpectraQueryService spectraQueryService

    RemoveIdenticalSpectraRule() {
    }

    @Override
    boolean executeRule(CurationObject toValidate) {

        Spectrum spectrum = toValidate.getObjectAsSpectra()

        if (spectrum.splash) {

            logger.info("received spectrum: ${spectrum.id}")
            logger.info("received splash: ${spectrum.splash}")
            logger.info("received compound: ${spectrum.biologicalCompound}")

            def identical = spectraQueryService.query([
                    match   : [
                            exact: spectrum.splash.splash
                    ],
                    compound: [
                            inchiKey: [
                                    eq: spectrum.biologicalCompound.inchiKey
                            ]
                    ]

            ],-1,-1,"")

            logger.info("found ${identical.size()} spectra")
            identical.each { Spectrum compare ->
                if (compare.score == null) {
                    logger.info("having to score similar spectra...")
                    scoringService.score(compare)
                }
            }

            Collections.sort(identical, new Comparator<Spectrum>() {
                @Override
                int compare(Spectrum o1, Spectrum o2) {
                    return o2.score.getRelativeScore().compareTo(o1.score.getRelativeScore())
                }
            })

            //only if we have some identical spectra, otherwise move on
            if (!identical.isEmpty()) {
                //ours is not the best hit
                if (identical[0].id.equals(spectrum.id)) {

                    return true
                }
                //delete it
                else {
                    spectrum.deleted = true
                    spectrum.save()
                }
            }
        }
        return true;

    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "this rule hides identical spectra from the system and keeps the one, with the highest score. The identity is ensure by the spectra hash"
    }
}
