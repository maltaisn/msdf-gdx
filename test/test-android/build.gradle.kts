plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    buildToolsVersion = "33.0.1"
    compileSdk = 33
    namespace = "com.example.android"
    defaultConfig {
        applicationId = "com.maltaisn.msdfgdx.test.android"
        minSdk = 14
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets {
        named("main") {
            java.srcDir("src/main/kotlin")  // Not necessary but works better with IntelliJ
        }
    }
}

val natives: Configuration by configurations.creating

dependencies {
    val gdxVersion: String by project

    implementation(project(":test:test-core"))

    implementation(kotlin("stdlib"))

    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")

    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
}

// Called every time gradle gets executed, takes the native dependencies of
// the natives configuration, and extracts them to the proper libs/ folders
// so they get packed with the APK.
tasks.register("copyAndroidNatives") {
    doFirst {
        val jniLibsPath = android.sourceSets.named("main").get().jniLibs.srcDirs.last().path
        natives.files.forEach { jar ->
            val nativeName = jar.nameWithoutExtension.substringAfterLast("natives-")
            val outputDir = File(jniLibsPath, nativeName)
            outputDir.mkdirs()
            copy {
                from(zipTree(jar))
                into(outputDir)
                include("*.so")
            }
        }
    }
}
tasks.whenTaskAdded {
    if ("package" in name) {
        dependsOn("copyAndroidNatives")
    }
}


// Task to copy the assets to the android module assets dir
val assetsPath = android.sourceSets.named("main").get().assets.srcDirs.last().path

tasks.register("copyAssets") {
    file(assetsPath).mkdirs()
    copy {
        from("../assets")
        into(assetsPath)
    }
}

tasks.register<Delete>("cleanAssets") {
    delete(assetsPath)
}

tasks.named("clean") {
    finalizedBy("cleanAssets")
}

tasks.named("build") {
    finalizedBy("copyAssets")
}
