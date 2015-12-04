package util.query

import grails.util.Environment
import moa.query.Query
import moa.server.query.PredefinedQueryService
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 6/17/15
 * Time: 2:38 PM
 */
class StaticQueries {
    private static Logger logger = Logger.getLogger(getClass())

    static void register() {
        if(Environment.current == Environment.PRODUCTION) {
            logger.info("Generating query tree definitions")

            defineGeneralQueries()
            defineGCMSQueries()
            defineLCMSQueries()
        }

        if(Environment.current == Environment.DEVELOPMENT) {
            logger.info("Generating testing query tree definitions")

            if(Query.findByLabel("Test") == null) {
                def x = Query.findOrSaveByLabelAndDescriptionAndQuery("Test", "test",
                        "{\"compound\": {},\"metadata\": [{\"name\": \"accession\", \"value\": {\"eq\": \"AU102001\"}}], \"tags\": []}")
            }
        }
    }

    private static void save(String label, String description, String query) {
        if(Query.findByLabel(label) == null){
            Query.findOrSaveByLabelAndDescriptionAndQuery(label, description, query)
        }
    }


    private static void defineGeneralQueries() {
        save("all spectra", "all spectra", """{
    "compound": {},
    "metadata": [],
    "tags": []
}""")
    }


    private static void defineGCMSQueries() {
        String label = "GCMS"
        List instrumentType = ["GC-EI-TOF", "CI-B", "GC-EI-QQ", "EI-B"]


        save("${label}", "${label} spectra", """{
    "compound": {},
    "metadata": [],
    "tags": [
        {
            "name": {
                "eq": "${label}"
            }
        }
    ]
}""")

        // derivatized
        save("${label} - derivatized", "derivatized ${label} spectra", """{
    "compound": {},
    "metadata": [
        {
            "name": "derivative type",
            "value": {
                "like": "%"
            }
        }
    ],
    "tags": [
        {
            "name": {
                "eq": "${label}"
            }
        }
    ]
}""")

        instrumentType.each { t ->
            // with derivative type
            save("${label} - derivatized - ${t}", "derivatized ${label} spectra with instrument type ${t}", """{
    "compound": {},
    "metadata": [
        {
            "name": "derivative type",
            "value": {
                "like": "%"
            }
        },
        {
            "name": "instrument type",
            "value": {
                "eq": "${t}"
            }
        }
    ],
    "tags": [
        {
            "name": {
                "eq": "${label}"
            }
        }
    ]
}""")

            // without derivative type
            save("${label} - ${t}", "${label} spectra with instrument type ${t}", """{
    "compound": {},
    "metadata": [
        {
            "name": "instrument type",
            "value": {
                "eq": "${t}"
            }
        }
    ],
    "tags": [
        {
            "name": {
                "eq": "${label}"
            }
        }
    ]
}""")
        }
    }


    private static void defineLCMSQueries() {
        String label = "LCMS"
        List mode = ["virtual", "experimental"]
        List acq = ["positive", "negative"]

        save("${label}", "${label} spectra", """{
    "compound": {},
    "metadata": [],
    "tags": [
        {
            "name": {
                "eq": "${label}"
            }
        }
    ]
}""")

        /**
         * build different modes
         */
        mode.each { m ->
            save("${label} - ${m}", "${m} ${label} spectra", """{
    "compound": {},
    "metadata": [],
    "tags": [
        {
            "name": {
                "eq": "${label}"
            }
        },
        {
            "name": {
                "eq": "${m}"
            }
        }
    ]
}""")

            /**
             * different acquisitions
             */
            acq.each { a ->
                //with mode
                save("${label} - ${m} - ${a}", "${a} mode ${m} ${label} spectra", """{
    "compound": {},
    "metadata": [
        {
            "name": "ion mode",
            "value": {
            "eq": "${a}"
            }
        }
    ],
    "tags": [
        {
            "name": {
                "eq": "${label}"
            }
        },
        {
            "name": {
                "eq": "${m}"
            }
        }
    ]
}""")
            }
        }

        acq.each {a ->
            //without mode
            save("${label} - ${a}","${a} mode ${label} spectra", """{
    "compound": {},
    "metadata": [
        {
            "name": "ion mode",
            "value": {
            "eq": "${a}"
            }
        }
    ],
    "tags": [
        {
            "name": {
                "eq": "${label}"
            }
        }
    ]
}""")
        }
    }
}
