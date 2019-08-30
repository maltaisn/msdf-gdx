buildscript {
    val libVersion by extra("0.1.0")

    val kotlinVersion by extra("1.3.50")
    val gdxVersion by extra("1.9.10")
    val ktxVersion by extra("1.9.10-b1")
    val junitVersion by extra("4.12")

    repositories {
        gradlePluginPortal()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.0")
        classpath(kotlin("gradle-plugin", kotlinVersion))
    }
}

plugins {
    base
}

allprojects {
    repositories {
        jcenter()
        google()
    }
}
