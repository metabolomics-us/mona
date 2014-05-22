grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6

grails.project.dependency.resolution = {
    inherits 'global'
   	log 'warn'

   	repositories {
   		grailsCentral()
   		mavenLocal()
   		mavenCentral()
   	}
    dependencies {
    }

    plugins {
        build(':release:3.0.1', ':rest-client-builder:2.0.0') {
            export = false
        }
        runtime(":resources:1.2.1") {
            export = false
        }
    }
}
