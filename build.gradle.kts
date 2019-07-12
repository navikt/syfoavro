import java.util.Date
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZoneId

import com.commercehub.gradle.plugin.avro.DefaultAvroExtension
import de.marcphilipp.gradle.nexus.NexusPublishExtension

plugins {
    java
    `maven-publish`
    signing
    id("com.commercehub.gradle.plugin.avro") version "0.16.0" apply false
    id("io.codearte.nexus-staging") version "0.21.0"
    id("de.marcphilipp.nexus-publish") version "0.2.0" apply false
}

version = System.getenv("CIRCLE_SHA1") ?: "local-build"

nexusStaging {
    packageGroup = "no.nav"
    username = System.getenv("SONATYPE_USERNAME")
    password = System.getenv("SONATYPE_PASSWORD")
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.commercehub.gradle.plugin.avro")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "de.marcphilipp.nexus-publish")

    group = "no.nav.syfo.schemas"
    version = parent!!.version

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        implementation("org.apache.avro:avro:1.8.2")
    }

    configure<NexusPublishExtension> {
        username.set(System.getenv("SONATYPE_USERNAME"))
        password.set(System.getenv("SONATYPE_PASSWORD"))
    }

    configure<DefaultAvroExtension> {
        fieldVisibility = "PRIVATE"
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.register<Jar>("sourcesJar") {
        from(sourceSets.main.get().allJava)
        archiveClassifier.set("sources")
    }

    tasks.register<Jar>("javadocJar") {
        from(tasks.javadoc)
        archiveClassifier.set("javadoc")
    }

    publishing {
        repositories {
            maven {
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                this.credentials {
                    username = System.getenv("SONATYPE_USERNAME")
                    password = System.getenv("SONATYPE_PASSWORD")
                }
            }
        }
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(tasks.getByName("sourcesJar"))
                artifact(tasks.getByName("javadocJar"))

                pom {
                    name.set("syfo avro schemas")
                    description.set("Common functionality used between the syfo mottak apps")
                    url.set("https://github.com/navikt/syfoavro")

                    organization {
                        name.set("NAV (Arbeids- og velferdsdirektoratet) - The Norwegian Labour and Welfare Administration")
                        url.set("https://www.nav.no/")
                    }

                    developers {
                        developer {
                            organization.set("NAV (Arbeids- og velferdsdirektoratet) - The Norwegian Labour and Welfare Administration")
                            organizationUrl.set("https://www.nav.no/")
                        }
                    }

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    scm {
                        connection.set("scm:git:https://github.com/navikt/syfoavro.git")
                        developerConnection.set("scm:git:https://github.com/navikt/syfoavro.git")
                        url.set("https://github.com/navikt/syfoavro")
                    }
                }
            }
        }
    }

    signing {
        useGpgCmd()
        sign(publishing.publications["mavenJava"])
    }
}
