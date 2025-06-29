plugins {
	java
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
	id("checkstyle")
}

group = "ovh.neziw"
version = "1.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

checkstyle {
	toolVersion = "10.26.1"
	maxWarnings = 0
}

repositories {
	gradlePluginPortal()
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito:mockito-core")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}