import groovy.json.JsonSlurper
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")

    id("com.google.dagger.hilt.android")
}

tasks.named("preBuild") {
    dependsOn("createTranslationCredits")
}

val translationCredits = layout.buildDirectory.dir("generated/translationCredits/res/raw")
val envToken = System.getenv("WEBLATE_TOKEN")
    ?: providers.gradleProperty("WEBLATE_TOKEN").orNull
    ?: error("WEBLATE_TOKEN is not set")

val envWeblateUrl = System.getenv("WEBLATE_URL")
    ?: providers.gradleProperty("WEBLATE_URL").orNull
    ?: error("WEBLATE_URL is not set")


abstract class GenerateAuthorCredits : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val weblateUrl: Property<String>

    @get:Input
    abstract val weblateToken: Property<String>
}

tasks.register<GenerateAuthorCredits>("createTranslationCredits") {
    outputDir.set(translationCredits)
    weblateToken.set(envToken)
    weblateUrl.set(envWeblateUrl)

    doLast {
        println("Generating translation credits")

        val today = LocalDate.now()
            .atTime(23, 59, 59)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        fun get(url: String): String {
            val connection = URI(url).toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            connection.setRequestProperty("Authorization", "Token ${weblateToken.get()}")

            return connection.inputStream.bufferedReader().use { it.readText() }
        }

        fun post(url: String, json: String): String {
            val connection = URI(url).toURL().openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Token ${weblateToken.get()}")

            connection.outputStream.use {
                it.write(json.toByteArray(Charsets.UTF_8))
            }

            return connection.inputStream.bufferedReader().use { it.readText() }
        }

        val requestReport = post("${weblateUrl.get()}/api/reports/", """
            {
                "kind": "credits",
                "project": "readbear",
                "start": "1970-01-01 00:00:00",
                "end": "$today"
            }
        """.trimIndent())

        var parsed = JsonSlurper().parseText(requestReport) as Map<*, *>
        val taskUrl = parsed["task_url"] as String
        var completed = false
        var response = ""

        while(!completed) {
            response = get("${weblateUrl.get()}${taskUrl}")
            parsed = JsonSlurper().parseText(response) as Map<*, *>
            completed = parsed["completed"] as Boolean
        }

        parsed = parsed["result"] as Map<*, *>
        response = get("${weblateUrl.get()}${parsed["url"]}json/")

        val withoutEmail = response.replace(
            Regex(""""email"\s*:\s*"[^"]*",?\s*"""),
            ""
        )

        val withoutFullName = withoutEmail.replace(
            Regex(""""full_name"\s*:\s*"[^"]*",?\s*"""),
            ""
        )

        val cleaned = withoutFullName.replace(Regex(""",\s*}"""), "}")
            .replace(Regex(""",\s*]"""), "]")


        val dir = outputDir
            .get()
            .asFile

        dir.mkdirs()

        File(dir, "translation_credits.json").writeText(
            cleaned.trimIndent()
        )
    }
}

android {
    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("../release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    sourceSets {
        getByName("main") {
            resources {
                res {
                    directories.add(
                        layout.buildDirectory
                            .dir("generated/translationCredits/res")
                            .get()
                            .asFile
                            .absolutePath
                    )
                }
            }
        }
    }

    namespace = "dev.tulis.readbear"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "dev.tulis.readbear"
        minSdk = 24
        targetSdk = 37
        versionCode = 3
        versionName = "v0.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.core)
//    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation("androidx.activity:activity-compose:1.10.1")

    implementation(platform(
        "androidx.compose:compose-bom:2025.07.00"
    ))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")

    implementation("io.coil-kt.coil3:coil-compose:3.5.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.5.0")



    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("androidx.navigation:navigation-compose:2.9.0")

    implementation("androidx.room:room-runtime:2.8.0")
    implementation("androidx.room:room-ktx:2.8.0")
    ksp("androidx.room:room-compiler:2.8.0")


    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.animation:animation-core")

    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-android-compiler:2.59.2")

    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("io.github.yuroyami:kitepdf:0.7.0")
    implementation("io.github.yuroyami:kitepdf-compose-viewer:0.7.0")

    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    implementation("dev.nucleusframework:pdfium:154.0.8021.0b")

}
