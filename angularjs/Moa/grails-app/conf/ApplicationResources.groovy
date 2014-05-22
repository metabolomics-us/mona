/**
 * defines all our java script modules
 */
modules = {

    'app-js' {
        dependsOn 'font-awesome'    //font awesome library
        dependsOn 'angular'         //base angular modules
        dependsOn 'bootstrap'       //bootstrap css library

        resource url: "http://cdnjs.cloudflare.com/ajax/libs/angular-ui-bootstrap/0.10.0/ui-bootstrap-tpls.js"   //angular bootstrap library

        //our js resources, which need to be in order
        resource url:'js/app.js'

        //all defined routes
        resource url:'js/routes.js'

        //all our services
        resource url:'js/services.js'

        //all our controllers
        resource url:'js/controllers.js'

        //all our fitlers
        resource url:'js/filters.js'

        //all our directives
        resource url:'js/directives.js'
    }

    'app' {
        dependsOn 'angular-all'  // enable this, as a sample ...
        dependsOn 'app-js'  // application-specific angular-related scripts ...
    }
}