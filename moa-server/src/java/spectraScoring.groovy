import curation.scoring.spectrum.FieldIsSuspectScoring
import curation.scoring.spectrum.HasAssociatedFieldsScoring
import curation.scoring.spectrum.HasFieldScoring
import curation.scoring.ScoringWorkflow

/**
 * configuration of all the scorings to be done in the system
 */
beans {

    /**
     * contains all our dynamically create beans and rules
     */
    def allMyBeans = []

    /**
     * basically the most common metadata fields, which should always be correct and have none suspicous values
     */
    [
            'ri',
            'ion mode',
            'injection',
            'injection volume',
            'column',
            'mass resolution',
            'instrument',
            'instrument type',
            'derivative type',
            'exact mass',
            'collision energy',
            'ms type',
            [field: 'fragmentation method', impact: 0.1],
            'data processing',
            'column temperature'


    ]
    /**
     * build our actual beans, just some mojo to save typing
     */
            .each {
        //in case it's a string just use it
        if (it instanceof String) {
            "${it}hasField"(HasFieldScoring, it)
            "${it}fieldIsSuspect"(FieldIsSuspectScoring, it)

            allMyBeans.add(ref("${it}hasField"))
            allMyBeans.add(ref("${it}fieldIsSuspect"))


        }
        //in case of a map, we setting additional properties
        if (it instanceof Map) {
            "${it.field}hasField"(HasFieldScoring, it.field, it?.impact)
            "${it.field}fieldIsSuspect"(FieldIsSuspectScoring, it.field, it?.impact)

            allMyBeans.add(ref("${it.field}hasField"))
            allMyBeans.add(ref("${it.field}fieldIsSuspect"))

        }

    }

    /**
     * these fields should always be provided with additional fields to ensure,
     * which will increase the score dramatically, but won't drop it if they are missing
     */

    [
            //retention time and column information
            [first: 'ri', second: 'column', failure: 0.0],
            [first: 'column', second: 'column temperature', failure: 0.0],

            //if we have a column, we might also have a guard column, but it's not that important
            [first: 'column', second: 'guard column', failure: 0.0, success: 0.05],
            [first: 'column', second: 'column temperature', failure: 0.0, success: 0.05],

            //general injection information
            [first: 'injection', second: 'injection volume', failure: 0.0],
            [first: 'injection', second: 'injection temperature', failure: 0.0],

            //instrument data
            [first: 'instrument', second: 'instrument type', failure: 0.0],

            //if we have a gradient specified, we should have also mobile phases defined
            [first: 'gradient', second: 'mobile phase a', failure: 0.0],
            [first: 'gradient', second: 'mobile phase b', failure: 0.0],
            [first: 'mobile phase a', second: 'mobile phase b', failure: 0.0],

            //if we have a column, we might have a mobile phase
            [first: 'column', second: 'mobile phase', failure: 0.0],

            //if we have a precursir type, we should also have a mass
            [first: 'precursortype', second: 'precursormz', failure: 0.0],

            //if we have a MS type, we should always have an ion mode
            [first: 'ms type', second: 'ion mode', failure: 0.0],

            //if we have a MS type, we should always have a collision energy
            [first: 'ms type', second: 'collision energy', failure: 0.0],


    ]
    /**
     * build our beans based on the given configuration
     */
            .each { map ->

        "${map.first}_${map.second}hasAssociatedFields"(HasAssociatedFieldsScoring, map.first, map.second, map.impact, map.success, map.failure)
        allMyBeans.add(ref("${map.first}_${map.second}hasAssociatedFields"))
    }

    /**
     *
     * putting everything together
     */
    spectraScoringWorkflow(ScoringWorkflow) {

        rules = allMyBeans
    }
}