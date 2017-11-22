package com.hikvision.www.framwork;

/**
 * @author huangxing7
 * @date on 2017/11/20
 * @company 杭州海康威视数字技术股份有限公司
 * @describe 调用bind后返回值
 */
public interface ViewBinder<T>
{
    Unbinder bind(T target, Object soure);
}
