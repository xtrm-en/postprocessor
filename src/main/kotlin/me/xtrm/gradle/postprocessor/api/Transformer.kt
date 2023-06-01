package me.xtrm.gradle.postprocessor.api

import org.objectweb.asm.tree.ClassNode

/**
 * @author xtrm
 */
abstract class Transformer(
    private vararg val targets: String,
) {
    abstract fun transform(classNode: ClassNode)

    internal fun isTarget(name: String): Boolean =
        targets.isEmpty() || targets.any { name.startsWith(it) }
}
