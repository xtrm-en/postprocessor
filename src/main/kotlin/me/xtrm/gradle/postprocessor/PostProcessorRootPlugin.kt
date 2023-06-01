package me.xtrm.gradle.postprocessor

import enterprises.stardust.stargrad.StargradPlugin
import me.xtrm.gradle.postprocessor.api.Transformer
import me.xtrm.gradle.postprocessor.api.TransformerManager
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


/**
 * @author xtrm
 */
class PostProcessorRootPlugin : StargradPlugin() {
    override val id: String =
        "me.xtrm.postprocessor.root"
    override val conflictsWith: Set<String> =
        setOf("me.xtrm.postprocessor")
    private lateinit var extension: PostProcessorExtension

    override fun applyPlugin() {
        project.plugins.apply("java-library")

        extension = registerExtension()

        loadFromClasspath()

        val classes = project.tasks.getByName("classes")
        val task = registerTask<PostProcessTask> {
            dependsOn("compileJava")
            if (arrayOf("", "-platform-jvm", "-android", "-multiplatform").any {
                    project.pluginManager.hasPlugin("kotlin$it")
                }) {
                dependsOn("compileKotlin")
            }
            if (project.pluginManager.hasPlugin("groovy")) {
                dependsOn("compileGroovy")
            }
            if (project.pluginManager.hasPlugin("scala")) {
                dependsOn("compileScala")
            }

            configure(extension)
        }
        classes.dependsOn(task)
    }

    private fun loadFromClasspath() {
        val cl = PostProcessorRootPlugin::class.java.classLoader
        var current = cl
        val urls = mutableListOf<URL>()
        while (current is URLClassLoader) {
            Collections.addAll(urls, *current.urLs)
            current = current.parent
        }
        urls.map { File(it.file) }.filter { it.isFile && it.exists() }.forEach { file ->
            val zipFile = ZipFile(file)
            val entries: Enumeration<out ZipEntry> = zipFile.entries()

            val classes = mutableListOf<ByteArray>()
            entries.iterator().forEach { entry ->
                if (!entry.isDirectory && entry.name.endsWith(".class")) {
                    zipFile.getInputStream(entry).apply {
                        classes += readBytes()
                    }.also { it.close() }
                }
            }
            zipFile.close()

            val transformerName = Transformer::class.java.name.replace('.', '/')

            val transformerClasses = mutableListOf<String>()
            classes.forEach lookup@{ classBytes ->
                val node = ClassNode(Opcodes.ASM9).also {
                    ClassReader(classBytes).accept(it, ClassReader.EXPAND_FRAMES)
                }

                if (transformerName == node.name) return@lookup
                if (transformerName == node.superName) {
                    transformerClasses += node.name.replace('/', '.').let {
                        if (it.startsWith('/'))
                            return@let it.substring(1)
                        it
                    }
                }
            }

            TransformerManager.loadAll(
                *transformerClasses.map {
                    @Suppress("UNCHECKED_CAST")
                    Class.forName(it) as Class<out Transformer>
                }.toTypedArray()
            )
        }
    }
}
