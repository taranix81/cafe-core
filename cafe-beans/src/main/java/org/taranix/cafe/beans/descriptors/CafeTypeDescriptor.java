package org.taranix.cafe.beans.descriptors;

import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.Set;

public interface CafeTypeDescriptor {

    BeanTypeKey typeKey();

    Set<BeanTypeKey> provides();

    Set<BeanTypeKey> dependencies();
}
