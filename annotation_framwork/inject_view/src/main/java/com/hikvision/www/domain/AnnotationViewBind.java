package com.hikvision.www.domain;

/**
 * @author huangxing7
 * @date on 2017/11/22
 * @company 杭州海康威视数字技术股份有限公司
 * @describe 注解属性
 */

public class AnnotationViewBind
{
    /**
     * 注解类名
     */
    private String annonationClassName;

    /**
     * 注解属性
     */
    private String annonationValue;

    public AnnotationViewBind(String annonationClassName, String annonationValue)
    {
        this.annonationClassName = annonationClassName;
        this.annonationValue = annonationValue;
    }

    public String getAnnonationClassName()
    {
        return annonationClassName;
    }

    public String getAnnonationValue()
    {
        return annonationValue;
    }
}
