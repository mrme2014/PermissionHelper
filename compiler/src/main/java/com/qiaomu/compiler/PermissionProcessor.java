package com.qiaomu.compiler;

import com.google.auto.service.AutoService;
import com.qiaomu.annotation.PermissionDenied;
import com.qiaomu.annotation.PermissionGrant;
import com.qiaomu.annotation.ShowRequestPermissionRationale;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import static javax.lang.model.SourceVersion.latestSupported;

/**
 * Created by qiaomu on 2017/10/9.
 */
@AutoService(Processor.class)
public class PermissionProcessor extends AbstractProcessor {
    private Elements mElementUtils;
    private Messager mMessager;
    private Map<String, ProxyInfo> mProxyMap = new HashMap<String, ProxyInfo>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(PermissionDenied.class.getCanonicalName());
        supportTypes.add(PermissionGrant.class.getCanonicalName());
        supportTypes.add(ShowRequestPermissionRationale.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mProxyMap.clear();
        mMessager.printMessage(Diagnostic.Kind.NOTE, "process...");

        if (!processAnnotations(roundEnv, PermissionGrant.class)) return false;
        if (!processAnnotations(roundEnv, PermissionDenied.class)) return false;
        if (!processAnnotations(roundEnv, ShowRequestPermissionRationale.class)) return false;

        for (String name:mProxyMap.keySet()) {
            ProxyInfo proxyInfo = mProxyMap.get(name);
            try
            {
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(proxyInfo.getProxyClassFullName(),
                        proxyInfo.getTypeElement());
                Writer writer = jfo.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e)
            {
                error(proxyInfo.getTypeElement(), "Unable to write injector for type %s: %s",
                        proxyInfo.getTypeElement(), e.getMessage());
            }
        }

        return false;
    }

    private boolean processAnnotations(RoundEnvironment roundEnv, Class<? extends Annotation> annotationClaz) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationClaz);
        for (Element element : elements) {
            if (!checkMethodValidate(element, annotationClaz)) return false;
            ExecutableElement method = (ExecutableElement) element;
            TypeElement enclosingElement = (TypeElement) method.getEnclosingElement();
            String qualifiedName = enclosingElement.getQualifiedName().toString();

            ProxyInfo proxyInfo = mProxyMap.get(qualifiedName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(mElementUtils, enclosingElement);
                mProxyMap.put(qualifiedName, proxyInfo);
                proxyInfo.setTypeElement(enclosingElement);
            }

            Annotation annotation = method.getAnnotation(annotationClaz);
            if (annotation instanceof PermissionGrant) {
                int requestCode = ((PermissionGrant) annotation).value();
                proxyInfo.grantMethodMap.put(requestCode, method.getSimpleName().toString());
            } else if (annotation instanceof PermissionDenied) {
                int requestCode = ((PermissionDenied) annotation).value();
                proxyInfo.deniedMethodMap.put(requestCode, method.getSimpleName().toString());
            } else if (annotation instanceof ShowRequestPermissionRationale) {
                int requestCode = ((ShowRequestPermissionRationale) annotation).value();
                proxyInfo.rationaleMethodMap.put(requestCode, method.getSimpleName().toString());
            } else {
                error(element, "%s not support .", annotationClaz.getSimpleName());
                return false;
            }
        }
        return true;
    }

    private boolean checkMethodValidate(Element element, Class claz) {
        if (element.getKind() != ElementKind.METHOD) {
            error(element, "%s must be declared on method.", claz.getSimpleName());
            return false;
        }
        if (ClassValidator.isPrivate(element) || ClassValidator.isAbstract(element)) {
            error(element, "%s() must can not be abstract or private.", element.getSimpleName());
            return false;
        }

        return true;
    }

    private void error(Element element, String message, Object... objs) {
        if (objs.length > 0) {
            message = String.format(message, objs);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}
