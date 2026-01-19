pluginManagement {
    repositories {
        // 优先使用腾讯云镜像源
        maven {
            url = uri("https://mirrors.tencent.com/gradle-plugin/")
        }
        maven {
            url = uri("https://mirrors.tencent.com/maven/central/")
        }
        maven {
            url = uri("https://mirrors.tencent.com/maven/google/")
        }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 优先使用腾讯云镜像源
        maven {
            url = uri("https://mirrors.tencent.com/maven/central/")
        }
        maven {
            url = uri("https://mirrors.tencent.com/maven/google/")
        }
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/")
        }
        // Cloudbase 官方仓库
        maven {
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "ShareAlarm"
include(":app")