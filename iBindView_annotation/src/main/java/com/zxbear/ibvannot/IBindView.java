package com.zxbear.ibvannot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IdRes;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IBindView {
  @IdRes int value();
}