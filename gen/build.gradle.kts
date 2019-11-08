import proguard.gradle.ProGuardTask
import java.util.*

plugins {
    kotlin("jvm")
}

val mainClassName = "com.maltaisn.msdfgdx.gen.MainKt"

dependencies {
    val junitVersion: String by project
    val gdxVersion: String by project
    val coroutinesVersion: String by project
    val jcommanderVersion: String by project
    val pngtasticVersion: String by project

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("com.beust:jcommander:$jcommanderVersion")
    implementation("com.badlogicgames.gdx:gdx-tools:$gdxVersion")
    implementation("com.github.depsypher:pngtastic:$pngtasticVersion")

    testImplementation("junit:junit:$junitVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6
}

tasks.register<JavaExec>("run") {
    main = mainClassName
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
    isIgnoreExitValue = true

    val properties = Properties()
    properties.load(project.rootProject.file("local.properties").inputStream())
    setArgsString(properties.getProperty("gen-test-args"))

    if ("mac" in System.getProperty("os.name").toLowerCase()) {
        jvmArgs("-XstartOnFirstThread")
    }
}

// Use this task to create a fat jar.
// The jar file is generated in test/test-desktop/build/libs
tasks.register<Jar>("dist") {
    from(files(sourceSets.main.get().output.classesDirs))
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    manifest {
        attributes["Main-Class"] = mainClassName
    }
    finalizedBy(tasks.named("shrinkJar"))
}

tasks.register<ProGuardTask>("shrinkJar") {
    configuration("proguard-rules.pro")
    injars("build/libs/gen.jar")
    outjars("build/libs/gen-release.jar")
    libraryjars("${System.getProperty("java.home")}/lib/rt.jar")
    libraryjars(configurations.runtimeClasspath.get().files)
}
