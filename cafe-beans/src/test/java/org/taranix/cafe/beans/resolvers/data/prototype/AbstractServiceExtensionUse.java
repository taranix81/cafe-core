package org.taranix.cafe.beans.resolvers.data.prototype;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.resolvers.data.AbstractServiceExtension;

@CafeService
@Getter
public class AbstractServiceExtensionUse {

    @CafeInject
    private AbstractServiceExtension abstractServiceExtension;
}
