package com.hikvision.www.InjectException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author huangxing7
 * @date on 2017/11/20
 * @company ���ݺ����������ּ����ɷ����޹�˾
 * @describe �쳣����ע����
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface InjectExceptionAnnotation
{
    /**
     * ע����,����ʱ��ܻ�ͨ�������ȡʵ��
     *
     * @return
     */
    Class annotationClass();

    /**
     * �쳣������λ��
     *
     * @return
     */
    String catchPath();
}
