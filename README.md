```
The purpose of the framework is to help developers initialize annotations on Views
```

**I.Necessary Configuration**
1. Adding dependencies and configurations in module build.gradle
 ```
 android {
  ...
  // iBindView requires Java 8.
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}

dependencies {
  implementation 'com.github.zxbear.ad_iBindView:iBindView_api:v1.1.1'
  annotationProcessor 'com.github.zxbear.ad_iBindView:iBindView_compiler:v1.1.1'
}
 ```
