buildscript {
    val kotlinVersion: String by project
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
    }
}

plugins {
    base
    id("io.codearte.nexus-staging") version "0.30.0"
}

allprojects {
    repositories {
        jcenter()
        google()
        mavenCentral()
    }
}

tasks.named("clean") {
    delete(project.buildDir)
}

nexusStaging {
    val libGroup: String by project
    val ossrhUsername: String by project
    val ossrhPassword: String by project
    packageGroup = libGroup
    username = ossrhUsername
    password = ossrhPassword
}
