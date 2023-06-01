private const val kotlinVersion = "1.8.20"

object Plugins {
    const val KOTLIN = kotlinVersion
    const val DOKKA = "1.+"
    const val GRADLE_PUBLISH_PLUGIN = "0.21.+"
    const val KTLINT = "10.2.1"
    const val BLOSSOM = "1.+"
}

object Dependencies {
    const val KOTLIN = kotlinVersion
    const val ASM = "9.5"
    const val STARGRAD = "0.5.4"

    val kotlinModules = arrayOf("stdlib")
}

object Repositories {
    val mavenUrls = arrayOf(
        "https://jitpack.io/",
    )
}
