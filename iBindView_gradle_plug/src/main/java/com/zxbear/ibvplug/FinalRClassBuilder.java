package com.zxbear.ibvplug;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;


public class FinalRClassBuilder {
    //"anim", "array", "attr", "bool", "color", "dimen", "drawable", "id", "integer", "layout", "menu", "plurals", "string", "style", "styleable"
    @NotNull
    private static Set SUPPORTED_TYPES;
    private static Map<String, String> annotatationMap;
    private Map<String, String> rMap;
    private String packageName;
    private String className;
    private int intDex;

    public FinalRClassBuilder(@NotNull String packageName, @NotNull String className) {
        this.packageName = packageName;
        this.className = className;
        SUPPORTED_TYPES = new HashSet();
        annotatationMap = new HashMap<>();
        rMap = new HashMap<>();
        SUPPORTED_TYPES.add("id");
        SUPPORTED_TYPES.add("layout");
        annotatationMap.put("id","IdRes");
        annotatationMap.put("layout","LayoutRes");
        intDex = 1;
    }

    /**
     * 初始化R文件
     *
     * @param rFile
     */
    public void initRFile(File rFile) {
        if (null != rFile && rFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(rFile);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader reader = new BufferedReader(isr);
                String res = "";
                while ((res = reader.readLine()) != null) {
                    if (res.contains("int")) {
                        //int id tv_module 0x0 --- 获取第二个空格
                        int star = res.indexOf(" ", res.indexOf(" ") + 1);
                        String key = res.substring(4, star);
                        if (SUPPORTED_TYPES.contains(key)) {
                            int end = res.lastIndexOf(" ");
                            String value = res.substring(star + 1, end);
                            rMap.put(key, rMap.containsKey(key) ? rMap.get(key) + "," + value : value);
                        }
                    }
                }
                reader.close();
                isr.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @NotNull
    public JavaFile build() {
        //构建R2类
        TypeSpec.Builder result = TypeSpec
                .classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        //构建静态内部类，id、layout
        Iterator iterator = SUPPORTED_TYPES.iterator();
        while (iterator.hasNext()) {
            String type = iterator.next().toString();
            TypeSpec.Builder tps = TypeSpec.classBuilder(type)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
            addResourceField(type, tps);
            result.addType(tps.build());
        }

        return JavaFile.builder(
                        packageName,
                        result.build()).addFileComment("Generated code from IBindView gradle plugin. Do not modify!")
                .build();
    }

    /**
     * 将R文件的资源加入进来
     *
     * @param type
     * @param tps
     */
    private void addResourceField(String type, TypeSpec.Builder tps) {
        if (rMap.containsKey(type)) {
            String[] values = rMap.get(type).split(",");
            //System.out.println(IBindViewPlug.TAG + "key=" + type + " value=" + rMap.get(type));
            for (String item : values) {
                FieldSpec.Builder fiedlBuild = FieldSpec.builder(Integer.TYPE, item)
                        .addAnnotation(ClassName.get("androidx.annotation", annotatationMap.get(type)))
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer(intDex + "");
                tps.addField(fiedlBuild.build());
                intDex++;
            }
        }
    }
}
