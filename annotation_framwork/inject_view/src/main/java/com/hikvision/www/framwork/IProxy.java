package com.hikvision.www.framwork;

import com.hikvision.www.domain.AnnotationViewBind;
import com.hikvision.www.domain.FileViewBind;
import com.squareup.javapoet.JavaFile;

/**
 * @author huangxing7
 * @date on 2017/11/22
 * @company 杭州海康威视数字技术股份有限公司
 * @describe 编译代理类接口
 */

public interface IProxy
{
    JavaFile generateJavaCode() throws Exception;

    void setAnnotationViewBind(AnnotationViewBind annotationViewBind);

    void setFileViewBind(FileViewBind fileViewBind);
}
