plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.kyori.blossom") version "1.2.0"
    id("org.sonarqube") version "3.3"
}

group = "net.bridgesplash"
version = "v1.2.0"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
    maven("https://jitpack.io")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    compileOnly("org.jetbrains:annotations:23.0.0")

    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.github.Minestom:Minestom:42195c536b")

    implementation("com.j256.ormlite:ormlite-jdbc:6.1")
    implementation("com.h2database:h2:2.1.214")

}
tasks{

    test {
        workingDir = file("./Testserver")
        useJUnitPlatform {

            includeEngines("junit-jupiter")
        }
    }

    blossom {
        replaceToken("&version", version)
        replaceToken("&implementationName", rootProject.name)
        replaceToken("&updateUri", "https://api.github.com/repos/BridgeSplash/PlaceStom/releases/latest")
    }

    processResources {
        filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to mapOf("version" to project.version))
    }
}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
sonarqube {
    properties {
        property("sonar.projectKey", "BridgeSplash_PlaceStom")
        property( "sonar.organization", "bridgesplash")
        property( "sonar.host.url", "https://sonarcloud.io")
    }
}