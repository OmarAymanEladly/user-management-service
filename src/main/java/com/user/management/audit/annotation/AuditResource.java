package com.user.management.audit.annotation;

import com.user.management.audit.enumeration.ResourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditResource {
    ResourceType type();
    String idSpEL() default "";
}