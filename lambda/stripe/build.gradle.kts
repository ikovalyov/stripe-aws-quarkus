import io.quarkus.gradle.tasks.QuarkusBuildCacheableAppParts
import org.gradle.internal.classpath.Instrumented.systemProperty

plugins {
    java
    id("io.quarkus")
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val quarkusAmazonServicesPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation(enforcedPlatform("io.quarkiverse.amazonservices:quarkus-amazon-services-bom:${quarkusAmazonServicesPlatformVersion}"))

    implementation("io.quarkus:quarkus-amazon-lambda-http")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-rest-client-jackson")
    implementation("io.quarkiverse.amazonservices:quarkus-amazon-secretsmanager")
    implementation("com.stripe:stripe-java:25.3.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(platform("software.amazon.awssdk:bom:2.21.1"))
    implementation("software.amazon.awssdk:secretsmanager")
    implementation("software.amazon.awssdk:url-connection-client")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "com.example"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
tasks.withType<QuarkusBuildCacheableAppParts> {
    systemProperty("quarkus.package.type", "native")
    systemProperty("quarkus.native.container-build", "true")
}

