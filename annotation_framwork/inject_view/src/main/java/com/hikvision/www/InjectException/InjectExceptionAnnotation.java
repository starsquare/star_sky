package com.hikvision.www.InjectException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author huangxing7
 * @date on 2017/11/20
 * @company 杭州海康威视数字技术股份有限公司
 * @describe 异常捕获注解类
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface InjectExceptionAnnotation
{
    /**
     * 注解类,编译时框架会通过反射获取实例
     *
     * @return
     */
    Class annotationClass();

    /**
     * 异常捕获存放位置
     *
     * @return
     */
    String catchPath();
}
