import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class PublishConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val tag = "local-kmp-42" // TODO RACOSTA
//        val tag = "git describe --tags --abbrev=0".runCommand(target)
        println("RACOSTA ${project.name} TAG = $tag")

        pluginManager.apply("com.vanniktech.maven.publish")
        extensions.configure<MavenPublishBaseExtension> {
            @Suppress("UnstableApiUsage")
            configureBasedOnAppliedPlugins()

            coordinates(
                groupId = "io.github.raamcosta.compose-destinations",
                artifactId = project.name,
                version = tag,
            )

            publishToMavenCentral(SonatypeHost.S01)

            signAllPublications()

            pom {
                name.set(project.name)
                description.set("Annotation processing library for type-safe Jetpack Compose navigation with no boilerplate.")
                inceptionYear.set("2021")
                url.set("https://github.com/raamcosta/compose-destinations")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("raamcosta")
                        name.set("Rafael Costa")
                        url.set("https://github.com/raamcosta")
                    }
                }
                scm {
                    url.set("https://github.com/raamcosta/compose-destinations.git")
                    connection.set("scm:git@github.com:raamcosta/compose-destinations.git")
                    developerConnection.set("scm:git@github.com:raamcosta/compose-destinations.git")
                }
            }
        }
    }

    fun String.runCommand(project: Project): String {
        val byteOut = java.io.ByteArrayOutputStream()
        project.exec {
            commandLine = this@runCommand.split("\\s".toRegex())
            standardOutput = byteOut
        }
        return String(byteOut.toByteArray()).trim()
    }
}