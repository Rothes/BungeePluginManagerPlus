import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":api", "shadow"))

    compileOnly("net.md-5:bungeecord-api:1.18-R0.1-SNAPSHOT")
}

tasks {
    val shadowJar by named("shadowJar", ShadowJar::class) {
        archiveBaseName.set("BungeePluginManagerPlus")
        dependencies {
            include(project(":api"))
            include(dependency("org.jetbrains.kotlin:.*"))
            include(dependency("org.bstats:.*"))
        }

        minimize()
        relocate("kotlin", "io.github.rothes.bungeepluginmanagerplus.libs.kotlin")
        relocate("org.jetbrains", "io.github.rothes.bungeepluginmanagerplus.libs.org.jetbrains")
        relocate("org.intellij", "io.github.rothes.bungeepluginmanagerplus.libs.org.intellij")
        relocate("org.bstats", "io.github.rothes.bungeepluginmanagerplus.libs.org.bstats")
    }
}
