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
 * @company ���ݺ����������ּ����ɷ����޹�˾
 * @describe ���봦����
 */

@AutoService(Processor.class)
public class CompileProcessor extends AbstractProcessor
{
    /**
     * �ļ���ظ�����
     */
    private Filer filer;

    /**
     * ��־��ظ�����
     */
    private Messager messager;

    /**
     * Ԫ����ظ�����
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
            error("�������" + e.getMessage());
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
     * ��ȡ֧��ע�������
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

    //*********************************���ط���***********************************************/

    /**
     * ͨ��ע���ȡԪ��
     *
     * @param env
     * @throws Exception
     */
    private void getElementForAnnotation(RoundEnvironment env) throws Exception
    {
        //Ŀ���༯��
        Map<TypeElement, IProxy> targetClassMap = new HashMap();

        //����InjectExceptionAnnotationע����Ϣ
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
     * ���һ��߽���InjectExceptionAnnotationע��
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
            //��ȡע��Ԫ��
            VariableElement variableElement = (VariableElement) element;
            //��ȡע��Ԫ����
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            //��ȡע��Ԫ������
            String fqName = typeElement.getQualifiedName().toString();
            //��ȡע��Ԫ���ֶ���
            Name filedName = variableElement.getSimpleName();
            //��ȡע��Ԫ�����η�(public,private,final��)
            Set<Modifier> modifiers = variableElement.getModifiers();
            //��ȡע���ֶ�����
            TypeMirror filedType = variableElement.asType();

            error("�ֶκ�����:" + filedName + "\n" + filedType);

            boolean required = true;
            if (modifiers.contains(Modifier.PRIVATE))
            {
                required = false;
            }

            //ע���ֶ���Ϣ
            FileViewBind fileViewBind = new FileViewBind(filedName, TypeName.get(filedType), required);

            //��ȡע������
            InjectExceptionAnnotation annotation = variableElement.getAnnotation(InjectExceptionAnnotation.class);

            //��ȡע���class��
            String annotationClassName = "";

            //��ȡע����·��
            String annotationValue = annotation.catchPath();

            //��ȡע��Ԫ������
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

            //ע����Ϣ
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
     * ��ӡ������Ϣ
     *
     * @param msg
     */

    private void error(String msg)
    {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
