package me.xtrm.gradle.postprocessor.ext

import fr.stardustenterprises.stargrad.ext.Extension
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import javax.inject.Inject

@Extension("postprocessor")
abstract class PostprocessorExtension
@Inject constructor(
    project: Project,
) {
    @Internal
    private val objects = project.objects

    @Input
    val transformers: MutableList<String> =
        mutableListOf()
}
