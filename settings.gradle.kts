rootProject.name = "UnlimitedEnchant"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://jitpack.io")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "me.elephant1214.plugin-loader-catalog") {
                useModule("com.github.Elephant1214:plugin-loader-catalog:${requested.version}")
            }
        }
    }
}
