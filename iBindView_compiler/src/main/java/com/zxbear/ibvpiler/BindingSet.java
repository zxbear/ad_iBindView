package com.zxbear.ibvpiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

public class BindingSet {
    private TypeElement tElement;
    //构建类
    private TypeSpec.Builder classSpec;
    //构建方法
    private MethodSpec.Builder methodSpec;
    private MethodSpec.Builder unIBindMethod;//UnIBind实现方法
    //构建方法体入参
    private ParameterSpec.Builder ptSpec, ptSpec2;
    //静态定义参数
    private final static String csName = "_IBindView";
    private final static String par_name = "target";
    private final static String par2_name = "source";
    //CLASS
    public static final ClassName UTILS = ClassName.get("com.zxbear.ibvapi", "IBindUtils");
    public static final ClassName UnIBind = ClassName.get("com.zxbear.ibvapi", "UnIBind");
    public static final ClassName UiThread = ClassName.get("androidx.annotation", "UiThread");
    public static final ClassName IllegalStateException = ClassName.get("java.lang", "IllegalStateException");
    public static final ClassName view = ClassName.get("android.view", "View");
    //UnIBind方法体内容
    private StringBuilder unIBindStr;

    public void init(TypeElement tE) {
        this.tElement = tE;
        //获取入参的activity定义
        TypeName paramType = TypeName.get(tElement.asType());
        ptSpec = ParameterSpec.builder(paramType, par_name);
        ptSpec2 = ParameterSpec.builder(view, par2_name);
        //构造方法
        methodSpec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ptSpec.build())
                .addParameter(ptSpec2.build());
        //UnIBind实现方法
        unIBindMethod = MethodSpec.methodBuilder("unbind")
                .addAnnotation(UiThread)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);
        initUnIBindStr();
        //类
        classSpec = TypeSpec.classBuilder(tElement.getSimpleName() + csName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(UnIBind)
                .addField(FieldSpec.builder(paramType, par_name, Modifier.PRIVATE).build());
    }

    /**
     * 初始化UnIBind方法体必要参数
     */
    private void initUnIBindStr() {
        unIBindStr = new StringBuilder();
        unIBindStr.append("$T " + par_name + " = this." + par_name + ";\n");
        unIBindStr.append("if(" + par_name + "== null) throw new $T(\"Bindings already cleared.\");\n");
    }

    /**
     * 构造方法添加方法体
     *
     * @param element
     */
    public void addField(Element element, String idName) {
        String targetParm = par_name + "." + element.getSimpleName();
        StringBuilder sb = new StringBuilder();
        sb.append(targetParm + " = ");
        sb.append("$T.findRequiredViewAsType($L, $L, $T.class)");
        methodSpec.addStatement(sb.toString(),
                UTILS,
                par2_name,
                idName,
                element.asType()
        );
        //bind参数置空
        unIBindStr.append(targetParm + " = null;\n");
    }

    public void addFields(Element element, ClassName viewClass, String[] idNames) {
        if (null != idNames && idNames.length > 0) {
            String targetParm = par_name + "." + element.getSimpleName();
            CodeBlock.Builder code = CodeBlock.builder();
            code.add(targetParm + " = ");
            code.add("$T.listFilteringNull(", UTILS);
            for (int i = 0; i < idNames.length; i++) {
                if (i != idNames.length - 1) {
                    code.add("$T.findRequiredViewAsType($L, $L, $T.class),",
                            UTILS,
                            par2_name,
                            idNames[i],
                            viewClass);
                } else {
                    code.add("$T.findRequiredViewAsType($L, $L, $T.class)",
                            UTILS,
                            par2_name,
                            idNames[i],
                            viewClass);
                }
            }
            code.add(")");
            methodSpec.addStatement(code.build());
        }
    }

    /**
     * @param packName
     * @return
     */
    public JavaFile.Builder getJavaFileBulid(PackageElement packName) {
        if (classSpec != null && methodSpec != null) {
            classSpec.addMethod(methodSpec.build());
            unIBindMethod.addStatement(unIBindStr.substring(0, unIBindStr.length() - 2),
                    TypeName.get(tElement.asType())
                    , IllegalStateException);
            classSpec.addMethod(unIBindMethod.build());
        }
        return JavaFile.builder(packName.toString(), classSpec.build());
    }
}
