package academy.reyngardt.annotation.processor.visitor;

import academy.reyngardt.annotation.processor.generator.UUIDGeneratorClass;
import com.squareup.javapoet.*;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner9;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;

/**
 * @author Mikhail Reyngardt 23.04.2022
 */
public class UUIDGeneratorVisitor extends ElementScanner9<Void, Void> {

    private final CodeBlock.Builder builder = CodeBlock.builder();
    private final Trees trees;
    private final Messager messager;
    private final Filer filer;
    private final TypeElement originElement;
    private final TreeMaker treeMaker;
    private final Names names;
    private final ClassTypeManager classTypeManager = new ClassTypeManager();

    public UUIDGeneratorVisitor(ProcessingEnvironment environment, TypeElement typeElement) {
        super();
        final JavacProcessingEnvironment javacProcessingEnvironment = ((JavacProcessingEnvironment) jbUnwrap(environment));
        trees = Trees.instance(javacProcessingEnvironment);
        messager = environment.getMessager();
        filer = javacProcessingEnvironment.getFiler();
        originElement = typeElement;
        treeMaker = TreeMaker.instance(javacProcessingEnvironment.getContext());
        names = Names.instance(javacProcessingEnvironment.getContext());
        classTypeManager.setEnclosingType(typeElement.asType());
    }

    @Override
    public Void visitVariable(VariableElement field, Void aVoid) {
        ((JCTree) trees.getTree(field)).accept(new TreeTranslator() {
            @Override
            public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
                super.visitVarDef(jcVariableDecl);
                jcVariableDecl.mods.flags &= ~Flags.PRIVATE;
            }
        });
        classTypeManager.setFiledType(field.asType());
        builder.addStatement("$T generator = new $T()", classTypeManager.geUUIDGeneratorClassName(), classTypeManager.geUUIDGeneratorClassName())
                .addStatement("$T generatedId = generator.getId()", String.class)
                .beginControlFlow("if (generatedId != null)")
                .addStatement("(($T) this).$L = generatedId", classTypeManager.getEnclosingElementClassName(),
                        field.getSimpleName())
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("throw new NullPointerException()")
                .endControlFlow();

        return super.visitVariable(field, aVoid);
    }

    public void brewJava() {
        final TypeSpec typeSpec = TypeSpec.classBuilder(originElement.getSimpleName() + "$$Proxy")
                .addModifiers(Modifier.ABSTRACT)
                .addOriginatingElement(originElement)
                .addInitializerBlock(builder.build())
                .build();
        final JavaFile javaFile = JavaFile.builder(originElement.getEnclosingElement().toString(), typeSpec)
                .addFileComment("Generated by UUIDGenerator processor, do not modify")
                .build();
        try {
            final JavaFileObject sourceFile = filer.createSourceFile(
                    javaFile.packageName + "." + typeSpec.name, originElement);
            try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                javaFile.writeTo(writer);
            }
            JCTree.JCExpression selector = treeMaker.Ident(names.fromString(javaFile.packageName));
            selector = treeMaker.Select(selector, names.fromString(typeSpec.name));
            ((JCTree.JCClassDecl) trees.getTree(originElement)).extending = selector;
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), originElement);
        }
    }

    private <T> T jbUnwrap(T wrapper) {
        T unwrapped = null;
        try {
            final Class<?> apiWrappers = wrapper.getClass().getClassLoader().loadClass("org.jetbrains.jps.javac.APIWrappers");
            final Method unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = ((Class<? extends T>) ProcessingEnvironment.class).cast(unwrapMethod.invoke(null, ProcessingEnvironment.class, wrapper));
        } catch (Throwable ignored) {
        }
        return unwrapped != null ? unwrapped : wrapper;
    }
}
