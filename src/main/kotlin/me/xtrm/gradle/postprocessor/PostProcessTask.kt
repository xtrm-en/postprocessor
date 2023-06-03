package me.xtrm.gradle.postprocessor

import enterprises.stardust.stargrad.task.ConfigurableTask
import enterprises.stardust.stargrad.task.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * TODO: @Input(s)
 * @author xtrm
 */
@Task("postprocessClasses")
@DisableCachingByDefault
open class PostProcessTask : ConfigurableTask<PostProcessorExtension>() {
    override fun run() {
        val targetDirectories = project.tasks.filterIsInstance<AbstractCompile>().map {
            it.destinationDirectory
        }.toMutableList()

        targetDirectories += try {
            val kotlin = Class.forName("org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool")
            val dest = kotlin.getMethod("getDestinationDirectory").also { it.isAccessible = true }
            project.tasks.filter { kotlin.isInstance(it) }.map {
                dest.invoke(it) as DirectoryProperty
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }

        targetDirectories.map { it.get().asFile.absolutePath to it.asFileTree }.forEach { (root, tree) ->
            val classes = mutableMapOf<String, ByteArray>()
            tree.visit { visit ->
                visit.file.let {
                    if (it.isDirectory || !it.name.endsWith(".class")) return@visit
                    if (!it.exists()) {
                        println("File ${it.absolutePath} does not exist!")
                        return@visit
                    }

                    val bytes = it.readBytes()
                    val relativePath = it.absolutePath.removePrefix(root).removePrefix("/")
                    classes[relativePath] = bytes
                }
            }
            TransformerManager.transform(classes)
                .forEach { (path, bytes) ->
                    val file = File("$root/$path")
                    file.parentFile.mkdirs()
                    file.writeBytes(bytes)
                }
        }
    }
}
