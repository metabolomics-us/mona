grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
grails.project.war.file = "target/mona-server.war"

//grails.server.port.http = 9090
grails.project.fork = [
        // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
        //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

        // configure settings for the test-app JVM, uses the daemon by default
//        test   : [maxMemory: 8192, minMemory: 64, debug: false, maxPerm: 256, daemon: true],
        test: false,
        // configure settings for the run-app JVM
        run    : [maxMemory: 8192, minMemory: 8192, debug: false, maxPerm: 256, forkReserve: false],
        // configure settings for the run-war JVM
        war    : [maxMemory: 8192, minMemory: 64, debug: false, maxPerm: 256, forkReserve: false],
        // configure settings for the Console UI JVM
        console: [maxMemory: 8192, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails" default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
    }
    log "error" // log level of Ivy resolver, either "error", "warn", "info", "debug" or "verbose"
    checksums true // Whether to verify checksums on resolve

    legacyResolve false
    // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        grailsPlugins()
        grailsHome()
        mavenLocal()
        grailsCentral()
        mavenCentral()
        mavenRepo "http://repo.fiehnlab.ucdavis.edu:55000/content/groups/public"

        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://maven.antelink.com/content/repositories/central/"
        mavenRepo "http://ambit.uni-plovdiv.bg:8083/nexus/content/repositories/public/"
        mavenRepo "http://ambit.uni-plovdiv.bg:8083/nexus/content/repositories/thirdparty"
        mavenRepo "http://jni-inchi.sourceforge.net/m2repo"

        // repo for spring security rest
        mavenRepo 'http://repo.spring.io/milestone'

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
    }


    def cdkVersion = "1.4.16"
    dependencies {
        // specify dependencies here under either "build", "compile", "runtime", "test" or "provided" scopes e.g.
        // runtime "mysql:mysql-connector-java:5.1.27"
        // runtime "org.postgresql:postgresql:9.3-1100-jdbc41"
        runtime "postgresql:postgresql:9.1-901-1.jdbc4"


        compile("xmlpull:xmlpull:1.1.3.1")

        compile("org.openscience.cdk:cdk-fingerprint:${cdkVersion}") {
            transitive = false
        }
        compile("org.openscience.cdk:cdk-inchi:${cdkVersion}") {
            transitive = false
        }
        compile("org.openscience.cdk:cdk-standard:${cdkVersion}")
        compile("org.openscience.cdk:cdk-interfaces:${cdkVersion}")
        compile("org.openscience.cdk:cdk-annotation:${cdkVersion}")
        compile("org.openscience.cdk:cdk-io:${cdkVersion}")
        compile("org.openscience.cdk:cdk-isomorphism:${cdkVersion}")
        compile("org.openscience.cdk:cdk-render:${cdkVersion}")
        compile("org.openscience.cdk:cdk-renderbasic:${cdkVersion}")
        compile("org.openscience.cdk:cdk-renderawt:${cdkVersion}")
        compile("org.openscience.cdk:cdk-smarts:${cdkVersion}")  {
            transitive = false
        }

        compile("org.openscience.cdk:cdk-extra:${cdkVersion}")  {
            transitive = false
        }
        compile("org.openscience.cdk:cdk-dict:${cdkVersion}")  {
            transitive = false
        }

        compile("jama:jama:1.0.2")  {
            transitive = false
        }

        compile("org.openscience.cdk:cdk-formula:${cdkVersion}")
        compile("org.openscience.cdk:cdk-smsd:${cdkVersion}")
        compile("org.openscience.cdk:cdk-qsarmolecular:${cdkVersion}")   {
            excludes "xercesImpl","xmlParserAPIs"
        }

        compile("xpp3:xpp3:1.1.4c")
        compile("java3d:vecmath:1.3.1")
        compile("net.sf.jni-inchi:jni-inchi:0.7")

	    compile("com.fasterxml.jackson.core:jackson-core:2.3.2")
	    compile("com.fasterxml.jackson.core:jackson-databind:2.3.0")
	    compile("com.github.fge:json-schema-validator:2.2.6")

        compile("edu.ucdavis.fiehnlab.splash:java:1.0-SNAPSHOT")

        compile("javax.mail:mail:1.4")
        compile("com.sun.mail:smtp:1.5.4")
    }

    plugins {
        //compile ":tomcat:8.0.18"

        // plugins for the build system only
        //build ":tomcat:7.0.50"
        compile ":jetty:2.0.3"

        // plugins for the compile step
        compile ":hibernate4:4.3.5.2"
        compile ":rest-client-builder:2.1.1"

        compile ":quartz:1.0.3-SNAPSHOT"
        compile ":cache:1.1.8"
        compile ":url-mappings-generator:0.1"

        // plugins needed at runtime but not for compilation
        runtime ":database-migration:1.4.1-SNAPSHOT"
        runtime ":resources:1.2.8"
        runtime ":jquery:1.11.1"
        runtime ':twitter-bootstrap:3.3.4'

        // spring security
        compile ":spring-security-core:2.0-RC5"
        compile ":spring-security-rest:1.5.1"
        /*
        , {
            excludes "spring-security-core", "jackson-core"
        }
        */

        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0.1"
        //runtime ":cached-resources:1.1"
        //runtime ":yui-minify-resources:0.1.5"
    }
}

grails.jvmArgs = ["-Xms1024m", "-Xmx8192m", "-XX:MaxPermSize=512m"]
grails.tomcat.jvmArgs = ["-Xms1024m", "-Xmx8192m", "-XX:MaxPermSize=512m"]
