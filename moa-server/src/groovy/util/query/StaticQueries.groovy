package util.query

import moa.query.Query
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 6/17/15
 * Time: 2:38 PM
 */
class StaticQueries {

    static void register(){

        lcms()
    }


    private static void save(String label, String description, String query){

        if(Query.findByLabel(label) == null){
            Query.findOrSaveByLabelAndDescriptionAndQuery(label,description,query)
        }

    }

    private static  void lcms(){


        List mode = ["virtual", "experimental"]

        List acq = ["positive", "negative"]

        String label = "LCMS"


        save("${label}","a predefined query returning all ${label} spectra from the system","""

            {
  "compound": {},
  "metadata": [],
  "tags": [
    {
      "name": {
        "eq": "${label}"
      }
    }
  ]
  }
            """)

        /**
         * build different modes
         */
        mode.each { m ->

            save("${label} - ${m}","a predefined query returning all ${m} ${label} spectra from the system","""

            {
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
  }
            """)

            /**
             * different acquistions
             */
            acq.each { a ->

                //with mode
                save("${label} - ${m} - ${a}","a predefined query returning all ${a} mode ${m} ${label} spectra from the system","""

            {
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
    ,
    {
      "name": {
        "eq": "${m}"
      }
    }
  ]
  }
            """)


            }
        }

        acq.each {a ->


            //without mode
            save("${label} - ${a}","a predefined query returning all ${a} mode ${label} spectra from the system","""

            {
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
  }
            """)
        }
    }
}
