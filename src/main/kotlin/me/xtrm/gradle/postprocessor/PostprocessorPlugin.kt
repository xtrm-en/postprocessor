package me.xtrm.gradle.postprocessor

import fr.stardustenterprises.stargrad.StargradPlugin
import me.xtrm.gradle.postprocessor.api.Transformer
import me.xtrm.gradle.postprocessor.ext.PostprocessorExtension
import me.xtrm.gradle.postprocessor.task.ProcessClassesTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.configurationcache.extensions.capitalized
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

open class PostprocessorPlugin : StargradPlugin() {
    companion object {
        private const val IMPLEMENTATION_CONFIGURATION = "implementation"

        private val API_PACKAGE = Transformer::class.java.packageName
            .replace('.', '/') + '/'
    }

    override val pluginId = "me.xtrm.postprocessor"
    private lateinit var postprocessorExtension: PostprocessorExtension

    override fun applyPlugin() {
        this.postprocessorExtension = extension(
            PostprocessorExtension::class.java
        )

        if (project.repositories.size == 0) {
            project.repositories.mavenCentral()
        }

        val apiJar = extractApi()

        val deps = mutableListOf<Any>(project.files(apiJar.path))
        // replaced at compile time by Blossom gradle plugin
        val transitiveDeps = "@transitive_deps@"
        transitiveDeps.split(';').forEach(deps::plusAssign)

        deps.forEach {
            project.dependencies.add(IMPLEMENTATION_CONFIGURATION, it)
        }
    }

    override fun afterEvaluate(project: Project) {
        val javaPlugin = project.extensions.getByType(
            JavaPluginExtension::class.java
        )

        javaPlugin.sourceSets.forEach { sourceSet ->
            val taskName = "processClasses${sourceSet.name.capitalized()}"

            project.tasks.register(
                taskName,
                ProcessClassesTask::class.java
            ).also {
                it.configure { task ->
                    task.configure(this.postprocessorExtension)
                }

                project.tasks
                    .findByName(sourceSet.classesTaskName)
                    ?.dependsOn(this)
                    ?: throw RuntimeException(
                        "Couldn't find \"${sourceSet.classesTaskName}\" task"
                    )
            }
        }
    }

    private fun extractApi(): File {
        val jarfileUrl = PostprocessorPlugin::class.java
            .protectionDomain.codeSource.location

        val pluginFile = File(jarfileUrl.file)
        if (!pluginFile.exists()) {
            throw RuntimeException("Postprocessor's plugin jarfile not found.")
        }

        val zipFile = ZipFile(pluginFile)
        val files = mutableMapOf<ZipEntry, ByteArray>()
        zipFile.use { zip ->
            val entries = zip.entries()
            entries.asIterator().forEach { entry ->
                val className: String = entry.name
                if (!className.endsWith(".class")) {
                    return@forEach
                }
                if (className.startsWith(API_PACKAGE)) {
                    val bytes = zip.getInputStream(entry)
                        .use(InputStream::readAllBytes)
                    files[entry] = bytes
                }
            }
        }

        if (files.isEmpty()) {
            throw RuntimeException("sus.")
        }

        val apiJar = Files.createTempFile(
            "postprocessor-api", ".jar"
        ).toFile()

        if (apiJar.exists()) {
            apiJar.delete()
        }
        apiJar.createNewFile()

        apiJar.outputStream().use { outputStream ->
            val zos = ZipOutputStream(outputStream)
            zos.use {
                files.forEach { (entry, buffer) ->
                    zos.putNextEntry(entry)
                    zos.write(buffer)
                }
            }
        }

        return apiJar
    }
}
