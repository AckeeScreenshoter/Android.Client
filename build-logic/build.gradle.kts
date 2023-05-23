plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

gradlePlugin {
    plugins.register("build-logic-plugin") {
        id = "build-logic-plugin"
        implementationClass = "io.github.ackeescreenshotter.android.BuildLogicPlugin"
    }
}
