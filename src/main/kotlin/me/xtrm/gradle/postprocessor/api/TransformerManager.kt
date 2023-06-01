package me.xtrm.gradle.postprocessor.api

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.net.URLClassLoader

/**
 * @author xtrm
 */
object TransformerManager {
    private val transformers = mutableListOf<Transformer>()

    internal fun loadFromClasspath() {
    }

    fun loadAll(vararg classNames: String) =
        loadAll(*classNames.map { TransformerClassLoader.loadClass(it) }.toTypedArray())

    private fun loadAll(vararg classes: Class<*>) {
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

    internal fun transform(buffers: Map<String, ByteArray>): Map<String, ByteArray> {
        val classNodes: MutableMap<String, ClassNode> = buffers.map { (name, bytes) ->
            name to ClassNode().also {
                ClassReader(bytes).accept(it, ClassReader.EXPAND_FRAMES)
            }
        }.toMap().toMutableMap()

        transformers.forEach {
            it.transform(classNodes)
        }

        return classNodes.map { (name, node) ->
            name to ClassWriter(ClassWriter.COMPUTE_MAXS).also {
                node.accept(it)
            }.toByteArray()
        }.toMap()
    }
}

internal object TransformerClassLoader : URLClassLoader(arrayOf(), TransformerManager::class.java.classLoader) {
    public override fun addURL(url: java.net.URL?) {
        super.addURL(url)
    }
}
