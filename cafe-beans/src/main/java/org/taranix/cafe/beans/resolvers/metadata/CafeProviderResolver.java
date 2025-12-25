package org.taranix.cafe.beans.resolvers.metadata;

import org.taranix.cafe.beans.metadata.CafeMember;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

public interface CafeProviderResolver {

    Object resolve(CafeMember memberInfo, CafeBeansFactory cafeBeansFactory);

    boolean isApplicable(CafeMember descriptor);
}
