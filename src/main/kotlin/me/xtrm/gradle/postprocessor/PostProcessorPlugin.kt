package me.xtrm.gradle.postprocessor

import enterprises.stardust.stargrad.StargradPlugin
import me.xtrm.gradle.postprocessor.api.Transformer
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * @author xtrm
 */
class PostProcessorPlugin: StargradPlugin() {
    override val id: String =
        "me.xtrm.postprocessor"
    override val conflictsWith: Set<String> =
        setOf("me.xtrm.postprocessor.root")

    override fun applyPlugin() {
        if (project.repositories.size == 0) {
            project.repositories.mavenCentral()
        }

        val apiJar = extractApi()

        val deps = mutableListOf<Any>(project.files(apiJar.path))
        // replaced at compile time by Blossom gradle plugin
        val transitiveDeps = "@transitive_deps@"
        transitiveDeps.split(';').forEach { deps += it }

        deps.forEach {
            project.dependencies.add(IMPLEMENTATION_CONFIGURATION, it)
        }
    }

    private fun extractApi(): File {
        val jarfileUrl = Transformer::class.java
            .protectionDomain.codeSource.location

        val pluginFile = File(jarfileUrl.file)
        if (!pluginFile.exists()) {
            throw RuntimeException("Postprocessor's plugin jarfile not found.")
        }

        val zipFile = ZipFile(pluginFile)
        val files = mutableMapOf<ZipEntry, ByteArray>()
        zipFile.use { zip ->
            val entries = zip.entries()
            entries.iterator().forEach { entry ->
                val className: String = entry.name
                if (!className.endsWith(".class")) {
                    return@forEach
                }
                if (className.startsWith(API_PACKAGE)) {
                    val bytes = zip.getInputStream(entry).readBytes()
                    files[entry] = bytes
                }
            }
        }

        if (files.isEmpty()) {
            throw RuntimeException("sus.")
        }

        val tempDir = Files.createTempFile("postprocessor", ".tmp").toFile().run {
            val parent = parentFile
            if (!delete()) deleteOnExit()
            parent
        }

        val apiJar = File(tempDir, "postprocessor-api.jar")
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

    companion object {
        private const val IMPLEMENTATION_CONFIGURATION = "implementation"

        private val API_PACKAGE = Transformer::class.java.`package`.name
            .replace('.', '/') + '/'
    }
}
