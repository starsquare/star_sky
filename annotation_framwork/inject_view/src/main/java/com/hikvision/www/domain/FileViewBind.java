package com.hikvision.www.domain;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Name;

/**
 * @author huangxing7
 * @date on 2017/11/22
 * @company ���ݺ����������ּ����ɷ����޹�˾
 * @describe �ֶΰ�����
 */

public class FileViewBind
{
    /**
     * �ֶ���
     */
    private final Name name;

    /**
     * �ֶ�����
     */
    private final TypeName typeName;

    /**
     * �Ƿ����ֱ���޸ĵ�(publice,private)
     */
    private final boolean required;

    public FileViewBind(Name name, TypeName typeName, boolean required)
    {
        this.name = name;
        this.typeName = typeName;
        this.required = required;
    }

    public Name getName()
    {
        return name;
    }

    public TypeName getTypeName()
    {
        return typeName;
    }

    public boolean isRequired()
    {
        return required;
    }
}
