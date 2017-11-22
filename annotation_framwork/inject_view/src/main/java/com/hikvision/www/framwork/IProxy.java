package com.hikvision.www.framwork;

import com.hikvision.www.domain.AnnotationViewBind;
import com.hikvision.www.domain.FileViewBind;
import com.squareup.javapoet.JavaFile;

/**
 * @author huangxing7
 * @date on 2017/11/22
 * @company ���ݺ����������ּ����ɷ����޹�˾
 * @describe ���������ӿ�
 */

public interface IProxy
{
    JavaFile generateJavaCode() throws Exception;

    void setAnnotationViewBind(AnnotationViewBind annotationViewBind);

    void setFileViewBind(FileViewBind fileViewBind);
}
