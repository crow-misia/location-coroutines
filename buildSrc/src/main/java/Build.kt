import org.gradle.api.JavaVersion

object Build {
    const val COMPILE_SDK = 35
    const val MIN_SDK = 21
    const val TARGET_SDK = 35
    val sourceCompatibility = JavaVersion.VERSION_1_8
    val targetCompatibility = JavaVersion.VERSION_1_8
}
