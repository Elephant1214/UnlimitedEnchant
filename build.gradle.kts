plugins {
    idea
    java
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
}

group = "me.elephant1214.unlimitedenchant"
version = "1.4.0"

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.74-stable")
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
        minecraftVersion("26.2")
        dependsOn(jar)
    }
}
