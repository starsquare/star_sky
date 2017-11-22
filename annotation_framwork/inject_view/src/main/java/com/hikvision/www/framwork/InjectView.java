package com.hikvision.www.framwork;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author huangxing7
 * @date on 2017/11/20
 * @company ���ݺ����������ּ����ɷ����޹�˾
 * @describe ע����
 */

public class InjectView
{
    /**
     * �洢ViewBinder�ļ���,ʹ����Ԫģʽ���
     */
    static final Map<Class<?>, ViewBinder<Object>> BINDERS = new LinkedHashMap<>();

    /**
     * ע��Ŀ����
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
     * ����ViewBinder����,û�оʹ���
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
