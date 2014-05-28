/**
 * defines all our java script modules
 */
modules = {

    'app-js' {

        /**
         * font awesome
         */
        dependsOn 'font-awesome'    //font awesome library

        /**
         * all the angular mods
         */
        dependsOn 'angular'         //base angular modules

        /**
         * bootstrap css styling
         */
        dependsOn 'bootstrap-css'       //bootstrap css library

        /**
         * angular bootstrap implementation
         */
        //bootstrap ui
        resource url: "http://cdnjs.cloudflare.com/ajax/libs/angular-ui-bootstrap/0.10.0/ui-bootstrap-tpls.js"
        //angular bootstrap library

        /**
         * chem doodle framework to draw fancy molecules
         */
        resource url: "http://hub.chemdoodle.com/cwc/latest/ChemDoodleWeb.js"
        resource url: "http://hub.chemdoodle.com/cwc/latest/ChemDoodleWeb.css"
        resource url: "http://hub.chemdoodle.com/cwc/latest/uis/ChemDoodleWeb-uis.js"

        /**
         * application resources
         */

        //our js resources, which need to be in order
        resource url: 'js/app.js'

        //all defined routes
        resource url: 'js/routes.js'

        //all our services
        resource url: 'js/services.js'

        //all our controllers
        resource url: 'js/controllers.js'

        //all our fitlers
        resource url: 'js/filters.js'

        //all our directives
        resource url: 'js/directives.js'
    }

    'app' {
        dependsOn 'angular-all'  // enable this, as a sample ...
        dependsOn 'app-js'  // application-specific angular-related scripts ...
    }
}