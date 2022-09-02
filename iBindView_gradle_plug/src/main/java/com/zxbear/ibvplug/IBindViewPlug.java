package com.zxbear.ibvplug;


import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.LibraryPlugin;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.api.BaseVariantImpl;
import com.android.build.gradle.internal.res.GenerateLibraryRFileTask;
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask;
import com.android.build.gradle.tasks.ProcessAndroidResources;
import com.android.builder.model.SourceProvider;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.ParserConfigurationException;

import groovy.util.XmlSlurper;


public class IBindViewPlug implements Plugin<Project> {
    public static final String TAG = "IBindViewPlug ==> ";

    @Override
    public void apply(Project project) {
        project.getPlugins().all(plugin -> {
            //判断是否为library
            if (plugin instanceof LibraryPlugin) {
                LibraryExtension libExt = project.getExtensions().getByType(LibraryExtension.class);
                configureR2Generation(project, libExt.getLibraryVariants());
            }
        });

    }

    private void configureR2Generation(Project project, DomainObjectSet variants) {
        variants.all(obj -> {
            BaseVariantImpl variant = (BaseVariantImpl) obj;
            //构建R2文件路径
            String outPath = project.getBuildDir().getAbsolutePath()
                    + "/generated/source/r2/"
                    + variant.getDirName();
            File outputDir = new File(outPath);
            AtomicBoolean once = new AtomicBoolean();
            variant.getOutputs().all(output -> {
                if (once.compareAndSet(false, true)) {
                    ProcessAndroidResources processResources = output.getProcessResourcesProvider().get();
                    File[] files = new File[1];//objs[0]==file
                    if (processResources instanceof GenerateLibraryRFileTask) {
                        files[0] = ((GenerateLibraryRFileTask) processResources).getTextSymbolOutputFile();
                    } else {
                        if (!(processResources instanceof LinkApplicationAndroidResourcesTask)) {
                            throw (new RuntimeException("Minimum supported Android Gradle Plugin is 3.3.0"));
                        }
                        files[0] = ((LinkApplicationAndroidResourcesTask) processResources).getTextSymbolOutputFile();
                    }
                    ConfigurableFileCollection rFile = project.files(files[0]).builtBy(processResources);
                    //做一个判断，系统R文件是否存在
                    if (files.length == 0 || !files[0].exists()) {
                        return;
                    }
                    //构建Tasks目录
                    String sours = "generate" + converInitCaper(variant.getName()) + "R2";
                    R2Generator generate = project.getTasks().create(sours, R2Generator.class, it -> {
                        //R2输出文件路径
                        it.setOutputDir(outputDir);
                        //module的R文件
                        it.setRFile(rFile);
                        //R2包名
                        it.setPackageName(getPackageName(variant));
                        //class名称
                        it.setClassName("R2");
                    });
                    //在对应的目录生成R2.java
                    //注册Task及目录
                    variant.registerJavaGeneratingTask(generate, outputDir);
                }
            });
        });
    }

    /**
     * 获取当前节点，所在的包名
     * @param variant
     * @return
     */
    private String getPackageName(BaseVariant variant) {
        try {
            XmlSlurper slurper = new XmlSlurper(false, false);
            List list = new ArrayList();
            for (SourceProvider item : variant.getSourceSets()) {
                list.add(item.getManifestFile());
            }
            if (list != null && list.size() > 0) {
                return slurper.parse((File) list.get(0)).getProperty("@package").toString();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 首字母转大写
     *
     * @param str
     * @return
     */
    private String converInitCaper(String str) {
        if (str != null && !str.equals("")) {
            char[] chars = str.toCharArray();
            chars[0] -= 32;
            return new String(chars);
        }
        return str;
    }
}