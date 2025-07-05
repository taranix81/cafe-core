package org.taranix.cafe.beans.resolvers.provider;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.descriptors.CafeMemberInfo;

public interface CafeProviderResolver {

    Object resolve(CafeMemberInfo memberInfo, CafeBeansFactory cafeBeansFactory);

    boolean isApplicable(CafeMemberInfo descriptor);
}
