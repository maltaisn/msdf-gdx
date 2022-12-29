plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}

dependencies {
    val gdxVersion: String by project
    val junitVersion: String by project

    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")

    compileOnly("org.jetbrains:annotations:17.0.0")

    testImplementation("junit:junit:$junitVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7

    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            val libGroup: String by project
            val libVersion: String by project
            groupId = libGroup
            version = libVersion
            artifactId = "msdf-gdx"

            pom {
                name.set("msdf-gdx")
                description.set("Provides lightweight utilities to draw MSDF text on LibGDX.")
                url.set("https://github.com/maltaisn/msdf-gdx")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("maltaisn")
                    }
                }
                scm {
                    url.set("https://github.com/maltaisn/msdf-gdx")
                    connection.set("scm:git:git://github.com/maltaisn/msdf-gdx.git")
                    developerConnection.set("scm:git:ssh://git@github.com:maltaisn/msdf-gdx.git")
                }
            }

            from(components["java"])
        }
    }
    repositories {
        maven {
            val ossrhUsername: String by project
            val ossrhPassword: String by project
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                this.username = ossrhUsername
                this.password = ossrhPassword
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
