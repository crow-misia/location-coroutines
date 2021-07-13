object Deps {
    const val androidPlugin = "com.android.tools.build:gradle:${Versions.gradlePlugin}"
    const val dokkaPlugin = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokkaPlugin}"

    const val kotlinxCoroutinesBom = "org.jetbrains.kotlinx:kotlinx-coroutines-bom:${Versions.kotlinxCoroutines}"
    const val kotlinxCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core"
    const val kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android"
    const val kotlinxCoroutinesPlayServices = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services"
    const val playServiceLocation = "com.google.android.gms:play-services-location:${Versions.playServicesLocation}"
}
