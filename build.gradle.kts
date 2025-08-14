import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)
}

group = "moe.minacle.minecraft"
version = "0.2.0"

repositories {
    mavenCentral()
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "Sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "Sonatype"
        url = uri("https://s01.oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${libs.versions.minecraft.get()}-R0.1-SNAPSHOT")
    implementation(libs.bstats.bukkit)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks {
    runServer {
    notCompatibleWithConfigurationCache("run-paper 2.3.1 accesses project during execution")
        minecraftVersion(libs.versions.minecraft.get())
    }
}

runPaper.folia.registerTask()

tasks.named("runFolia") {
    notCompatibleWithConfigurationCache("run-paper 2.3.1 accesses project during execution")
}

tasks.named<ShadowJar>("shadowJar") {
    isEnableRelocation = true
    relocationPrefix = "moe.minacle.minecraft.plugins.voidplate.shadowjar"
    archiveClassifier.set("")
    minimize()
}
