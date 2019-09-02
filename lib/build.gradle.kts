plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    val gdxVersion: String by project
    val junitVersion: String by project

    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")

    compileOnly("org.jetbrains:annotations:17.0.0")

    testImplementation("junit:junit:$junitVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6

    sourceSets {
        main {
            resources.srcDir("src/main/java")
        }
    }
}

// Maven publishing
tasks.register<Jar>("sourcesJar") {
    dependsOn(tasks.classes)
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc.get().destinationDir!!.path)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.maltaisn"
            artifactId = "msdfgdx"
            version = rootProject.extra["libVersion"] as String
            pom {
                name.set("Card game")
                description.set("Card game base application")
                url.set("https://github.com/maltaisn/msdf-gdx")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("maltaisn")
                    }
                }
            }
            from(components["java"])
            artifact(tasks["sourcesJar"])
            //artifact(tasks["javadocJar"])
        }
    }
}
