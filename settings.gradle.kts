pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
            maven { url = uri("https://artifacts.mercadolibre.com/repository/android-releases") }
            content {
                includeGroup("com.mercadopago.android.sdk")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://artifacts.mercadolibre.com/repository/android-releases")
            content {
                includeGroup("com.mercadopago.android.sdk")
            }
        }
    }
}

rootProject.name = "MobileApp"
include(":app")
 