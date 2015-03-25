package moa.scoring

import grails.converters.JSON
import moa.Spectrum
import moa.server.scoring.ScoringService
import org.spockframework.compiler.model.Spec

class ScoringController {

    static responseFormats = ['json']

    ScoringService scoringService

    /**
     * utilize the service to score the given spectra for us
     * @return
     */
    def score() {

        def result = scoringService.score(params.id as long)

        render([message: "the spectrum was scored and received the following score", score: Spectrum.get(params.id as long).score.score] as JSON)
    }


    def scoreExplain() {

        def result = scoringService.score(params.id as long)

        Spectrum spectrum = Spectrum.get(params.id as long)

        render([message: "the spectrum was scored and received the following score", score: spectrum.score.score, explaination: spectrum.score] as JSON)

    }

}
