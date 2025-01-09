# location-coroutine

[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/crow-misia/location-coroutines/android.yml)](https://github.com/crow-misia/location-coroutines/actions/workflows/android.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.crow-misia.location-coroutines/location-coroutines)](https://central.sonatype.com/artifact/io.github.crow-misia.location-coroutines/location-coroutines)
[![GitHub License](https://img.shields.io/github/license/crow-misia/location-coroutines)](LICENSE)

Coroutines Function for FusedLocationProviderClient.

## Get Started

### Gradle

Add dependencies (you can also add other modules that you need):

`${latest.version}` is [![Maven Central Version](https://img.shields.io/maven-central/v/io.github.crow-misia.location-coroutines/location-coroutines)](https://central.sonatype.com/artifact/io.github.crow-misia.location-coroutines/location-coroutines)

```groovy
dependencies {
    implementation "io.github.crow-misia.location-coroutines:location-coroutines:${latest.version}"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-play-services" (optional)
    implementation "com.google.android.gms:play-services-location" (optional)
}
```

Make sure that you have either `mavenCentral()` in the list of repositories:

```
repository {
    mavenCentral()
}
```

## License

```
Copyright 2021, Zenichi Amano.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
