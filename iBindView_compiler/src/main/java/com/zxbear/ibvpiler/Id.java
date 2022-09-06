package com.zxbear.ibvpiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.sun.tools.javac.code.Symbol;

public class Id {
    private static final ClassName ANDROID_R = ClassName.get("android", "R");
    private static final String R = "R";

    private int value;
    private CodeBlock code;

    public Id(int value) {
        this(value, null);
    }

    public Id(int value, Symbol rSymbol) {
        this.value = value;
        if (rSymbol != null){
            ClassName className = ClassName.get(rSymbol.packge().getQualifiedName().toString(), R,
                    rSymbol.enclClass().name.toString());
            String resourceName = rSymbol.name.toString();
            this.code = className.topLevelClassName().equals(ANDROID_R)
                    ? CodeBlock.of("$L.$N", className, resourceName)
                    : CodeBlock.of("$T.$N", className, resourceName);
        }else {
            this.code = CodeBlock.of("$L", value);
        }
    }

    public int getValue() {
        return value;
    }

    public CodeBlock getCode() {
        return code;
    }
}
