object Coordinates {
    const val NAME = "postprocessor"
    const val DESC = "A Gradle plugin that allows for bytecode transformation of compiled jars."

    const val GIT_HOST = "github.com"
    const val REPO_ID = "xtrm-en/$NAME"

    const val GROUP = "me.xtrm"
    const val VERSION = "0.2.0"
}

object Pom {
    val licenses = arrayOf(
        License("ISC License", "https://opensource.org/licenses/ISC")
    )
    val developers = arrayOf(
        Developer("xtrm")
    )
}

data class License(val name: String, val url: String, val distribution: String = "repo")
data class Developer(val id: String, val name: String = id)
