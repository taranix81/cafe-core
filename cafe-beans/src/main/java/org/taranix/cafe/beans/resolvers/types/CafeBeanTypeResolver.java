package org.taranix.cafe.beans.resolvers.types;

import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

public interface CafeBeanTypeResolver {

    Object resolve(BeanTypeKey typeKey, CafeBeansFactory beansFactory);

    Object resolveOrNull(BeanTypeKey typeKey, CafeBeansFactory beansFactory);

    boolean isApplicable(BeanTypeKey typeKey);


}
