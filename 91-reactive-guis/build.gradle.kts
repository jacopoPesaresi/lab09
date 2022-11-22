plugins {
    java
    application
     id("org.danilopianini.gradle-java-qa") version "0.40.0" //added
}


repositories {
    mavenCentral() //added
}

val mainClass: String by project

application {
    // The following allows to run with: ./gradlew -PmainClass=it.unibo.oop.MyMainClass run
    mainClass.set(project.properties["mainClass"].toString())
}