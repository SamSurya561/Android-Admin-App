// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.11.2" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
    // ** ADD THIS LINE **
    // This line makes the Google Services plugin available to the whole project.
    id("com.google.gms.google-services") version "4.4.3" apply false
}
