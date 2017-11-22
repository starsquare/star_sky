package com.hikvision.www.framwork;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author huangxing7
 * @date on 2017/11/20
 * @company 杭州海康威视数字技术股份有限公司
 * @describe 注解框架
 */

public class InjectView
{
    /**
     * 存储ViewBinder的集合,使用享元模式设计
     */
    static final Map<Class<?>, ViewBinder<Object>> BINDERS = new LinkedHashMap<>();

    /**
     * 注入目标类
     *
     * @param target
     * @return
     */
    public static Unbinder inject(Object target)
    {
        Class<?> targetClass = target.getClass();
        ViewBinder<Object> viewBinder = findViewBinderForClassName(targetClass);
        return viewBinder.bind(target, target);
    }

    /**
     * 查找ViewBinder对象,没有就创建
     *
     * @param cls
     * @return
     */
    private static ViewBinder<Object> findViewBinderForClassName(Class<?> cls)
    {
        ViewBinder<Object> viewBinder = BINDERS.get(cls);
        if (viewBinder != null)
        {
            return viewBinder;
        }
        String name = cls.getName();
        String proxyClassName = name + "$$ViewBinder";
        try
        {
            viewBinder = (ViewBinder) Class.forName(proxyClassName).newInstance();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        } catch (InstantiationException e)
        {
            throw new RuntimeException("Unable to create view binder for " + proxyClassName, e);
        } catch (IllegalAccessException e)
        {
            throw new RuntimeException("Unable to create view binder for " + proxyClassName, e);
        }
        BINDERS.put(cls, viewBinder);
        return viewBinder;
    }
}
