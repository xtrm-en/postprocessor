private const val kotlinVersion = "1.6.10"

object Plugins {
    const val KOTLIN = kotlinVersion
    const val DOKKA = kotlinVersion
    const val GRADLE_PUBLISH_PLUGIN = "0.18.0"
    const val KTLINT = "10.2.1"
    const val BLOSSOM = "1.3.0"
}

object Dependencies {
    const val KOTLIN = kotlinVersion
    const val ASM = "9.2"
    const val STARGRAD = "0.2.0"

    val kotlinModules = arrayOf("stdlib")
}

object Repositories {
    val mavenUrls = arrayOf(
        "https://jitpack.io/",
    )
}
