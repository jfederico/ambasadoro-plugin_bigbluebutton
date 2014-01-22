//grails.project.class.dir = "target/classes"
//grails.project.test.class.dir = "target/test-classes"
//grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target"

grails.plugin.location.ambasadoro = "../ambasadoro-plugin"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'[A
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility
    repositories {
        grailsCentral()
        mavenCentral()
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "https://raw.github.com/blindsidenetworks/bigbluebutton-api/mvn-repo/"
        mavenRepo "https://raw.github.com/blindsidenetworks/lti/mvn-repo/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.21'
        runtime   "org.bigbluebutton:bigbluebutton-api:1.0.5"
        runtime   "org.lti:lti:1.0.4"
    }

    plugins {
        //build(":tomcat:$grailsVersion",
        build(":release:2.2.1",
              ":rest-client-builder:1.0.3") {
            export = false
        }
    }
}
