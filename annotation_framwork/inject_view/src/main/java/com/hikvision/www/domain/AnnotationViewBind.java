package com.hikvision.www.domain;

/**
 * @author huangxing7
 * @date on 2017/11/22
 * @company ���ݺ����������ּ����ɷ����޹�˾
 * @describe ע������
 */

public class AnnotationViewBind
{
    /**
     * ע������
     */
    private String annonationClassName;

    /**
     * ע������
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
