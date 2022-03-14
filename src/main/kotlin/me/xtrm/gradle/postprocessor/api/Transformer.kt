package me.xtrm.gradle.postprocessor.api

import org.objectweb.asm.tree.ClassNode

/**
 * Base transformer interface.
 *
 * @author xtrm
 * @since 0.1.0
 */
@FunctionalInterface
interface Transformer {
    /**
     * Allows for transformation of a classfile
     * via its parsed [ClassNode].
     *
     * @param classNode the parsed [ClassNode]
     * @return wheather or not the class was modified
     */
    fun transform(classNode: ClassNode): Boolean

    /**
     * Annotation used to load only required classes for transformation.
     *
     * @author xtrm
     * @since 0.1.0
     */
    annotation class Target(vararg val value: String)
}
