import curation.scoring.HasAssociatedFieldsScoring
import curation.scoring.HasFieldScoring
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
     * let's build a bunch of rules, requiering some metadata with the default impact
     */
    [
            'ri',
            'ion mode',
            'injection',
            'column',
            'mass resolution',
            'instrument',
            'instrument type',
            'derivative type',
            'exact mass',
            'collision energy',
            'ms type',
            [field:'fragmentation method',impact:0.1]

    ].each {
        //in case it's a string just use it
        if (it instanceof String) {
            "${it}hasField"(HasFieldScoring, it)
            allMyBeans.add(ref("${it}hasField"))

        }
            //in case of a map, we setting additional properties
        if(it instanceof Map){
            "${it.field}hasField"(HasFieldScoring, it.field,it.impact)
            allMyBeans.add(ref("${it.field}hasField"))
        }

    }

    /**
     * let's build a bunch of associated fields
     */

    [
            //retention time and column information
            [first: 'ri', second: 'column'],
            [first: 'column', second: 'column temperature'],

            //general injection information
            [first: 'injection', second: 'injection volume'],
            [first: 'injection', second: 'injection temperature'],

            //instrument data
            [first: 'instrument', second: 'instrument type'],

            //if we have a gradient specified, we should have also mobile phases defined
            [first: 'gradient', second: 'mobile phase a'],
            [first: 'gradient', second: 'mobile phase b'],
            [first: 'mobile phase a', second: 'mobile phase b']

    ]
    //go over our map and build the acutal beans based on the config
            .each { map ->

        if (map.impact) {
            "${map.first}_${map.second}hasAssociatedFields"(HasAssociatedFieldsScoring, map.first, map.second, map.impact)
        } else {
            "${map.first}_${map.second}hasAssociatedFields"(HasAssociatedFieldsScoring, map.first, map.second)
        }
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