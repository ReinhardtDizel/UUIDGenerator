package academy.reyngardt.annotation.processor;

import academy.reyngardt.annotation.processor.visitor.UUIDGeneratorVisitor;
import com.google.auto.service.AutoService;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.annotation.processing.Processor;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Mikhail Reyngardt 30.04.2022
 */
@SupportedAnnotationTypes(
        "academy.reyngardt.annotation.processor.UUIDGenerator")
@SupportedSourceVersion(SourceVersion.RELEASE_9)
@AutoService(Processor.class)
public class UUIDGeneratorProcessor extends AbstractProcessor {

    private final Map<TypeElement, UUIDGeneratorVisitor> elementUUIDGeneratorVisitorMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(UUIDGenerator.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (annotations.isEmpty()) {
            return false;
        }
        final Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(UUIDGenerator.class);
        for (final Element element : elements) {
            final TypeElement object = (TypeElement) element.getEnclosingElement();
            UUIDGeneratorVisitor visitor = elementUUIDGeneratorVisitorMap.get(object);
            if (visitor == null) {
                visitor = new UUIDGeneratorVisitor(processingEnv, object);
                elementUUIDGeneratorVisitorMap.put(object, visitor);
            }
            element.accept(visitor, null);
        }

        for (final UUIDGeneratorVisitor visitor : elementUUIDGeneratorVisitorMap.values()) {
            visitor.brewJava();
        }

        return true;
    }
}
