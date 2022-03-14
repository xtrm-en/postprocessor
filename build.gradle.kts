import java.net.URL

plugins {
    // Language Plugins
    `java-library`
    kotlin("jvm") version Plugins.KOTLIN

    // Code Quality
    id("org.jlleitschuh.gradle.ktlint") version Plugins.KTLINT

    // Documentation Generation
    id("org.jetbrains.dokka") version Plugins.DOKKA

    // Maven Publication
    id("com.gradle.plugin-publish") version Plugins.GRADLE_PUBLISH_PLUGIN
    `java-gradle-plugin`
    `maven-publish`

    id("net.kyori.blossom") version Plugins.BLOSSOM
}

group = Coordinates.GROUP
version = Coordinates.VERSION

// What JVM version should this project compile to
val targetVersion = "1.8"
// What JVM version this project is written in
val sourceVersion = "1.8"
// Should we generate an /api/ source set
val apiSourceSet = true

// Maven Repositories
repositories {
    mavenLocal()
    mavenCentral()
    Repositories.mavenUrls.forEach(::maven)
}

// Transitive Dependencies
configurations {
    val implementation by getting
    val transitive by creating
    implementation.extendsFrom(transitive)
}

// Project Dependencies
dependencies {
    val transitive by configurations

    listOf("asm", "asm-tree").forEach {
        transitive("org.ow2.asm", it, Dependencies.ASM)
    }

    implementation("fr.stardustenterprises", "stargrad", Dependencies.STARGRAD)
    implementation(gradleApi())

    Dependencies.kotlinModules.forEach {
        implementation("org.jetbrains.kotlin", "kotlin-$it", Plugins.KOTLIN)
    }
    testImplementation("org.jetbrains.kotlin", "kotlin-test", Plugins.KOTLIN)
}

// Generate the /api/ source set
if (apiSourceSet) {
    sourceSets {
        val name = "api"

        val main by sourceSets
        val test by sourceSets

        val sourceSet = create(name) {
            java.srcDir("src/$name/kotlin")
            resources.srcDir("src/$name/resources")

            this.compileClasspath += main.compileClasspath
            this.runtimeClasspath += main.runtimeClasspath
        }

        arrayOf(main, test).forEach {
            it.compileClasspath += sourceSet.output
            it.runtimeClasspath += sourceSet.output
        }
    }
}

blossom {
    val sb = StringBuilder()
    val transitive by configurations
    transitive.dependencies.forEach {
        sb.append("${it.group}:${it.name}:${it.version}")
        sb.append(";")
    }
    // hacky? yes. required? probably. who? asked.
    replaceToken("@transitive_deps@", sb.substring(0, sb.length - 1))
}

// Disable unneeded rules
ktlint {
    this.disabledRules.add("no-wildcard-imports")
}

tasks {
    test {
        useJUnitPlatform()
    }

    // Configure JVM versions
    compileKotlin {
        kotlinOptions.jvmTarget = targetVersion
    }
    compileJava {
        targetCompatibility = targetVersion
        sourceCompatibility = sourceVersion
    }

    dokkaHtml {
        val moduleFile = File(projectDir, "MODULE.temp.md")

        run {
            // In order to have a description on the rendered docs, we have to have
            // a file with the # Module thingy in it. That's what we're
            // automagically creating here.

            doFirst {
                moduleFile.writeText("# Module ${Coordinates.NAME}\n${Coordinates.DESC}")
            }

            doLast {
                moduleFile.delete()
            }
        }

        moduleName.set(Coordinates.NAME)

        dokkaSourceSets.configureEach {
            displayName.set("${Coordinates.NAME} on ${Coordinates.GIT_HOST}")
            includes.from(moduleFile.path)

            skipDeprecated.set(false)
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)
            reportUndocumented.set(true)
            suppressObviousFunctions.set(true)

            // Link the source to the documentation
            sourceLink {
                localDirectory.set(file("src"))
                remoteUrl.set(URL("https://${Coordinates.GIT_HOST}/${Coordinates.REPO_ID}/tree/trunk/src"))
            }

            // External documentation link template
//            externalDocumentationLink {
//                url.set(URL("https://javadoc.io/doc/net.java.dev.jna/jna/5.10.0/"))
//            }
        }
    }

    // The original artifact, we just have to add the API source output and the
    // LICENSE file.
    jar {
        if (apiSourceSet) {
            from(sourceSets["api"].output)
        }
        from("LICENSE")
    }

    if (apiSourceSet) {
        // API artifact, only including the output of the API source and the
        // LICENSE file.
        create("apiJar", Jar::class) {
            group = "build"

            archiveClassifier.set("api")
            from(sourceSets["api"].output)

            from("LICENSE")
        }
    }

    // Source artifact, including everything the 'main' does but not compiled.
    create("sourcesJar", Jar::class) {
        group = "build"

        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
        if (apiSourceSet) {
            from(sourceSets["api"].allSource)
        }

        from("LICENSE")
    }

    // The Javadoc artifact, containing the Dokka output and the LICENSE file.
    create("javadocJar", Jar::class) {
        group = "build"

        val dokkaHtml = getByName("dokkaHtml")

        archiveClassifier.set("javadoc")
        dependsOn(dokkaHtml)
        from(dokkaHtml)

        from("LICENSE")
    }
}

// Define the default artifacts' tasks
val defaultArtifactTasks = arrayOf(
    tasks["sourcesJar"],
    tasks["javadocJar"]
).also {
    if (apiSourceSet) {
        it.plus(tasks["apiJar"])
    }
}

// Declare the artifacts
artifacts {
    defaultArtifactTasks.forEach(::archives)
}

gradlePlugin {
    plugins {
        create("postprocessor") {
            displayName = "X's postprocessor"
            description = "A plugin that allows for bytecode transformation of compiled jars."
            id = "me.xtrm.postprocessor"
            implementationClass = "me.xtrm.gradle.postprocessor.PostprocessorPlugin"
        }
    }
}

pluginBundle {
    vcsUrl = "https://${Coordinates.GIT_HOST}/${Coordinates.REPO_ID}"
    website = "https://${Coordinates.GIT_HOST}/${Coordinates.REPO_ID}"
    tags = listOf("java", "post-processing", "jvm", "kotlin")
}
