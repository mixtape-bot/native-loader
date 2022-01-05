plugins {
    kotlin("jvm")
}

val projectName = "rust"
project.group = "com.example"
project.version = "1.0.0"

dependencies {
    implementation(rootProject)
    implementation("ch.qos.logback:logback-classic:1.2.10")
}

tasks.create<Exec>("deployRust") {
    workingDir = file(".")
    commandLine = listOf("cargo", "build", "--release")

    copy {
        // TODO: Make these a per-system value lol
        from("build/rust/release/lib$projectName.so")
        into("src/main/resources/natives/linux-x86-64")
    }
}
