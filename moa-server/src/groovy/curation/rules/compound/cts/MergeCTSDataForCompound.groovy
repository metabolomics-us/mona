package curation.rules.compound.cts

import curation.AbstractCurationRule
import curation.CurationObject
import grails.plugins.rest.client.RestBuilder
import moa.Compound
import moa.MetaDataValue
import moa.server.NameService
import moa.server.caluclation.CompoundPropertyService
import moa.server.metadata.MetaDataPersistenceService
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 8/13/15
 * Time: 2:39 PM
 */
class MergeCTSDataForCompound extends AbstractCurationRule {

    private Logger logger = Logger.getLogger(getClass())

    MetaDataPersistenceService metaDataPersistenceService

    NameService nameService

    @Override
    boolean executeRule(CurationObject toValidate) {

        Compound compound = toValidate.objectAsCompound


        compound = Compound.lock(compound.id)

        logger.info("running rule on: ${compound} with key: ${compound.inchiKey}")




        def rest = new RestBuilder()

        def json = fetchProperty(compound, rest)

        logger.info(json.getClass())

        if (json instanceof JSONArray) {
            logger.warn("not able to find this compound in cts!")
            //ok not found
            return true
        }

        if (json.synonyms) {
            json.synonyms.each {

                if (it.type.toString().toLowerCase().equals("synonym")) {
                    nameService.addNameToCompound(it.name, compound, true,"cts")
                }

            }
        }

        if(json.externalIds){
            json.externalIds.each{
                metaDataPersistenceService.generateMetaDataObject(compound, [name: it.name, value: it.value, category: "external id", computed: true, url:it.url])
            }
        }

        return true
    }

    private def fetchProperty(Compound compound, RestBuilder rest) {
        String inchiKey = compound.inchiKey

        def json = rest.get("http://cts.fiehnlab.ucdavis.edu/service/compound/${inchiKey}").json

        if (json instanceof JSONArray) {
            compound.listAvailableValues().each { MetaDataValue v ->
                if (v.getName().equals("calculated InChI Key")) {
                    logger.info("using comupted key: ${v.getValue()}")
                    inchiKey = v.getValue().toString()
                    json = rest.get("http://cts.fiehnlab.ucdavis.edu/service/compound/${inchiKey}").json

                }
            }
        }

        return json
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isCompound()
    }


    @Override
    String getDescription() {
        return "this rule integrates all the cts based information"
    }
}
