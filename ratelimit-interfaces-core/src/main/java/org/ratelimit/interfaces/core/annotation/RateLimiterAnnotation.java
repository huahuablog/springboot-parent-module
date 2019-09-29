package org.ratelimit.interfaces.core.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 *声明定义注解类 格式：public @interface 注解名 {定义体}
 * @interface DBColumnSetting定义了一个注解@RateLimiterAnnotation,注解的是一个类
 * 当其他的Entity类使用了该注解@RateLimiterAnnotation,就可以设置改注解里面包含的属性，
 * 
    key--表示限流模块名，指定该值用于区分不同应用，不同场景，推荐格式为：应用名:模块名:ip:接口名:方法名
    limit--表示单位时间允许通过的请求数
    expire--incr的值的过期时间，业务中表示限流的单位时间。

 * 拓展知识:
 * 	1.使用@interface自定义注解时，自动继承注解接口annotation接口，
 * 常用的几个注解：
 * @Target(ElementType.FIELD) :元注释类型，可以与枚举类型的常量提供一个简单的分类,本次是提供在方法上的注解
 * @Retention(RetentionPolicy.RUNTIME) :指定注释要保留多长时间
 * @Documented ：注释表明是由javadoc记录的。
 * 
 */


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiterAnnotation {
	String key() default "rate:limiter";
	long limit() default 10;
	long expire() default 1;
	String message() default "false";
}
