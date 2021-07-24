package com.stitch.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SecuredAccess
{
    String checkPost();
    String guard();
}