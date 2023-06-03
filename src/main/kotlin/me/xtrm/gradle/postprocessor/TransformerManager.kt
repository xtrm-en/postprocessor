package me.xtrm.gradle.postprocessor

import me.xtrm.gradle.postprocessor.api.Transformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.net.URLClassLoader
import java.util.*

/**
 * @author xtrm
 */
object TransformerManager {
    private val transformers = mutableListOf<Transformer>()

    fun loadAllClassNames(vararg classNames: String) =
        loadAllClasses(*classNames.map {
            Class.forName(it, true, TransformerClassLoader)
        }.toTypedArray())

    internal fun loadAllClasses(vararg classes: Class<*>) {
        classes.forEach { clazz ->
            clazz.getDeclaredConstructor().also {
                it.isAccessible = true
            }.newInstance().also {
                addTransformers(it as Transformer)
            }
        }
    }

    private fun addTransformers(vararg transformers: Transformer) {
        Collections.addAll(TransformerManager.transformers, *transformers)
        TransformerManager.transformers.sortByDescending { it.priority() }
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

internal object TransformerClassLoader : URLClassLoader(arrayOf(), PostProcessorRootPlugin::class.java.classLoader) {
    public override fun addURL(url: java.net.URL?) = super.addURL(url)
}
