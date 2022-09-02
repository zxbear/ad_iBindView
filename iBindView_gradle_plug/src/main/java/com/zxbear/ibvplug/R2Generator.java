package com.zxbear.ibvplug;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

public class R2Generator extends DefaultTask {
    @Nullable
    private File outputDir;// 输出文件路径
    @Nullable
    private FileCollection rFile;
    @Nullable
    private String packageName;//包名
    @Nullable
    private String className;//类名

    @OutputDirectory
    @Nullable
    public File getOutputDir() {
        return this.outputDir;
    }

    public void setOutputDir(@Nullable File var1) {
        this.outputDir = var1;
    }

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    @Nullable
    public FileCollection getRFile() {
        return this.rFile;
    }

    public void setRFile(@Nullable FileCollection var1) {
        this.rFile = var1;
    }

    @Input
    @Nullable
    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(@Nullable String var1) {
        this.packageName = var1;
    }

    @Input
    @Nullable
    public String getClassName() {
        return this.className;
    }

    public void setClassName(@Nullable String var1) {
        this.className = var1;
    }

    @TaskAction
    public void brewJava() {
        if (rFile == null) throw (new RuntimeException("the rFile is null"));
        File var1 = rFile.getSingleFile();
        if (outputDir == null) throw (new RuntimeException("the outputDir is null"));
        if (packageName == null) throw (new RuntimeException("the packageName is null"));
        if (className == null) throw (new RuntimeException("the className is null"));
        FinalRClassBuilder frcb = new FinalRClassBuilder(packageName, className);
        frcb.initRFile(var1);
        try {
            frcb.build().writeTo(outputDir);
           // System.out.println(IBindViewPlug.TAG + outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
