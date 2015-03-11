package moa.server.statistics

import grails.transaction.Transactional
import groovy.sql.Sql
import moa.Compound
import moa.MetaData
import moa.MetaDataValue
import moa.Spectrum
import moa.Statistics
import moa.Submitter
import moa.Tag

import javax.sql.DataSource

/**
 * provides us with uptodate statistics of the system
 */
class StatisticsService {

    DataSource dataSource

    /**
     * counts the total number of all domain classes
     * @return
     */
    Map countAll() {
        return [
                spectra      : Spectrum.count(),
                compounds    : Compound.count(),
                metadata     : MetaData.count(),
                metadataValue: MetaDataValue.count(),
                tags         : Tag.count(),
                submitters   : Submitter.count()
        ]
    }

    /**
     * returns the spectra count for the given tag
     * @param text
     * @return
     */
    int getSpectraCountForTag(String text) {
        int count = 0

        Tag.withSession { session ->
            def result = session.createSQLQuery(" select count(*) as c from supports_meta_data_tag a, tag b where a.tag_id = b.id and b.text = ? group by text").setString(0, text).list()

            if (!result.isEmpty()) {
                count = result[0]
            }
        }

        return count
    }

    /**
     * spectra count for all tags
     * @return
     */
    List getSpectraCountForAllTags() {
        def res = []

        Tag.withSession { session ->
            def result = session.createSQLQuery(" select count(*) as count, text  from supports_meta_data_tag a, tag b where a.tag_id = b.id group by text").list()

            if (!result.isEmpty()) {
                for (Object[] o : result) {
                    res.add([count: o[0], tag: o[1]])
                }
            }
        }

        return res
    }

    /**
     * compound count for the specified tag
     * @param text
     * @return
     */
    List getCompoundCountForTag(String text) {
        int compoundCount = 0

        Tag.withSession { session ->
            def result = session.createSQLQuery("select count(*) as c from supports_meta_data_tag a, tag b where a.tag_id = b.id and b.text = ? group by text").setString(0, text).list()

            if (!result.isEmpty()) {
                compoundCount = result[0]
            }

        }

        return compoundCount
    }

    /**
     * spectra count for metadata id
     * @param id
     * @return
     */
    List getSpectraCountForMetaDataId(long id) {
        def result = []

        Spectrum.withSession { session ->
            def res = session.createSQLQuery("select count(*) as item_count, b.name, string_value from meta_data_value a, meta_data b where a.meta_data_id = b.id and b.id = ?  group by name, string_value").setLong(0, id).list()

            for (Object[] o : res) {
                result.add([count: o[0], name: o[1], value: o[2]])

            }
        }

        return result
    }

    /**
     *
     * @return
     */
    List getSpectraCountForAllSubmitters() {
        def res = []

        Submitter.withSession { session ->
            def result = session.createSQLQuery("select count(*) as count, b.first_name, b.last_name, b.email_address from spectrum a, submitter b where a.submitter_id = b.id group by b.id").list()

            if (!result.isEmpty()) {
                for (Object[] o : result) {
                    res.add([count: o[0], firstName: o[1], lastName: o[2], emailAddress: o[3]])
                }
            }
        }

        return res
    }

    /**
     * how many spectra have been imported today
     * @return
     */
    int getSpectraImportCountForToday() {
        Sql sql = Sql.newInstance(dataSource)
        int spectra = sql.firstRow("select count(*) as spectra from supports_meta_data a, spectrum b where a.id = b.id and date_trunc('day', date_created) = date_trunc('day', now())").spectra

        return spectra
    }

    /**
     * how many spectra have been imported this week
     * @return
     */
    int getSpectraImportCountForCurrentWeek() {
        Sql sql = Sql.newInstance(dataSource)
        int spectra = sql.firstRow("select count(*) as spectra from supports_meta_data a, spectrum b where a.id = b.id and date_trunc('week', date_created) = date_trunc('week', now())").spectra

        return spectra
    }

    /**
     * how many spectra have been imported this month
     * @return
     */
    int getSpectraImportCountForCurrentMonth() {
        Sql sql = Sql.newInstance(dataSource)
        int spectra = sql.firstRow("select count(*) as spectra from supports_meta_data a, spectrum b where a.id = b.id and date_trunc('month', date_created) = date_trunc('month', now())").spectra

        return spectra
    }

    /**
     * how many spectra have been imported this year
     * @return
     */
    int getSpectraImportCountForCurrentYear() {
        Sql sql = Sql.newInstance(dataSource)
        int spectra = sql.firstRow("select count(*) as spectra from supports_meta_data a, spectrum b where a.id = b.id and date_trunc('year', date_created) = date_trunc('year', now())").spectra

        return spectra
    }

    /**
     * aquires a new statistics object in the system and sends it as a job to quartz
     * @param value
     * @param title
     * @param description
     * @param category
     * @return
     */
    def acquire(Double value, String title, String description = "none", String category = "runtime") {
        Statistics statistics = new Statistics()

        statistics.category = category
        statistics.description = description
        statistics.value = value
        statistics.title = title

        statistics.save()
    }
}
