package me.xtrm.gradle.postprocessor.api

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

/**
 * @author xtrm
 */
object TransformerManager {
    private val transformers = mutableListOf<Transformer>()

    internal fun loadFromClasspath() {
    }

    fun loadAll(vararg classes: Class<*>) {
        classes.forEach { clazz ->
            clazz.getDeclaredConstructor().also {
                it.isAccessible = true
            }.newInstance().also {
                transformers.add(it as Transformer)
            }
        }
    }

    fun addTransformers(transformer: Transformer) {
        transformers.add(transformer)
    }

    internal fun transform(byteArray: ByteArray): ByteArray? = run {
        ClassWriter(ClassWriter.COMPUTE_FRAMES).also {
            ClassNode().also { node ->
                ClassReader(byteArray).accept(node, ClassReader.EXPAND_FRAMES)
                var transform = false
                transformers.forEach { transformer ->
                    if (transformer.isTarget(node.name)) {
                        transform = true
                        transformer.transform(node)
                    }
                }
                if (!transform) return null
            }.accept(it)
        }.toByteArray()
    }
}
