import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmTask

plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.hibernate.orm") version "7.1.4.Final"
    id("org.sonarqube") version "6.3.1.5724"
    id("com.github.node-gradle.node") version "7.1.0"
}

group = "dev.jacobandersen.cam"

val versionBase = "0.0.1-SNAPSHOT"
val isSnapshot = versionBase.endsWith("-SNAPSHOT")
if (isSnapshot) {
    val gitHash = "git rev-parse --short HEAD".runCommand()?.trim() ?: "unknown"
    version = "$versionBase-$gitHash"
} else {
    version = versionBase
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val expiringMapVersion = "0.5.11"
    val bucket4JVersion = "8.14.0"
    val commonsValidatorVersion = "1.9.0"
    val bouncycastleVersion = "1.80"
    val jakartaPersistenceApiVersion = "3.2.0"
    val mockitoCoreVersion = "5.17.0"
    val testContainersVersion = "1.21.0"
    val apacheCommonsLangVersion = "3.19.0"
    val pebbleVersion = "3.2.4"
    val lettuceVersion = "6.8.1.RELEASE"

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("net.jodah:expiringmap:${expiringMapVersion}")
    implementation("com.bucket4j:bucket4j_jdk17-core:${bucket4JVersion}")
    implementation("commons-validator:commons-validator:${commonsValidatorVersion}")
    implementation("org.bouncycastle:bcprov-jdk18on:${bouncycastleVersion}")
    implementation("org.bouncycastle:bcpkix-jdk18on:${bouncycastleVersion}")
    implementation("jakarta.persistence:jakarta.persistence-api:${jakartaPersistenceApiVersion}")
    implementation("org.apache.commons:commons-lang3:${apacheCommonsLangVersion}")
    implementation("io.pebbletemplates:pebble-spring-boot-starter:${pebbleVersion}")
    implementation("io.lettuce:lettuce-core:${lettuceVersion}")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito:mockito-core:${mockitoCoreVersion}")
    testImplementation("org.testcontainers:junit-jupiter:${testContainersVersion}")
    testImplementation("org.testcontainers:postgresql:${testContainersVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

hibernate {
    enhancement {
        enableAssociationManagement = true
    }
}

val frontendDir = file("$projectDir/src/main/frontend")
val compiledCss = file("$projectDir/src/main/resources/static/main.css")

node {
    version = "20.18.0"
    download = true
    nodeProjectDir = frontendDir
}

fun frontendDirComputed(path: String): String {
    return "${frontendDir.path}/$path"
}

tasks.named<NpmInstallTask>("npmInstall") {
    workingDir = frontendDir
    inputs.files(frontendDirComputed("package.json"), frontendDirComputed("package-lock.json"))
    outputs.dir(frontendDirComputed("node_modules"))
    finalizedBy(npmBuild)
}

val npmBuild by tasks.registering(NpmTask::class) {
    workingDir = frontendDir
    args = listOf("run", "build")
    inputs.file(frontendDirComputed("main.css"))
    dependsOn("npmInstall")
}

tasks.processResources {
    dependsOn(npmBuild)
}

val coverageExclusions = arrayOf(
    "**/annotation/**",
    "**/api/**",
    "**/config/**",
    "**/*Config.class",
    "**/dto/**",
    "**/*Dto.class",
    "**/exception/**",
    "**/*Exception.class",
    "**/model/**",
    "**/repo/**"
)

sonar {
    properties {
        property("sonar.projectKey", "cards-against-my-sanity_auth")
        property("sonar.organization", "cards-against-my-sanity")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.exclusions", coverageExclusions.joinToString(","))
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required = true
    }

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(*coverageExclusions)
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            enabled = false // TODO: re-enable later
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.6".toBigDecimal()
            }
        }
    }
}

// helper
fun String.runCommand(): String? =
    ProcessBuilder(*split(" ").toTypedArray())
        .redirectErrorStream(true)
        .start()
        .inputStream
        .bufferedReader()
        .readText()
