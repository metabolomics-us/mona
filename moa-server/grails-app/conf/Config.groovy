grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.app.context = "/";
// The ACCEPT header will not be used for content negotiation for user agents containing the following strings (defaults to the 4 major rendering engines)
grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
grails.mime.types = [ // the first one is the default format
                      all          : '*/*', // 'all' maps to '*' or the first available format in withFormat
                      atom         : 'application/atom+xml',
                      css          : 'text/css',
                      csv          : 'text/csv',
                      form         : 'application/x-www-form-urlencoded',
                      html         : ['text/html', 'application/xhtml+xml'],
                      js           : 'text/javascript',
                      json         : ['application/json', 'text/json'],
                      multipartForm: 'multipart/form-data',
                      rss          : 'application/rss+xml',
                      text         : 'text/plain',
                      hal          : ['application/hal+json', 'application/hal+xml'],
                      xml          : ['text/xml', 'application/xml']
]
grails.mime.use.accept.header = true
// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// Legacy setting for codec used to encode data with ${}
grails.views.default.codec = "html"

// The default scope for controllers. May be prototype, session or singleton.
// If unspecified, controllers are prototype scoped.
grails.controllers.defaultScope = 'singleton'

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside ${}
                scriptlet = 'html' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}

grails.converters.encoding = "UTF-8"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the convert method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

logdirectory = "./"

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console appender:
    //
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '[%t] [%-5c] [%p] [%d{HH:mm:ss}] [%m]%n'), threshold: org.apache.log4j.Level.INFO
    }

    root {
        info 'stdout'
    }

    //info file: 'grails.app'
    debug 'grails.app'


    error 'org.codehaus.groovy.grails.web.mapping',        // URL mapping
            'org.codehaus.groovy.grails.web.pages',          // GSP
            'org.codehaus.groovy.grails.web.sitemesh',       // layouts
            'org.codehaus.groovy.grails.commons',            // core / classloading
            'org.springframework',
            'org.codehaus.groovy.grails.orm.hibernate'      // hibernate integration

    debug 'org.codehaus.groovy.grails.web.servlet',        // controllers
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.plugins'            // plugins
    //'org.hibernate',
    //'net.sf.ehcache.hibernate',
    //'org.quartz.plugins.history.LoggingTriggerHistoryPlugin'

    //warn   'org.quartz.plugins.history.LoggingJobHistoryPlugin'

    //debug 'org.hibernate'

    environments {

        test {
            debug 'stdout', additivity: false
            debug stdout: ['grails.app', 'grails.app.services', 'util.query', 'moa.server.query'], additivity: false

            error 'org.springframework.security', 'grails.app.resourceMappers'
        }

        development {
            debug 'util.chemical'
            debug 'curation'
            debug 'moa'
            //debug 'grails.plugin.cache'

            info 'org.hibernate.SQL'
            info 'org.hibernate.type.descriptor.sql.BasicBinder'
        }

        production {
            info stdout:
                    'grails.app'
        }
    }
}

//let's use jquery
grails.views.javascript.library = "jquery"

grails.cache.config = {
    cache {
        name 'spectrum'
    }
    cache {
        name 'compound'
    }
    cache {
        name 'metadata'
    }
    cache {
        name 'tag'
    }
}


grails.cache.enabled = false

grails.cache.keyGenerator = "cacheKey"

// Set additional config locations
grails.config.locations = []
grails.config.locations << SpringSecurityConfig

cors.enabled = true
cors.url.pattern = '/rest/*'
cors.headers = [
        'Access-Control-Allow-Origin'     : '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Allow-Headers'    : 'origin, authorization, accept, content-type, x-requested-with, X-Auth-Token',
        'Access-Control-Allow-Methods'    : 'GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS',
        'Access-Control-Max-Age'          : 3600
]

boolean queuesAreDurable = false

environments {
    development {
        rabbitmq {
            connection = {
                connection host: "localhost", username: "guest", password: "guest", threads: Runtime.getRuntime().availableProcessors() - 2
            }
        }

        grails.logging.jul.usebridge = true
        grails.converters.default.pretty.print = true

        logdirectory = "/var/log/mona/"
        queryDownloadDirectory = './query_export/'


        repository {
            //where to dump the data
            directory = "./repository"

            //timeframe of days we dump the uploaded data from
            timeframeInDays = 7

        }

    }

    test {
        rabbitmq {
            connection = {
                connection host: "localhost", username: "guest", password: "guest", threads: 5
            }
        }
        grails.logging.jul.usebridge = true
        grails.converters.default.pretty.print = true

        logdirectory = "/var/log/mona/"
        queryDownloadDirectory = './query_export/'

    }

    production {

        //we don't want to loose our queues in case of a crash
        queuesAreDurable = true

        rabbitmq {
            connection = {
                connection host: "gose.fiehnlab.ucdavis.edu", username: "mona", password: "mona", threads: Runtime.getRuntime().availableProcessors() - 2
            }
        }
        grails.logging.jul.usebridge = false
        //  grails.serverURL = "http://mona.fiehnlab.ucdavis.edu"

        logdirectory = "/data/log/"
        queryDownloadDirectory = "/data/export/"

        repository {
            //where to dump the data
            directory = "/data/repository"

            //timeframe of days we dump the uploaded data from
            timeframeInDays = 7

        }

    }
}

//rabbit mq configuration for our different enviornments

rabbitmq {
    queues = {
        queue name: "mona.validate.spectra", arguments: ["x-max-priority", 6], durable: queuesAreDurable
        queue name: "mona.import.spectra", arguments: ["x-max-priority", 9], durable: queuesAreDurable
        queue name: "mona.association.spectra", arguments: ["x-max-priority", 2], durable: queuesAreDurable
        queue name: "mona.export.spectra", arguments: ["x-max-priority", 9], durable: queuesAreDurable
        queue name: "mona.validate.compound", arguments: ["x-max-priority", 8], durable: queuesAreDurable
        queue name: "mona.repository.export.spectra", arguments: ["x-max-priority", 8], durable: queuesAreDurable
    }
}


