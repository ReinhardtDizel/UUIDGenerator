package academy.reyngardt.annotation.processor.visitor;

import academy.reyngardt.annotation.processor.generator.UUIDGeneratorClass;
import com.squareup.javapoet.ClassName;

import javax.lang.model.type.TypeMirror;
import java.util.*;

/**
 * @author Mikhail Reyngardt 23.04.2022
 */
public class ClassTypeManager {

    private final Map<UsedType, ClassName> classNameUtilMap;

    public ClassTypeManager() {
        classNameUtilMap = new HashMap<>();
        classNameUtilMap.put(UsedType.UUID_GENERATOR, ClassName.get(UUIDGeneratorClass.class));
    }

    public ClassName getFiledClassName() {
        return classNameUtilMap.get(UsedType.ENTITY);
    }

    public ClassName geUUIDGeneratorClassName() {
        return classNameUtilMap.get(UsedType.UUID_GENERATOR);
    }

    public ClassName  getEnclosingElementClassName() {
        return classNameUtilMap.get(UsedType.ENCLOSING_ELEMENT);
    }

    public void setFiledType(TypeMirror typeMirror) {
        ClassName name = ClassTypeManagerUtils.getAnnotatedFiledClassName(typeMirror.toString());
        Objects.requireNonNull(name, "in ClassTypeManager setFiledType() nameList is NULL");
        ClassTypeManager.this.classNameUtilMap.put(UsedType.ENTITY, name);
    }

    public void setEnclosingType(TypeMirror typeMirror) {
        List<String> classNames = ClassTypeManagerUtils.getSplitsClassName(typeMirror.toString());
        Objects.requireNonNull(classNames, "in ClassTypeManager setEnclosingType() classNames is NULL");
        ClassName name = ClassName.get(classNames.get(0), classNames.get(1));
        ClassTypeManager.this.classNameUtilMap.put(UsedType.ENCLOSING_ELEMENT, name);
    }

    private enum UsedType {
        ENTITY,
        ENCLOSING_ELEMENT,
        UUID_GENERATOR
    }

    private static class ClassTypeManagerUtils {

        private static ClassName getAnnotatedFiledClassName(String string) {
            List<String> stringType = getSplitsClassName(string);
            return ClassName.get(stringType.get(0), stringType.get(1));
        }

        private static List<String> getSplitsClassName(String name) {
            String[] value = name.split("\\.");
            StringBuilder packageName = new StringBuilder();
            StringBuilder simpleName = new StringBuilder();
            List<String> res = new ArrayList<>();
            for (int i = 0; i < value.length - 1; i++) {
                packageName.append(value[i]);
                if (i < value.length - 2) {
                    packageName.append(".");
                }
            }
            simpleName.append(value[value.length - 1]);
            res.add(packageName.toString());
            res.add(simpleName.toString());
            return res;
        }
    }
}
