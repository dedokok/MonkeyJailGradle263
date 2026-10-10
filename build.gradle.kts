plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.24"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://nexus.scarsz.me/content/groups/public/")
    //maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    //compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    compileOnly("net.skinsrestorer:skinsrestorer-api:15.12.6")
    compileOnly("com.discordsrv:discordsrv:1.28.0")
    paperweight.paperDevBundle("26.3.build.+")
}


java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.3")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
