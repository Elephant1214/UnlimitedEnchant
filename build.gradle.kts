plugins {
    idea
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.runPaper)
    id("me.elephant1214.plugin-loader-catalog") version "1.0.0"
}

group = "me.elephant1214"
version = "2.0-SNAPSHOT"

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("26.3.build.237-stable")
}

pluginLoaderCatalog {
    packageName = "me.elephant1214.unlimitedenchant.bootstrap"
    className = "VersionCatalog"
    bundles = setOf("kotlin")
}

val targetJavaVersion = JavaVersion.VERSION_25
java {
    targetCompatibility = targetJavaVersion
    sourceCompatibility = targetJavaVersion
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
    
    runServer {
        minecraftVersion("26.3")
        dependsOn(jar)
    }
}
