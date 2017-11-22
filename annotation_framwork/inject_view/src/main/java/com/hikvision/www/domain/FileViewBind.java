package com.hikvision.www.domain;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Name;

/**
 * @author huangxing7
 * @date on 2017/11/22
 * @company 杭州海康威视数字技术股份有限公司
 * @describe 字段绑定数据
 */

public class FileViewBind
{
    /**
     * 字段名
     */
    private final Name name;

    /**
     * 字段类型
     */
    private final TypeName typeName;

    /**
     * 是否可以直接修改的(publice,private)
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
