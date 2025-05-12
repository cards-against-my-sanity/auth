plugins {
	java
	id("org.springframework.boot") version "3.4.0"
	id("io.spring.dependency-management") version "1.1.6"
	id("org.hibernate.orm") version "6.6.2.Final"
	id("org.sonarqube") version "6.1.0.5360"
	jacoco
}

group = "dev.jacobandersen.cams"
version = "0.0.1-SNAPSHOT"

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
	val jjwtVersion = "0.12.6"
	val expiringMapVersion = "0.5.11"
	val bucket4JVersion = "8.14.0"
	val testContainersVersion = "1.21.0"
	val apacheCommonsLangVersion = "3.17.0"

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-mysql")
	implementation("io.jsonwebtoken:jjwt-api:${jjwtVersion}")
	implementation("net.jodah:expiringmap:${expiringMapVersion}")
	implementation("com.bucket4j:bucket4j_jdk17-core:${bucket4JVersion}")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:${jjwtVersion}")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jjwtVersion}")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.testcontainers:junit-jupiter:${testContainersVersion}")
	testImplementation("org.testcontainers:mariadb:${testContainersVersion}")
	testImplementation("org.apache.commons:commons-lang3:${apacheCommonsLangVersion}")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

hibernate {
	enhancement {
		enableAssociationManagement = true
	}
}

sonar {
	properties {
		property("sonar.projectKey", "cards-against-my-sanity_auth")
		property("sonar.organization", "cards-against-my-sanity")
		property("sonar.host.url", "https://sonarcloud.io")
	}
}

tasks.test {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)

	classDirectories.setFrom(
		files(classDirectories.files.map {
			fileTree(it) {
				exclude(
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
