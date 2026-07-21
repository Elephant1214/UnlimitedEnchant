rootProject.name = "UnlimitedEnchant"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("io.papermc.paperweight.userdev") version("2.0.0-beta.21")
        id("xyz.jpenilla.run-paper") version("3.0.2")
    }
}
