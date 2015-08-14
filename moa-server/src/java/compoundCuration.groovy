/**
 * defines the workflow for our compound curation tasks
 */
import curation.CurationWorkflow
import curation.rules.compound.DeleteComputedNamesRule
import curation.rules.compound.cts.MergeCTSDataForCompound
import curation.rules.compound.inchi.VerifyInChIKeyAndMolFileMatchRule
import curation.rules.compound.meta.CompoundComputeMetaDataRule
import curation.rules.compound.meta.DeletedComputedMetaDataRule
import curation.rules.tag.RemoveComputedTagRule

beans {

//computes the compound validation data
    computeCompoundValidationData(CompoundComputeMetaDataRule) { bean ->
        bean.autowire = 'byName'
    }
    inchiKeyMatchesMolFile(VerifyInChIKeyAndMolFileMatchRule) { bean ->
        bean.autowire = 'byName'
    }

    deleteMetaDataRule(DeletedComputedMetaDataRule) { bean ->
        bean.autowire = 'byName'
    }


    deleteRuleBasedTagRule(RemoveComputedTagRule) { bean ->
        bean.autowire = 'byName'
    }

    mergeCtsData(MergeCTSDataForCompound) { bean ->
        bean.autowire = 'byName'
    }

    deleteNameRule(DeleteComputedNamesRule) { bean ->
        bean.autowire = 'byName'
    }

/**
 * actual workflow to be executed
 */
    compoundCurationWorkflow(CurationWorkflow) { bean ->
        bean.autowire = 'byName'

        rules = [
                deleteNameRule,
                deleteMetaDataRule,
                deleteRuleBasedTagRule,
                computeCompoundValidationData,
                inchiKeyMatchesMolFile,
                mergeCtsData
        ]
    }
}