package com.hikvision.www.framwork;

/**
 * @author huangxing7
 * @date on 2017/11/20
 * @company ���ݺ����������ּ����ɷ����޹�˾
 * @describe ����bind�󷵻�ֵ
 */
public interface ViewBinder<T>
{
    Unbinder bind(T target, Object soure);
}
