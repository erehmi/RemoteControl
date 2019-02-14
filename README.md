# RemoteControl

Remote control components for Android.

![](https://github.com/erehmi/RemoteControl/raw/master/screenshot.jpg)

## Gradle

```
implementation 'com.github.erehmi:remotecontrol:1.0.0'
```

## Usage

### Create ArrowKeyGroup in XML

```
<com.github.erehmi.remotecontrol.ArrowKeyGroup
    xmlns:nexus="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layerType="software"
    android:rotation="45"
    nexus:button="@mipmap/ic_arrow_key_right"
    nexus:buttonBackgroundColor="@color/arrow_key_button"
    nexus:okButtonText="@string/app_name"
    nexus:okButton="@mipmap/ic_launcher"
    nexus:okButtonBackgroundColor="@color/arrow_key_button"
    nexus:innerRadius="@dimen/arrow_key_group_inner_radius"
    nexus:innerRadiusRatio="1"
    nexus:thickness="@dimen/arrow_key_group_thickness"
    nexus:thicknessRatio="1"
    nexus:useDefaultMargin="false" />
```

## License

```
Copyright 2019, erehmi

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