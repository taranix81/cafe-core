package org.taranix.cafe.beans.resolvers.metadata;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.CafeMemberMetadata;

public interface CafeProviderResolver {

    Object resolve(CafeMemberMetadata memberInfo, CafeBeansFactory cafeBeansFactory);

    boolean isApplicable(CafeMemberMetadata descriptor);
}
