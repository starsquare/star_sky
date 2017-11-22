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
 * @company 杭州海康威视数字技术股份有限公司
 * @describe 异常捕获代理类
 */

public final class ExceptionProxy implements IProxy
{
    /**
     * 完整的类名
     */
    private TypeElement mTypeElement;


    /**
     * 元素相关的辅助类
     */
    private Elements mElementUtils;

    /**
     * 编译信息注解类
     */
    private Messager messager;

    /**
     * 代理类名
     */
    private String proxyClassName;


    /**
     * 绑定view的类
     */
    private ClassName bindViewClassName;

    /**
     * 代理类中内部类名称
     */
    private String innerClass;

    /**
     * 注解属性(注解中类名和路径名)
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
     * 获取代理类的名字
     */
    private void getProxyName()
    {
        String packageName = getPackageName();
        bindViewClassName = ClassName.get(packageName, mTypeElement.getSimpleName() + "$$ViewBinder");
        messager.printMessage(Diagnostic.Kind.NOTE, "测试\n" + bindViewClassName.simpleName());
        proxyClassName = mTypeElement.getSimpleName() + "$$ViewBinder";
    }

    /**
     * 获取包名
     *
     * @return
     */
    private String getPackageName()
    {
        return mElementUtils.getPackageOf(mTypeElement).getQualifiedName().toString();
    }


    //**************************************javapoet***************************************************/

    /**
     * 生成javaCode代码
     */
    public JavaFile generateJavaCode() throws Exception
    {
        messager.printMessage(Diagnostic.Kind.NOTE, "开始生成javaCode");
        //创建代理类
        TypeSpec.Builder proxyClass = createProxyClass();

        //创建代理类构造
        MethodSpec constructionMethod = createProxyClassConstruction();
        proxyClass.addMethod(constructionMethod);

        messager.printMessage(Diagnostic.Kind.NOTE, "创建代理类构造");

        //实现viewBinder方法
        MethodSpec binderMethod = realizeViewBinderMethod();
        proxyClass.addMethod(binderMethod);

        messager.printMessage(Diagnostic.Kind.NOTE, "实现viewBinder方法");
        //代理类中内部类
        TypeSpec innerClassTypeSpec = createProxyInnerClass();
        proxyClass.addType(innerClassTypeSpec);

        messager.printMessage(Diagnostic.Kind.NOTE, "代理类中内部类");

        TypeSpec build = proxyClass.build();

        return JavaFile.builder(mElementUtils.getPackageOf(mTypeElement).getQualifiedName().toString(), build).build();
    }

    /**
     * 创建代理类中的内部类
     */
    private TypeSpec createProxyInnerClass()
    {
        TypeSpec.Builder innerClassBuilder = TypeSpec.classBuilder(innerClass)
                .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
                .addTypeVariable(TypeVariableName.get("T", TypeName.get(mTypeElement.asType())))
                .addSuperinterface(UNBINDER);

        //构造内部类方法
        MethodSpec constructorMethod = createInnerConstructor();

        //创建unbind方法
        MethodSpec unbindMethod = createInnerClassUnbind();

        innerClassBuilder.addMethod(constructorMethod);
        innerClassBuilder.addMethod(unbindMethod);

        //添加变量
        FieldSpec targetFiled = createFiled();
        innerClassBuilder.addField(targetFiled);

        return innerClassBuilder.build();
    }

    /**
     * 创建成员变量
     */
    private FieldSpec createFiled()
    {
        TypeVariableName T = TypeVariableName.get("T");
        FieldSpec targetFiled = FieldSpec.builder(T, "target", Modifier.PROTECTED)
                .build();
        return targetFiled;
    }

    /**
     * 创建内部类中的构造方法
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
     * 创建代理类中内部类Unbinder方法
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
     * 实现ViewBinder方法
     */
    private MethodSpec realizeViewBinderMethod()
    {
        TypeVariableName variableName = TypeVariableName.get("T");

        //内部类
        String packageName = mElementUtils.getPackageOf(mTypeElement).getQualifiedName().toString();
        //获取内部类名称
        innerClass = "InnerUnbinder";
        ClassName innerClassName = ClassName.get(packageName, proxyClassName + "." + innerClass);

        //代码区
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
     * 创建代理类构造
     */
    private MethodSpec createProxyClassConstruction()
    {
        MethodSpec constructorMethod = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();
        return constructorMethod;
    }


    /**
     * 创建ViewBinder类
     *
     * @return 创建代理类
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
