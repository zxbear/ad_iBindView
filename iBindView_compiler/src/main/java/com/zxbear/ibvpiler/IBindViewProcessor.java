package com.zxbear.ibvpiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;
import com.zxbear.ibvannot.IBindView;
import com.zxbear.ibvannot.IBindViews;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;


@AutoService(Processor.class)//注解器注册
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.DYNAMIC)//支持注解器增量编译
@SupportedSourceVersion(SourceVersion.RELEASE_8)//支持的 Java 版本
public class IBindViewProcessor extends AbstractProcessor {
    private Filer filer;
    private Elements elementUtils;
    public static final String VIEW_TYPE = "android.view.View";
    public static final String LIST_TYPE = "java.util.List";
    private Trees trees;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        try {
            //task；compileDebugJavaWithJavac Trees maybe is null
            trees = Trees.instance(processingEnv);
        } catch (IllegalArgumentException ignored) {
            //Get original ProcessingEnvironment from Gradle-wrapped one or KAPT-wrapped one
            try {
                for (Field field : processingEnv.getClass().getDeclaredFields()) {
                    if (field.getName().equals("delegate") || field.getName().equals("processingEnv")) {
                        field.setAccessible(true);
                        ProcessingEnvironment javacEnv = (ProcessingEnvironment) field.get(processingEnv);
                        trees = Trees.instance(javacEnv);
                        break;
                    }
                }
            } catch (Throwable ignored2) {
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(IBindView.class.getName());
        annotations.add(IBindViews.class.getName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
        if (!set.isEmpty()) {
            Map<TypeElement, BindingSet> codeMap = findAndParseTargets(env);
            for (Map.Entry<TypeElement, BindingSet> item : codeMap.entrySet()) {
                try {
                    item.getValue().getJavaFileBulid(elementUtils.getPackageOf(item.getKey()))
                            .build()
                            .writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    private Map<TypeElement, BindingSet> findAndParseTargets(RoundEnvironment env) {
        Map<TypeElement, BindingSet> spCode = new HashMap<>();
        for (Element item : env.getElementsAnnotatedWith(IBindView.class)) {
            TypeElement tE = (TypeElement) item.getEnclosingElement();
            boolean isField = true;
            if (!isExtendsofTyoe(item.asType(), VIEW_TYPE)) {
                errorPrint(IBindView.class.getSimpleName() + " fields must extend from View or be an interface.( " +
                        tE.getQualifiedName() + "." + item.getSimpleName() + ")", tE);
                isField = false;
            }
            if (isField) {
                if (!spCode.containsKey(tE)) {
                    BindingSet bindingSet = new BindingSet();
                    bindingSet.init(tE);
                    spCode.put(tE, bindingSet);
                }
                //item.getAnnotation(IBindView.class)
                spCode.get(tE).addField(item, getViewIdParmsByBindViewsElement(item)[0]);
                //spCode.get(tE).addField(item, item.getAnnotation(IBindView.class).value()+"");
            }
        }

        for (Element item : env.getElementsAnnotatedWith(IBindViews.class)) {
            //获取泛型
            String pars = item.asType().toString();
            if (pars.contains("<")) {
                if (pars.substring(0,pars.indexOf("<")).equals(LIST_TYPE)){
                    String fxAll = pars.substring(pars.indexOf("<") + 1, pars.indexOf(">"));
                    String pack = fxAll.substring(0, fxAll.lastIndexOf("."));
                    String clName = fxAll.substring(fxAll.lastIndexOf(".") + 1);
                    ClassName cls = ClassName.get(pack, clName);
                    if (cls != null) {
                        TypeElement tE = (TypeElement) item.getEnclosingElement();
                        BindingSet bindingSet;
                        if (!spCode.containsKey(tE)) {
                            bindingSet = new BindingSet();
                            bindingSet.init(tE);
                            spCode.put(tE, bindingSet);
                        }
                        spCode.get(tE).addFields(item, cls, getViewIdParmsByBindViewsElement(item));
                    } else {
                        errorPrint("The list <T> cast error", item);
                    }
                }else {
                    errorPrint("must be a List or array", item);
                }
            } else {
                errorPrint("LIST IS ADD <>", item);
            }
        }
        return spCode;
    }


    /**
     * 获取节点的继承关系-判断是否是view
     *
     * @param typeMirror
     * @param otherType
     * @return
     */
    private boolean isExtendsofTyoe(TypeMirror typeMirror, String otherType) {
        //是否是otherType(例如：android.view.View)类型
        if (otherType.equals(typeMirror.toString())) {
            return true;
        }
        //判断类或接口类型
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        //获取父类
        DeclaredType declaredType = (DeclaredType) typeMirror;
        if (!(declaredType.asElement() instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) declaredType.asElement();
        TypeMirror superType = typeElement.getSuperclass();
        if (isExtendsofTyoe(superType, otherType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isExtendsofTyoe(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 错误信息打印
     *
     * @param msg
     * @param element
     */
    private void errorPrint(String msg, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element);
    }

    /**
     * 获取IBindView的树节点，拿到R.id.xx-字段
     *
     * @param element
     * @return
     */
    private String[] getViewIdParmsByBindViewsElement(Element element) {
        JCTree tree = (JCTree) trees.getTree(element, getMirror(element, IBindView.class));
        String idName = tree.toString().replace("R2", "R");
        if (idName.contains("{")) {
            return idName.substring(idName.indexOf("{") + 1, idName.indexOf("}")).split(",");
        } else {
            int end = idName.lastIndexOf(")");
            return new String[]{idName.substring(idName.indexOf("R"), end)};
        }
    }

    private static AnnotationMirror getMirror(Element element,
                                              Class<? extends Annotation> annotation) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            //System.out.println("AnnotationMirror => " + annotationMirror.getAnnotationType());
            if (annotationMirror.getAnnotationType().toString().equals(annotation.getCanonicalName())) {
                return annotationMirror;
            }
        }
        return null;
    }

    /**
     * 判断注解是否可用
     * 是否是feidID、注解对象修饰符
     *
     * @param annotationClass
     * @param targetThing
     * @param element
     * @return
     */
    private boolean isInaccessibleViaGeneratedCode(Class<? extends Annotation> annotationClass,
                                                   String targetThing, Element element) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        Set<Modifier> modifiers = element.getModifiers();
        //判断是否为PRIVATE||STATIC
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
            hasError = true;
        }
        //判断是否为CLASS
        if (enclosingElement.getKind() != ElementKind.CLASS) {
            hasError = true;
        }
        //
        if (enclosingElement.getModifiers().contains(Modifier.PRIVATE)) {
            hasError = true;
        }
        return hasError;
    }

}