package util.query

import grails.util.Environment
import moa.query.Query
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
        if(Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST) {
            logger.info("Generating testing query tree definitions")

            if(Query.findByLabel("Test") == null) {
                Query.findOrSaveByLabelAndDescriptionAndQuery("Test", "test",
                        "{\"compound\": {},\"metadata\": [{\"name\": \"accession\", \"value\": {\"eq\": \"AU102001\"}}], \"tags\": []}")
            }
        } else if(Environment.current == Environment.PRODUCTION) {
            logger.info("Generating query tree definitions")

            defineGeneralQueries()
            defineGCMSQueries()
            defineExperimentalLCMSQueries()
            defineVirtualLCMSQueries()
        }


    }

    private static void save(String label, String description, String query) {
        if(Query.findByLabel(label) == null){
            Query.findOrSaveByLabelAndDescriptionAndQuery(label, description, query)
        }
    }


    private static void defineGeneralQueries() {
        save("All Spectra", "All Spectra", """{
    "compound": {},
    "metadata": [],
    "tags": []
}""")

        save("Libraries - MassBank", "MassBank", """{
    "compound": {},
    "metadata": [],
    "tags": [
        {
            "name": { "eq": "massbank" }
        }
    ]
}""")

        save("Libraries - RESPECT", "RESPECT", """{
    "compound": {},
    "metadata": [],
    "tags": [
        {
            "name": { "eq": "respect" }
        }
    ]
}""")

        save("Libraries - HMDB", "HMDB", """{
    "compound": {},
    "metadata": [],
    "tags": [
        {
            "name": { "eq": "hmdb" }
        }
    ]
}""")

        save("Libraries - GNPS", "GNPS", """{
    "compound": {},
    "metadata": [],
    "tags": [
        {
            "name": { "eq": "gnps" }
        }
    ]
}""")



        save("Libraries - LipidBlast", "LipidBlast", """{
    "compound": {},
    "metadata": [],
    "tags": [
        {
            "name": { "eq": "lipidblast" }
        }
    ]
}""")

        save("Libraries - FAHFA", "FAHFA", """{
    "compound": {},
    "metadata": [],
    "tags": [
        {
            "name": { "eq": "FAHFA" }
        }
    ]
}""")
    }


    private static void defineGCMSQueries() {
        String label = "GC-MS"
        String label_tag = "GCMS"
        List ionMode = ["positive", "negative"]
        List ionizationType = ["CI", "EI"]


        save("${label}", "${label} spectra", """{
    "compound": {},
    "metadata": [],
    "tags": [
        {
            "name": { "eq": "${label_tag}" }
        }
    ]
}""")

        // ion mode
        ionMode.each { mode ->
            save("${label} - ${mode.capitalize()}", "${mode.capitalize()} mode ${label} spectra", """{
    "compound": {},
    "metadata": [
        {
            "name": "ion mode",
            "value": { "eq": "${mode}" }
        }
    ],
    "tags": [
        {
            "name": { "eq": "${label_tag}" }
        }
    ]
}""")
        }

        ionizationType.each { type ->
            // with ion mode
            ionMode.each { mode ->
                save("${label} - ${type} - ${mode.capitalize()}", "${mode.capitalize()} mode ${label} spectra with instrument type ${type}", """{
    "compound": {},
    "metadata": [
        {
            "name": "ion mode",
            "value": { "eq": "${mode}" }
        },
        {
            "name": "instrument type",
            "value": { "like": "%${type}%" }
        }
    ],
    "tags": [
        {
            "name": { "eq": "${label_tag}" }
        }
    ]
}""")
            }

            // without ion mode
            save("${label} - ${type}", "${label} spectra with instrument type ${type}", """{
    "compound": {},
    "metadata": [
        {
            "name": "instrument type",
            "value": { "like": "%${type}%" }
        }
    ],
    "tags": [
        {
            "name": { "eq": "${label_tag}" }
        }
    ]
}""")
        }
    }


    private static void defineExperimentalLCMSQueries() {
        String label = "LC-MS"
        String label_tag = "LCMS"
        List ionMode = ["positive", "negative"]
        List ionizationType = ["CI", "ESI"]

        save("${label}", "${label} spectra", """{
    "compound": {},
    "metadata": [],
    "tags": [
        {
            "name": { "eq": "${label_tag}" }
        }
    ]
}""")

        // ms/ms
        save("${label} - MS/MS", "${label}/MS spectra", """{
    "compound": {},
    "metadata": [
        {
            "name": "ms level",
            "value": { "eq": "MS2" }
        }
    ],
    "tags": [
        {
            "name": { "eq": "${label_tag}" }
        }
    ]
}""")

        // ion mode
        ionMode.each { mode ->
            save("${label} - ${mode.capitalize()}", "${mode.capitalize()} mode ${label} spectra", """{
    "compound": {},
    "metadata": [
        {
            "name": "ion mode",
            "value": { "eq": "${mode}" }
        }
    ],
    "tags": [
        {
            "name": { "eq": "${label_tag}" }
        }
    ]
}""")

            // ms/ms
            save("${label} - MS/MS - ${mode.capitalize()}", "${mode.capitalize()} mode ${label}/MS spectra", """{
    "compound": {},
    "metadata": [
        {
            "name": "ms level",
            "value": { "eq": "MS2" }
        },
        {
            "name": "ion mode",
            "value": { "eq": "${mode}" }
        }
    ],
    "tags": [
        {
            "name": { "eq": "${label_tag}" }
        }
    ]
}""")
        }

        ionizationType.each { type ->
            // with ion mode
            ionMode.each { mode ->
                save("${label} - ${type} - ${mode.capitalize()}", "${mode.capitalize()} mode ${label} spectra with instrument type ${type}", """{
    "compound": {},
    "metadata": [
        {
            "name": "ion mode",
            "value": { "eq": "${mode}" }
        },
        {
            "name": "instrument type",
            "value": { "like": "%${type}%" }
        }
    ],
    "tags": [
        {
            "name": { "eq": "${label_tag}" }
        }
    ]
}""")
            }

            // without ion mode
            save("${label} - ${type}", "${label} spectra with instrument type ${type}", """{
    "compound": {},
    "metadata": [
        {
            "name": "instrument type",
            "value": { "like": "%${type}%" }
        }
    ],
    "tags": [
        {
            "name": { "eq": "${label_tag}" }
        }
    ]
}""")
        }
    }


    private static void defineVirtualLCMSQueries() {
        String label = "LC-MS"
        String label_tag = "LCMS"
        List ionMode = ["positive", "negative"]

        save("In-silico ${label}", "In-silico ${label} spectra", """{
    "compound": {},
    "metadata": [],
    "tags": [
        {
            "name": { "eq": "${label_tag}" },
            "name": { "eq": "virtual" }
        }
    ]
}""")

        // ion mode
        ionMode.each { mode ->
            save("In-silico ${label} - ${mode.capitalize()}", "In-silico ${mode.capitalize()} mode ${label} spectra", """{
    "compound": {},
    "metadata": [
        {
            "name": "ion mode",
            "value": { "eq": "${mode}" }
        }
    ],
    "tags": [
        {
            "name": { "eq": "${label_tag}" },
            "name": { "eq": "virtual" }
        }
    ]
}""")
        }
    }
}
