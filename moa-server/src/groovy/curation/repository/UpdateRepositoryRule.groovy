package curation.repository

import curation.AbstractCurationRule
import curation.CurationObject
import util.FireJobs

/**
 * every time a spectra has been modified, this rule will automatically schedule a repository update
 */
class UpdateRepositoryRule extends AbstractCurationRule {

    @Override
    boolean executeRule(CurationObject toValidate) {
        FireJobs.fireSpectraRepositoryExportJob([id: toValidate.getObjectAsSpectra().id])

        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "this rule, schedules that the static repository should be updated to the latest version, for this spectra"
    }
}
