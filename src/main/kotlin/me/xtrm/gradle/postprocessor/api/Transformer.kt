package me.xtrm.gradle.postprocessor.api

import org.objectweb.asm.tree.ClassNode

/**
 * @author xtrm
 */
@FunctionalInterface
fun interface Transformer {
    fun transform(classNodes: MutableMap<String, ClassNode>)
    fun priority(): Int = 0
}
