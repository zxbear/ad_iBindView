package com.zxbear.ibvapi;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import java.lang.reflect.Constructor;
import java.util.Objects;

public final class IBinds {

    public static UnIBind bind(Activity activity) {
        View view = activity.getWindow().getDecorView();
        return bind(activity, view);
    }

    public static UnIBind bind(Dialog dialog) {
        View view = dialog.getWindow().getDecorView();
        return bind(dialog, view);
    }


    public static UnIBind bind(Object target, View source) {
        Constructor<? extends UnIBind> constructor = findBindingConstructorForClass(target.getClass());
        if (constructor == null) {
            return UnIBind.EMPTY;
        }
        try {
            return constructor.newInstance(target, source);
        } catch (Exception e) {
            throw new RuntimeException("Unable to invoke " + constructor, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Constructor<? extends UnIBind> findBindingConstructorForClass(Class<?> cls) {
        Constructor<? extends UnIBind> bindingCtor = null;
        String clsName = cls.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.")
                || clsName.startsWith("androidx.")) {
            return null;
        }
        try {
            ClassLoader classLoader = cls.getClassLoader();
            if (classLoader != null) {
                Class<?> bindingClass = classLoader.loadClass(cls.getName() + "_IBindView");
                if (bindingClass!=null){
                    //这是个 未进行安全检查的
                    bindingCtor = (Constructor<? extends UnIBind>) bindingClass.getConstructor(cls, View.class);
                }
            }
        } catch (ClassNotFoundException e) {
            bindingCtor = findBindingConstructorForClass(Objects.requireNonNull(cls.getSuperclass()));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find binding constructor for " + clsName, e);
        }
        return bindingCtor;
    }
}
