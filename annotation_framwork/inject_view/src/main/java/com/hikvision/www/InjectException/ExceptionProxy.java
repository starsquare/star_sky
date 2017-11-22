package com.hikvision.www.InjectException;

import com.hikvision.www.domain.AnnotationViewBind;
import com.hikvision.www.domain.FileViewBind;
import com.hikvision.www.framwork.IProxy;
import com.hikvision.www.framwork.ViewBinder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static com.hikvision.www.processor.MessageDescribeConstant.UNBINDER;

/**
 * @author huangxing7
 * @date on 2017/11/20
 * @company ���ݺ����������ּ����ɷ����޹�˾
 * @describe �쳣���������
 */

public final class ExceptionProxy implements IProxy
{
    /**
     * ����������
     */
    private TypeElement mTypeElement;


    /**
     * Ԫ����صĸ�����
     */
    private Elements mElementUtils;

    /**
     * ������Ϣע����
     */
    private Messager messager;

    /**
     * ��������
     */
    private String proxyClassName;


    /**
     * ��view����
     */
    private ClassName bindViewClassName;

    /**
     * ���������ڲ�������
     */
    private String innerClass;

    /**
     * ע������(ע����������·����)
     */
    private AnnotationViewBind annotationViewBind;

    private List<FileViewBind> fileViewBindList = new ArrayList<>();

    public ExceptionProxy(Elements mElementUtils, TypeElement mTypeElement, Messager messager)
    {
        this.mElementUtils = mElementUtils;
        this.mTypeElement = mTypeElement;
        this.messager = messager;

        getProxyName();
    }

    public AnnotationViewBind getAnnotationViewBind()
    {
        return annotationViewBind;
    }

    public void setAnnotationViewBind(AnnotationViewBind annotationViewBind)
    {
        this.annotationViewBind = annotationViewBind;
    }

    public void setFileViewBind(FileViewBind fileViewBind)
    {
        fileViewBindList.add(fileViewBind);
    }

    /**
     * ��ȡ�����������
     */
    private void getProxyName()
    {
        String packageName = getPackageName();
        bindViewClassName = ClassName.get(packageName, mTypeElement.getSimpleName() + "$$ViewBinder");
        messager.printMessage(Diagnostic.Kind.NOTE, "����\n" + bindViewClassName.simpleName());
        proxyClassName = mTypeElement.getSimpleName() + "$$ViewBinder";
    }

    /**
     * ��ȡ����
     *
     * @return
     */
    private String getPackageName()
    {
        return mElementUtils.getPackageOf(mTypeElement).getQualifiedName().toString();
    }


    //**************************************javapoet***************************************************/

    /**
     * ����javaCode����
     */
    public JavaFile generateJavaCode() throws Exception
    {
        messager.printMessage(Diagnostic.Kind.NOTE, "��ʼ����javaCode");
        //����������
        TypeSpec.Builder proxyClass = createProxyClass();

        //���������๹��
        MethodSpec constructionMethod = createProxyClassConstruction();
        proxyClass.addMethod(constructionMethod);

        messager.printMessage(Diagnostic.Kind.NOTE, "���������๹��");

        //ʵ��viewBinder����
        MethodSpec binderMethod = realizeViewBinderMethod();
        proxyClass.addMethod(binderMethod);

        messager.printMessage(Diagnostic.Kind.NOTE, "ʵ��viewBinder����");
        //���������ڲ���
        TypeSpec innerClassTypeSpec = createProxyInnerClass();
        proxyClass.addType(innerClassTypeSpec);

        messager.printMessage(Diagnostic.Kind.NOTE, "���������ڲ���");

        TypeSpec build = proxyClass.build();

        return JavaFile.builder(mElementUtils.getPackageOf(mTypeElement).getQualifiedName().toString(), build).build();
    }

