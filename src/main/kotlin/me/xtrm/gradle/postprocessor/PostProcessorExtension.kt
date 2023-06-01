package me.xtrm.gradle.postprocessor

import enterprises.stardust.stargrad.ext.Extension
import enterprises.stardust.stargrad.ext.StargradExtension
import org.gradle.api.Project
import javax.inject.Inject

/**
 * TODO: disabled transformers
 * @author xtrm
 */
@Extension("postprocessor")
abstract class PostProcessorExtension
@Inject constructor(
    project: Project,
    plugin: PostProcessorRootPlugin,
) : StargradExtension<PostProcessorRootPlugin>(project, plugin)
