# Andromeda
 A collection of Android libraries for simplifying development using Android APIs.

This is intended for my use only at this point, so there isn't any documentation on it.

## Usage
Andromeda uses Jitpack for distribution, the following gradle dependency can be added:

In the root build.gradle file:
```gradle
allprojects {
 repositories {
  ...
  maven { url 'https://jitpack.io' }
 }
}
```

In the project specific build.gradle file (all modules):
```gradle
dependencies {
 // Tag is the release number
 implementation 'com.github.kylecorry31:andromeda:Tag'
}
```

In the project specific build.gradle file (specific module):
```gradle
dependencies {
 // Tag is the release number
 // Module is the specific module name, it shares the folder name of the top level folders in this repo (ex. buzz)
 implementation 'com.github.kylecorry31.andromeda:Module:Tag'
}
```

See the releases page for the latest version.
