package org.taranix.cafe.beans.resolvers.data.prototype;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.resolvers.data.AbstractServiceExtension;

@CafeSingleton
@Getter
public class AbstractServiceExtensionUse {

    @CafeInject
    private AbstractServiceExtension abstractServiceExtension;
}
