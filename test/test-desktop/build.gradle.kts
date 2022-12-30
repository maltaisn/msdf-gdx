plugins {
    kotlin("jvm")
}

val assetsDir = file("../assets")
val mainClassName = "com.maltaisn.msdfgdx.test.desktop.DesktopLauncher"

dependencies {
    val gdxVersion: String by project

    implementation(project(":test:test-core"))

    implementation(kotlin("stdlib"))

    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.register<JavaExec>("run") {
    main = mainClassName
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
    workingDir = assetsDir
    isIgnoreExitValue = true

    if ("mac" in System.getProperty("os.name").toLowerCase()) {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.register<Jar>("dist") {
    from(files(sourceSets.main.get().output.classesDirs))
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(assetsDir)

    manifest {
        attributes["Main-Class"] = mainClassName
    }
}
