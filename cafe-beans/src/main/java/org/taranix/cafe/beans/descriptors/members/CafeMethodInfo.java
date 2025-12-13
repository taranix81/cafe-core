package org.taranix.cafe.beans.descriptors.members;

import lombok.Getter;
import org.taranix.cafe.beans.CafeReflectionUtils;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.descriptors.CafeClassInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.PropertyTypeKey;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Descriptor for a class method. This primarily handles factory methods
 * (which provide a bean) and task/event handler methods (which have dependencies).
 */
@Getter
public class CafeMethodInfo extends CafeMemberInfo {

    private final Method method;

    /**
     * Constructs a descriptor for the given method.
     */
    CafeMethodInfo(final Method method, CafeClassInfo cafeClassInfo) {
        super(cafeClassInfo);
        this.method = method;
    }

    @Override
    public Member getMember() {
        return getMethod();
    }

    /**
     * Returns the set of BeanTypeKeys provided by this method.
     * For factory methods, this is the method's return type.
     */
    @Override
    public Set<BeanTypeKey> provides() {
        // A method can only provide one bean (its return value)
        return Set.of(getMethodReturnTypeKey());
    }

    /**
     * Returns the dependencies required to invoke this method. These include:
     * <ul>
     * <li>Dependency on the owner class instance (if the method is non-static).</li>
     * <li>Dependencies on beans matching all method parameters.</li>
     * </ul>
     */
    @Override
    public List<BeanTypeKey> dependencies() {
        List<BeanTypeKey> result = new ArrayList<>();

        // Dependency on owner class instance for non-static methods
        if (!isStatic()) {
            result.add(getOwnerClassTypeKey());
        }

        // Add all parameter types as bean dependencies
        result.addAll(CafeReflectionUtils.determineMethodParameterTypes(getMethod(), getCafeClassInfo().getTypeClass())
                .stream()
                .map(BeanTypeKey::from)
                .toList());
        return result;
    }

    /**
     * Methods typically do not have direct property dependencies defined
     * on the method itself.
     */
    @Override
    public List<PropertyTypeKey> propertyDependencies() {
        return List.of();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        // Uses pattern matching for instanceof (Java 16+)
        if (obj instanceof CafeMethodInfo cafeMethodInfo) {
            return getMember().equals(cafeMethodInfo.getMember());
        }
        return false;
    }

    /**
     * Retrieves the BeanTypeKey representing the return type and optional identifier of the method.
     */
    public BeanTypeKey getMethodReturnTypeKey() {
        String memberIdentifier = CafeAnnotationUtils.getMemberName(getMethod());
        // Resolves the actual generic return type if necessary
        Type methodReturnType = CafeReflectionUtils.determineMethodReturnType(getMethod(), getCafeClassInfo().getTypeClass());
        return BeanTypeKey.from(methodReturnType, memberIdentifier);
    }

}