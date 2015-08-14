package curation.rules.compound

import curation.AbstractCurationRule
import curation.CurationObject
import grails.plugins.rest.client.RestBuilder
import moa.Compound
import moa.Name
import moa.server.NameService
import moa.server.metadata.MetaDataPersistenceService
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 8/14/15
 * Time: 11:54 AM
 */
class DeleteComputedNamesRule extends AbstractCurationRule {

    private Logger logger = Logger.getLogger(getClass())

    MetaDataPersistenceService metaDataPersistenceService

    NameService nameService

    @Override
    boolean executeRule(CurationObject toValidate) {

        Compound compound = toValidate.objectAsCompound

        logger.info("running rule on: ${compound}")

        def namesToDelete = []

        compound.names.each {Name name ->
            if(name.computed){
                namesToDelete.add(name)
            }
        }

        namesToDelete.each {
            compound.removeFromNames(it)
            it.compound = null
            it.delete()
        }


        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isCompound()
    }


    @Override
    String getDescription() {
        return "this rule deletes all computed names"
    }
}
