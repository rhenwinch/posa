import com.codingfeline.buildkonfig.compiler.FieldSpec
import de.jensklingenberg.ktorfit.gradle.ErrorCheckingMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.kmp.nativeCoroutines)
    alias(libs.plugins.room)
    alias(libs.plugins.buildKonfig)
}

room {
    schemaDirectory("$projectDir/schemas")
}

ktorfit {
    compilerPluginVersion.set("2.3.3")
    errorCheckingMode = ErrorCheckingMode.ERROR
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets.all {
        languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
    }

    android {
        namespace = "io.posa.shared"
        minSdk = libs.versions.android.minSdk.get().toInt()
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        androidResources {
            enable = true
        }

        withHostTest {
            isIncludeAndroidResources = true
        }

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            execution = "HOST"
        }
    }

    val xcFramework = XCFramework()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
            xcFramework.add(this)
            // Required when using NativeSQLiteDriver
            linkerOpts.add("-lsqlite3")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)

            // TODO: Remove these compose dependencies here for better separation of concerns.
            api(libs.androidx.lifecycle.viewmodelCompose)
            api(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.ktorfit)
            implementation(libs.ktorfit.converters.response)
            implementation(libs.ktorfit.converters.flow)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.room.runtime)
            implementation(libs.room.sqlite.bundled)

            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            implementation(libs.kermit)
        }
        commonTest.dependencies {
            implementation(libs.junit)
            implementation(libs.koin.test)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
        androidMain.dependencies {
            api(libs.koin.android)
            implementation(libs.room.sqlite.wrapper)
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)

    add("androidDeviceTestImplementation", libs.junit)
    add("androidDeviceTestImplementation", libs.androidx.testExt.junit)
    add("androidDeviceTestImplementation", libs.androidx.espresso.core)
    add("androidDeviceTestImplementation", libs.kotlinx.coroutines.test)
}

buildkonfig {
    packageName = "io.posa.core.common"

    defaultConfigs {
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        buildConfigField(FieldSpec.Type.STRING, "CAT_API_KEY", (properties["CAT_API_KEY"] as String?) ?: "")
    }
}

tasks.named("assemble") {
    dependsOn("assembleXCFramework")
}

tasks.named("assembleXCFramework") {
    finalizedBy("generatePackageSwift")
}

val xcFrameworkPath = "build/XCFrameworks/release/shared.xcframework"
tasks.register("generatePackageSwift") {
    group = "build"
    description = "Generates Package.swift for Swift Package Manager"

    doLast {
        val packageSwiftContent = """
            // swift-tools-version: 6.2
            import PackageDescription

            let package = Package(
                name: "shared",
                platforms: [
                    .iOS(.v14)
                ],
                products: [
                    .library(
                        name: "shared",
                        targets: ["shared"]
                    )
                ],

                targets: [
                    .binaryTarget(
                        name: "shared",
                        path: "./$xcFrameworkPath"
                    )
                ]
            )
        """.trimIndent()

        val packageSwiftFile = File("$projectDir/Package.swift")
        packageSwiftFile.writeText(packageSwiftContent)
        println("✅ Package.swift updated successfully.")
    }
}