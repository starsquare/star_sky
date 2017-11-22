package com.hikvision.www.processor;

import com.google.auto.service.AutoService;
import com.hikvision.www.InjectException.ExceptionProxy;
import com.hikvision.www.InjectException.InjectExceptionAnnotation;
import com.hikvision.www.domain.AnnotationViewBind;
import com.hikvision.www.domain.FileViewBind;
import com.hikvision.www.framwork.IProxy;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * @author huangxing7
 * @date on 2017/11/20
 * @company 杭州海康威视数字技术股份有限公司
 * @describe 编译处理器
 */

@AutoService(Processor.class)
public class CompileProcessor extends AbstractProcessor
{
    /**
     * 文件相关辅助类
     */
    private Filer filer;

    /**
     * 日志相关辅助类
     */
    private Messager messager;

    /**
     * 元素相关辅助类
     */
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment)
    {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env)
    {
        messager.printMessage(Diagnostic.Kind.NOTE, MessageDescribeConstant.PROCESSOR_START);
        try
        {
            getElementForAnnotation(env);
        } catch (Exception e)
        {
            error("编译错误" + e.getMessage());
            return true;
        }
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latestSupported();
    }

    /**
     * 获取支持注解的类型
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        Set<String> annotations = new HashSet<>();
        annotations.add(InjectExceptionAnnotation.class.getCanonicalName());
        return annotations;
    }

    //*********************************本地方法***********************************************/

    /**
     * 通过注解获取元素
     *
     * @param env
     * @throws Exception
     */
    private void getElementForAnnotation(RoundEnvironment env) throws Exception
    {
        //目标类集合
        Map<TypeElement, IProxy> targetClassMap = new HashMap();

        //查找InjectExceptionAnnotation注解信息
        findAndParseTargetException(env, targetClassMap);

        for (Map.Entry<TypeElement, IProxy> entry : targetClassMap.entrySet())
        {
            TypeElement element = entry.getKey();
            IProxy proxyClass = entry.getValue();
            try
            {
                JavaFile javaFile = proxyClass.generateJavaCode();
                javaFile.writeTo(filer);

            } catch (Exception e)
            {
                error(e.getMessage());
            }

        }
    }

    /**
     * 查找或者解析InjectExceptionAnnotation注解
     *
     * @param env
     * @param targetClassMap
     */
    private void findAndParseTargetException(RoundEnvironment env, Map<TypeElement, IProxy> targetClassMap)
    {
        Set<? extends Element> elementsAnnotatedWith = env
                .getElementsAnnotatedWith(InjectExceptionAnnotation.class);

        for (Element element : elementsAnnotatedWith)
        {
            //获取注解元素
            VariableElement variableElement = (VariableElement) element;
            //获取注解元素类
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            //获取注解元素类名
            String fqName = typeElement.getQualifiedName().toString();
            //获取注解元素字段名
            Name filedName = variableElement.getSimpleName();
            //获取注解元素修饰符(public,private,final等)
            Set<Modifier> modifiers = variableElement.getModifiers();
            //获取注解字段类型
            TypeMirror filedType = variableElement.asType();

            error("字段和类型:" + filedName + "\n" + filedType);

            boolean required = true;
            if (modifiers.contains(Modifier.PRIVATE))
            {
                required = false;
            }

            //注解字段信息
            FileViewBind fileViewBind = new FileViewBind(filedName, TypeName.get(filedType), required);

            //获取注解内容
            InjectExceptionAnnotation annotation = variableElement.getAnnotation(InjectExceptionAnnotation.class);

            //获取注解的class类
            String annotationClassName = "";

            //获取注解中路径
            String annotationValue = annotation.catchPath();

            //获取注解元素类型
            TypeElement annonationTypeElement = elementUtils.getTypeElement(InjectExceptionAnnotation.class.getName());

            for (AnnotationMirror annotationMirror : variableElement.getAnnotationMirrors())
            {
                if (annotationMirror.getAnnotationType().equals(annonationTypeElement.asType()))
                {
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet())
                    {
                        if ("annotation".equals(entry.getKey().getSimpleName().toString()))
                        {
                            AnnotationValue value = entry.getValue();
                            annotationClassName=value.getValue().toString();
                        }
                    }
                }
            }

            //注解信息
            AnnotationViewBind annotationViewBind = new AnnotationViewBind(annotationClassName, annotationValue);

            IProxy proxy = targetClassMap.get(typeElement);
            if (proxy == null)
            {
                proxy = new ExceptionProxy(elementUtils, typeElement, messager);
                proxy.setAnnotationViewBind(annotationViewBind);
                proxy.setFileViewBind(fileViewBind);
                targetClassMap.put(typeElement, proxy);
            } else
            {
                proxy.setFileViewBind(fileViewBind);
            }
        }
    }


    /**
     * 打印错误信息
     *
     * @param msg
     */

    private void error(String msg)
    {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
