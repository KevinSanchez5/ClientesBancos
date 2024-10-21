plugins {
    id("java")
    id("io.freefair.lombok") version "8.6"
    id("jacoco")
}

group = "kj"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // Project Reactor
    implementation("io.projectreactor:reactor-core:3.6.10")

    // Test Containers
    testImplementation("org.testcontainers:testcontainers:1.20.2")
    testImplementation("org.testcontainers:junit-jupiter:1.20.2")
    testImplementation("org.testcontainers:postgresql:1.20.2")

    // Logger SLF4J
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.slf4j:slf4j-simple:1.7.32")

    // Lombok
    implementation("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // SQLite
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")

    // Hikary
    implementation("com.zaxxer:HikariCP:5.0.1")

    // PostGresSQL
    implementation("org.postgresql:postgresql:42.7.4")

    // Ibatis
    implementation("org.mybatis:mybatis:3.5.13")

    // Junit
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Mockito
    testImplementation("org.mockito:mockito-core:3.12.4")
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")

    // Reactor Test
    testImplementation("io.projectreactor:reactor-test:3.6.11")

    // Jackson for JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")

    // Reactor Core
    implementation("io.projectreactor:reactor-core:3.6.10")

    //data type
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.11.0") // Jackson con Retrofit
    implementation("com.jakewharton.retrofit:retrofit2-reactor-adapter:2.1.0") // Reactor con Retrofit
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.11.0") // RxJava3 con Retrofit

    //StepVerifier
    testImplementation("io.projectreactor:reactor-test:3.4.12")


}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}


tasks.jar {
    manifest {
        // Clase principal
        attributes["Main-Class"] = "banco.Main"
    }
    // Incluir dependencias
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    // Excluir duplicados
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}