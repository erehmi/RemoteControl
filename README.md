# RemoteControl

Remote control components(views) for Android.

![](https://github.com/erehmi/RemoteControl/raw/master/screenshot.jpg)

## Gradle

```
implementation 'com.github.erehmi:remotecontrol:1.0.0'
```

## Usage

### Create ArrowKeyGroup in XML

```
<com.github.erehmi.remotecontrol.ArrowKeyGroup
    xmlns:remotecontrol="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layerType="software"
    android:rotation="45"
    remotecontrol:button="@mipmap/ic_arrow_key_right"
    remotecontrol:buttonBackgroundColor="@color/arrow_key_button"
    remotecontrol:okButtonText="@string/app_name"
    remotecontrol:okButton="@mipmap/ic_launcher"
    remotecontrol:okButtonBackgroundColor="@color/arrow_key_button"
    remotecontrol:innerRadius="@dimen/arrow_key_group_inner_radius"
    remotecontrol:innerRadiusRatio="1"
    remotecontrol:thickness="@dimen/arrow_key_group_thickness"
    remotecontrol:thicknessRatio="1"
    remotecontrol:useDefaultMargin="false" />
```

### Tips
#### 1. Enable ArrowKeyGroup shadow
You should assign `android:layerType` to `software`.

##### In XML:
```
android:layerType="software"
```
##### Programmatically:
```
setLayerType(View.LAYER_TYPE_SOFTWARE, null) 
```

#### 2. Anti alias
If the parent background's background color and button background color are different, it works like this:

![](https://github.com/erehmi/RemoteControl/raw/master/screenshot-2.jpg)

Avoid that, you should do like this:

##### In XML:
```
android:rotation="45"
```
##### Programmatically:
```
setAntiAliasWhenDefaultMarginNotEnabled(true) 
```
or
```
setRotation(45.f)
```

![](https://github.com/erehmi/RemoteControl/raw/master/screenshot-3.jpg)

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