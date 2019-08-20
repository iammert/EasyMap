# EasyMap
Ready to use address selection activity using Google Maps.

# Demo

# Implementation 

## Enable Google Maps and Places APIs
* Go to google maps api page from google cloud console from [here](https://console.cloud.google.com/google/maps-apis/apis/maps-android-backend.googleapis.com/metrics?). Enable it from the top of the page.
* Go to google places api page from google cloud console from [here](https://console.cloud.google.com/google/maps-apis/apis/places-backend.googleapis.com/metrics?). Enable it from the top of the page.

## Get the API Key from Console

* Follow [this](https://developers.google.com/maps/documentation/android-sdk/get-api-key) link and get API key from Google Cloud Console.

* In AndroidManifest.xml, add the following element as a child of the <application> element, by inserting it just before the closing </application> tag:
```xml
<meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="YOUR_API_KEY"/>
```

# Dependency

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.momento-lab:EasyMap:0.1'
}
```


License
--------


    Copyright 2019 Mert Şimşek

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

