plugins {
    java
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "com.hungergames"
version = "1.0.0"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
    maven("https://repo.purpurmc.org/snapshots")
}

dependencies {
    // Purpur API — extend มาจาก Paper ทุกอย่างที่เขียนไว้ยังใช้ได้หมด
    // plugin นี้รันได้บน Purpur server
    compileOnly("org.purpurmc.purpur:purpur-api:1.21-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }

    jar {
        archiveFileName = "LootChest+Airdrop.jar"
    }

    runServer {
        // ใช้ Paper สำหรับทดสอบ local (Purpur extend Paper โค้ดเราทำงานได้เหมือนกัน)
        minecraftVersion("1.21")
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
