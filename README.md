# xtrm's postprocessor
[![Build][badge-github-ci]][project-gradle-ci] 

a [Gradle][gradle] plugin that allows for bytecode transformation of compiled jars.

## how to use

you can import [postprocessor][project-url] from the [gradle plugins repo][gpr]
just by adding it to your `plugins` block:

```kotlin
plugins {
    id("me.xtrm.postprocessor") version "{VERSION}"
}
```

## troubleshooting

if you ever encounter any problem **related to this project**, you can [open an issue][new-issue] describing what the
problem is. please, be as precise as you can, so that we can help you asap. we are most likely to close the issue if it
is not related to our work.

## contributing

you can contribute by [forking the repository][fork], making your changes and [creating a new pull request][new-pr]
describing what you changed, why and how.

## licensing

this project is under the [ISC license][project-license].


<!-- Links -->

[jvm]: https://adoptium.net "adoptium website"

[kotlin]: https://kotlinlang.org "kotlin website"

[gpr]: https://plugins.gradle.com/ "gradle plugins repo"

[gradle]: https://gradle.org/

<!-- Project Links -->

[project-url]: https://github.com/xtrm-en/postprocessor "project github repository"

[fork]: https://github.com/xtrm-en/postprocessor/fork "fork this repository"

[new-pr]: https://github.com/xtrm-en/postprocessor/pulls/new "create a new pull request"

[new-issue]: https://github.com/xtrm-en/postprocessor/issues/new "create a new issue"

[project-gradle-ci]: https://github.com/xtrm-en/postprocessor/actions/workflows/gradle-ci.yml "gradle ci workflow"

[project-license]: https://github.com/xtrm-en/postprocessor/blob/trunk/LICENSE "LICENSE source file"

<!-- Badges -->

[badge-github-ci]: https://github.com/xtrm-en/postprocessor/actions/workflows/build.yml/badge.svg?branch=trunk "github actions badge"
