package me.xtrm.gradle.postprocessor.task

import fr.stardustenterprises.stargrad.task.ConfigurableTask
import me.xtrm.gradle.postprocessor.api.Transformer
import me.xtrm.gradle.postprocessor.ext.PostprocessorExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SourceSet

open class ProcessClassesTask : ConfigurableTask<PostprocessorExtension>() {
    @Input
    lateinit var transformers: List<Transformer>

    @Input
    lateinit var sourceSets: List<SourceSet>

    override fun applyConfiguration() {
        val javaExtension = project.extensions.getByType(
            JavaPluginExtension::class.java
        )
        this.sourceSets = javaExtension.sourceSets.toList()

        val tempTransformers = mutableListOf<Transformer>()
        this.configuration.transformers.forEach { className ->
            val clazz = Class.forName(className)
            val isTransformer = clazz.isAssignableFrom(Transformer::class.java)
            if (!isTransformer) {
                throw RuntimeException(
                    "Class $className isn't a subclass of " +
                        Transformer::class.java.name
                )
            }

            try {
                val instance = clazz.kotlin.objectInstance
                    ?: clazz.getConstructor().newInstance()

                tempTransformers += instance as Transformer
            } catch (throwable: Throwable) {
                logger.warn("Class $className couldn't be initialized.")
                throwable.printStackTrace()
            }
        }
        this.transformers = tempTransformers
    }

    override fun doTask() {
        this.sourceSets.forEach { sourceSet ->
            sourceSet.output.classesDirs.forEach {

            }
        }
    }
}
