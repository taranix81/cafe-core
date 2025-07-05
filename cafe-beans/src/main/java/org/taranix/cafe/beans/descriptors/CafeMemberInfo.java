package org.taranix.cafe.beans.descriptors;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.PropertyTypeKey;

import java.lang.reflect.*;
import java.util.List;
import java.util.Set;

@Getter
public abstract class CafeMemberInfo {

    private final CafeClassInfo cafeClassInfo;

    protected CafeMemberInfo(final CafeClassInfo cafeClassInfo) {
        this.cafeClassInfo = cafeClassInfo;
    }

    static CafeMemberInfo from(CafeClassInfo cafeClassInfo, Member member) {
        if (member instanceof Field field) {
            return new CafeFieldInfo(field, cafeClassInfo);
        }

        if (member instanceof Method method) {
            return new CafeMethodInfo(method, cafeClassInfo);
        }

        if (member instanceof Constructor<?> constructor) {
            return new CafeConstructorInfo(constructor, cafeClassInfo);
        }
        return null;
    }

    public BeanTypeKey getOwnerClassTypeKey() {
        return BeanTypeKey.from(getCafeClassInfo().getTypeClass());
    }

    public Class<?> declaringClass() {
        return getMember().getDeclaringClass();
    }

    public abstract Member getMember();

    public boolean hasSameDeclaringClass(CafeMemberInfo other) {
        return this.declaringClass().equals(other.declaringClass());
    }

    public boolean isStatic() {
        return Modifier.isStatic(getMember().getModifiers());
    }

    public boolean isConstructor() {
        return getMember() instanceof Constructor<?>;
    }

    public boolean isField() {
        return getMember() instanceof Field;
    }

    public boolean isMethod() {
        return getMember() instanceof Method;
    }

    public abstract Set<BeanTypeKey> provides();

    public boolean hasDependencies() {
        return !dependencies().isEmpty();
    }

    public abstract List<BeanTypeKey> dependencies();

    public abstract List<PropertyTypeKey> propertyDependencies();

    public boolean hasDependencies(BeanTypeKey typeKey) {
        return dependencies().contains(typeKey);
    }

    @Override
    public int hashCode() {
        return getMember().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof CafeMemberInfo cafeMemberInfo) {
            return getMember().equals(cafeMemberInfo.getMember());
        }
        return false;
    }

    @Override
    public String toString() {
        String memberType = getMember() instanceof Constructor<?> ? "Constructor" :
                getMember() instanceof Field ? "Field" : "Method";

        return "(" + memberType + ") " + cafeClassInfo.getTypeClass().getCanonicalName() + ":" + getMember().getName();
    }

    public boolean isPrototype() {
        return !isSingleton();
    }

    public boolean isSingleton() {
        return CafeAnnotationUtils.isSingleton(getMember());
    }

    public abstract boolean isPrimary();

    public abstract boolean isOptional();

}
