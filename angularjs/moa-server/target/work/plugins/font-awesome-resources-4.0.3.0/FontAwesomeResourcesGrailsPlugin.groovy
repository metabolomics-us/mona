class FontAwesomeResourcesGrailsPlugin {
    // the plugin version
    def version = "4.0.3.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
    ]

    // TODO Fill in these fields
    def title = "Grails Font Awesome Resources Plugin" // Headline display name of the plugin
    def author = "Eamonn O'Connell, Alexey Zhokhov"
    def authorEmail = "eamonnoconnell@gmail.com"
    def description = '''\
Like the jquery-resources plugin that pulls in the jquery javascript lib as a resource, this plugin pulls in Font Awesome. 
Font Awesome is a very popular font based icon set. Font based icons are a very convenient means of incorporating icons into a web application and the technique is growing in popularity. 
Font Awesome probably works best when used with the twitter bootstrap UI framework for which it was designed.
From version 4.0.3.0 support for IE7 is gone, since Font Awesome does not support IE7 any longer.
'''
    def developers = [
            [name: "Eamoon O'Connell", email: "eamonnoconnell@gmail.com"],
            [name: "Alexey Zhokhov", email: ""],
            [name: "Søren Berg Glasius", email: "soeren@glasius.dk"]
    ]

    // URL to the plugin's documentation
    def documentation = "https://github.com/halfbaked/grails-font-awesome-resources"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "GitHub", url: "https://github.com/halfbaked/grails-font-awesome-resources/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/halfbaked/grails-font-awesome-resources" ]

}
