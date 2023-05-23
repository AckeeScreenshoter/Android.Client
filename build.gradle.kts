plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.dokka) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.nexus.staging)
    id("build-logic-plugin")
}

//buildscript {
//    extra["kotlin_version"] = "1.8.21"
//    extra["compileTargetVersion"] = JavaVersion.VERSION_11
//
//    repositories {
//        google()
//        mavenCentral()
//    }
//
//    dependencies {
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${extra["kotlin_version"]}")
//        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.8.10")
//        classpath("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
//    }
//}




allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

subprojects {
    // TODO This is needed to solve this issue https://youtrack.jetbrains.com/issue/KT-55947/Unable-to-set-kapt-jvm-target-version
    //  KAPT and KSP now ignore kotlinOptions.jvmTarget set globally for all tasks (e.g. in app module) and it
    //  has to be set explicitly like this to force them to use this Java version and not the one
    //  that is used by Gradle to run (currently Java 17 for Gradle 8). With not the same Java version
    //  used for all compilation tasks it fails, so we need to set it to same version like this.
    //  In the issue one guy says that this change was intentional and not a bug and that we should
    //  use Gradle JVM Toolchain to set a jvmTarget for KAPT/KSP, but this requires to actually
    //  download a JDK/JRE for that specific version (e.g. 11) as well, to be able to just compile
    //  to a Java 11 bytecode which can be done even with a Java 17 compiler. We might need to
    //  (and probably will) migrate to toolchains, as it seems to be a future of dealing with
    //  Java versions, but so far wait for some resolution on that task + some future additional
    //  info when it becomes more clear what is really a correct way to deal with this.
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
        kotlinOptions.jvmTarget = Constants.COMPILE_TARGET_VERSION
    }
}

//task clean (type: Delete) {
//    delete rootProject . buildDir
//}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}

///**
// * Define properties with library info
// */
//ext.libProperties = new Properties()
//ext.libProperties.load(file("${rootDir}/lib.properties").newReader())

//apply plugin : 'io.codearte.nexus-staging' // this should be at the end of the file

// TODO Migrate scripts at least to plugin DSL, try to remove JCenter and maven { url "https://jitpack.io" }
