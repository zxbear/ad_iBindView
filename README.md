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
2. Used in the Activity
 ```
public class MainActivity extends AppCompatActivity {
    //Annotating a single View
    @IBindView(R.id.tv)
    public TextView tv;
    
    //Annotating multiple Views
    @IBindViews({R.id.btn1,R.id.btn2,R.id.btn3})
    List<Button> btns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize the view
        IBinds.bind(this);
        //Using ids
        tv.setText(btns.size());
    }
}
 ```
 
 **II.Libray additional Configuration**
1. Adding dependencies in your project build.gradle
 ```
buildscript {
    dependencies {
        ...
        //add Plug-in dependencies
        classpath 'com.github.zxbear.ad_iBindView:iBindView_gradle_plug:v1.0.98'
    }
}
 ```
2. Adding plug in your libray build.gradle
 ```
 ...
 //add plugin
apply plugin: 'com.zxbear.IBindView'

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
3. Used in the activity
 ```
 public class MainActivity extends AppCompatActivity {
    //user R2
    @IBindView(R2.id.tv)
    public TextView tv;

    @IBindViews({R2.id.image1,R2.id.image2})
    List<ImageView> imgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IBinds.bind(this);

        tv.setText(imgs.size());
    }
}
 ```
