plugins {
    idea
    java
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
}

group = "me.elephant1214.unlimitedenchant"
version = "1.0.0"

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

val targetJavaVersion = JavaVersion.VERSION_21
java {
    targetCompatibility = targetJavaVersion
    sourceCompatibility = targetJavaVersion
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
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
        minecraftVersion("1.21")
        dependsOn(jar)
    }
}
