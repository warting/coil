package coil3

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.Lint
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar.Dokka
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.androidLibrary(
    name: String,
    config: Boolean = false,
    action: LibraryExtension.() -> Unit = {},
) = androidBase<LibraryExtension>(name) {
    buildFeatures {
        buildConfig = config
    }
    sourceSets["main"].resources {
        srcDirs("src/commonMain/resources", "src/jvmCommonMain/resources")
    }
    if (project.name in publicModules) {
        apply(plugin = "org.jetbrains.dokka")
        apply(plugin = "com.vanniktech.maven.publish.base")
        setupDokka()
        setupPublishing {
            val platform = if (project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
                KotlinMultiplatform(Dokka("dokkaGenerate"))
            } else {
                AndroidSingleVariantLibrary()
            }
            configure(platform)
        }
    }
    testOptions {
        unitTests.all { test ->
            test.testLogging {
                exceptionFormat = TestExceptionFormat.FULL
                showExceptions = true
                showStackTraces = true
                showCauses = false
            }
        }
    }
    action()
}

fun Project.setupPublishing(
    action: MavenPublishBaseExtension.() -> Unit = {},
) {
    extensions.configure<MavenPublishBaseExtension> {
        pomFromGradleProperties()
        publishToMavenCentral()
        signAllPublications()
        action()
    }
}

fun Project.setupDokka(
    action: DokkaExtension.() -> Unit = {},
) {
    extensions.configure<DokkaExtension> {
        dokkaPublications.configureEach {
            failOnWarning.set(true)
            suppressInheritedMembers.set(true)
        }
        dokkaSourceSets.configureEach {
            jdkVersion.set(8)
            skipDeprecated.set(true)

            externalDocumentationLinks.register("android") {
                url.set(uri("https://developer.android.com/reference/"))
            }
            externalDocumentationLinks.register("coroutines") {
                url.set(uri("https://kotlinlang.org/api/kotlinx.coroutines/"))
            }
            externalDocumentationLinks.register("skiko") {
                url.set(uri("https://jetbrains.github.io/skiko/"))
                packageListUrl.set(uri("https://jetbrains.github.io/skiko/skiko/package-list"))
            }
            externalDocumentationLinks.register("ktor") {
                url.set(uri("https://api.ktor.io/"))
            }
            externalDocumentationLinks.register("datetime") {
                url.set(uri("https://kotlinlang.org/api/kotlinx-datetime/"))
                packageListUrl.set(uri("https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/package-list"))
            }
            externalDocumentationLinks.register("okio") {
                url.set(uri("https://square.github.io/okio/3.x/okio/"))
                packageListUrl.set(uri("https://square.github.io/okio/3.x/okio/okio/package-list"))
            }
            externalDocumentationLinks.register("okhttp") {
                url.set(uri("https://square.github.io/okhttp/5.x/okhttp/okhttp3/"))
                packageListUrl.set(uri("https://square.github.io/okhttp/5.x/package-list"))
            }
        }
        action()
    }
}

fun Project.androidApplication(
    name: String,
    action: BaseAppModuleExtension.() -> Unit = {},
) = androidBase<BaseAppModuleExtension>(name) {
    defaultConfig {
        applicationId = name
        versionCode = project.versionCode
        versionName = project.versionName
        androidResources.localeFilters += "en"
        vectorDrawables.useSupportLibrary = true
    }
    action()
}

fun Project.androidTest(
    name: String,
    config: Boolean = false,
    action: TestExtension.() -> Unit = {},
) = androidBase<TestExtension>(name) {
    buildFeatures {
        buildConfig = config
    }
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }
    action()
}

private fun <T : BaseExtension> Project.androidBase(
    name: String,
    action: T.() -> Unit,
) {
    android<T> {
        namespace = name
        compileSdkVersion(project.compileSdk)
        defaultConfig {
            minSdk = project.minSdk
            targetSdk = project.targetSdk
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        packagingOptions {
            resources.pickFirsts += listOf(
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/*kotlin_module",
            )
        }
        testOptions {
            unitTests.isIncludeAndroidResources = true
        }
        lint {
            warningsAsErrors = true
            disable += listOf(
                "ComposableNaming",
                "UnknownIssueId",
                "UnsafeOptInUsageWarning",
                "UnusedResources",
                "UseSdkSuppress",
                "VectorPath",
                "VectorRaster",
            )
        }
        action()
    }
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.configureEach {
                languageSettings {
                    optIn("coil3.annotation.DelicateCoilApi")
                    optIn("coil3.annotation.ExperimentalCoilApi")
                    optIn("coil3.annotation.InternalCoilApi")
                }
            }
            targets.configureEach {
                compilations.configureEach {
                    // https://youtrack.jetbrains.com/issue/KT-61573#focus=Comments-27-9822729.0-0
                    @Suppress("DEPRECATION")
                    compilerOptions.configure {
                        val arguments = listOf(
                            // https://kotlinlang.org/docs/compiler-reference.html#progressive
                            "-progressive",
                            // https://youtrack.jetbrains.com/issue/KT-61573
                            "-Xexpect-actual-classes",
                        )
                        freeCompilerArgs.addAll(arguments)
                    }
                }
            }
        }
    }
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors.set(System.getenv("CI").toBoolean())

            val arguments = mutableListOf<String>()

            // https://kotlinlang.org/docs/compiler-reference.html#progressive
            arguments += "-progressive"

            // Enable Java default method generation.
            arguments += "-Xjvm-default=all"

            // Generate smaller bytecode by not generating runtime not-null assertions.
            arguments += "-Xno-call-assertions"
            arguments += "-Xno-param-assertions"
            arguments += "-Xno-receiver-assertions"

            if (project.name != "benchmark") {
                arguments += "-opt-in=coil3.annotation.DelicateCoilApi"
                arguments += "-opt-in=coil3.annotation.ExperimentalCoilApi"
                arguments += "-opt-in=coil3.annotation.InternalCoilApi"
            }

            freeCompilerArgs.addAll(arguments)
        }
    }
}

private fun <T : BaseExtension> Project.android(action: T.() -> Unit) {
    extensions.configure("android", action)
}

private fun BaseExtension.lint(action: Lint.() -> Unit) {
    (this as CommonExtension<*, *, *, *, *, *>).lint(action)
}
