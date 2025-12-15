package org.taranix.cafe.beans.resolvers.metadata;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;

public interface CafeProviderResolver {

    Object resolve(CafeMemberInfo memberInfo, CafeBeansFactory cafeBeansFactory);

    boolean isApplicable(CafeMemberInfo descriptor);
}