    /**
     * �����������е��ڲ���
     */
    private TypeSpec createProxyInnerClass()
    {
        TypeSpec.Builder innerClassBuilder = TypeSpec.classBuilder(innerClass)
                .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
                .addTypeVariable(TypeVariableName.get("T", TypeName.get(mTypeElement.asType())))
                .addSuperinterface(UNBINDER);

        //�����ڲ��෽��
        MethodSpec constructorMethod = createInnerConstructor();

        //����unbind����
        MethodSpec unbindMethod = createInnerClassUnbind();

        innerClassBuilder.addMethod(constructorMethod);
        innerClassBuilder.addMethod(unbindMethod);

        //��ӱ���
        FieldSpec targetFiled = createFiled();
        innerClassBuilder.addField(targetFiled);

        return innerClassBuilder.build();
    }

    /**
     * ������Ա����
     */
    private FieldSpec createFiled()
    {
        TypeVariableName T = TypeVariableName.get("T");
        FieldSpec targetFiled = FieldSpec.builder(T, "target", Modifier.PROTECTED)
                .build();
        return targetFiled;
    }

    /**
     * �����ڲ����еĹ��췽��
     */
    private MethodSpec createInnerConstructor()
    {
        TypeVariableName T = TypeVariableName.get("T");

        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.add("try{");
        codeBlock.add("this.target = target;");
        for (FileViewBind fileViewBind : fileViewBindList)
        {
            CodeBlock filedCodeBlock = CodeBlock.builder()
                    .add("target.$L=new $T();\n", fileViewBind.getName(), fileViewBind.getTypeName())
                    .build();
            codeBlock.add(filedCodeBlock);
        }
        codeBlock.add("}catch($T e){", Exception.class)
                .add(" e.printStackTrace();")
                .add("}");


        MethodSpec constructorMethod = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(T, "target")
                .addParameter(TypeName.OBJECT, "source")
                .addCode(codeBlock.build())
                .build();
        return constructorMethod;
    }

    /**
     * �������������ڲ���Unbinder����
     */
    private MethodSpec createInnerClassUnbind()
    {
        CodeBlock.Builder codeBlock=CodeBlock.builder()
                .add("$T target=this.target;\n",TypeName.get(mTypeElement.asType()))
                .add("if(target==null){\n")
                .add("throw new $T($S);\n",IllegalStateException.class,"Bindings already cleared.")
                .add("}else{\n");

        for(FileViewBind fileViewBind : fileViewBindList)
        {
            CodeBlock filedCodeBlock = CodeBlock.builder()
                    .add("target.$L=null;\n", fileViewBind.getName())
                    .build();
            codeBlock.add(filedCodeBlock);
        }
        codeBlock.add("this.target=null;\n")
                .add("}\n");

        MethodSpec unbindMethod = MethodSpec.methodBuilder("unbind")
                .addModifiers(Modifier.PUBLIC)
                .addCode(codeBlock.build())
                .build();
        return unbindMethod;
    }

    /**
     * ʵ��ViewBinder����
     */
    private MethodSpec realizeViewBinderMethod()
    {
        TypeVariableName variableName = TypeVariableName.get("T");

        //�ڲ���
        String packageName = mElementUtils.getPackageOf(mTypeElement).getQualifiedName().toString();
        //��ȡ�ڲ�������
        innerClass = "InnerUnbinder";
        ClassName innerClassName = ClassName.get(packageName, proxyClassName + "." + innerClass);

        //������
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.add("return new $T(target,source);\n", innerClassName);

        MethodSpec bindMethod = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(UNBINDER)
                .addParameter(variableName, "target")
                .addParameter(TypeName.OBJECT, "source")
                .addCode(builder.build())
                .build();
        return bindMethod;
    }


    /**
     * ���������๹��
     */
    private MethodSpec createProxyClassConstruction()
    {
        MethodSpec constructorMethod = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();
        return constructorMethod;
    }


    /**
     * ����ViewBinder��
     *
     * @return ����������
     */
    private TypeSpec.Builder createProxyClass()
    {
        TypeVariableName variableName = TypeVariableName.get("T");
        ClassName viewBinderClassName = ClassName.get(ViewBinder.class);

        TypeSpec.Builder proxyClassBuilder = TypeSpec.classBuilder(proxyClassName)
                .addTypeVariable(TypeVariableName.get("T", TypeName.get(mTypeElement.asType())))
                .addSuperinterface(ParameterizedTypeName.get(viewBinderClassName, variableName))
                .addModifiers(Modifier.PUBLIC);
        return proxyClassBuilder;
    }
}
