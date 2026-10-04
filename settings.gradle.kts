pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "personal-cv"

// Application module: personal content, photo, generation CLI and artifact tasks.
include("my-cv")

// Develop against a local cv-dsl checkout instead of the pinned release, for
// changes that need both repositories:
//   ./gradlew generatePdf -PcvDslPath=../cv-dsl
// The included build replaces the JitPack artifact on the plugin classpath and
// in the application dependencies alike.
providers.gradleProperty("cvDslPath").orNull?.let { path ->
    includeBuild(path) {
        dependencySubstitution {
            substitute(module("com.github.tolikttaaa:cv-dsl")).using(project(":"))
        }
    }
}
