package org.taranix.cafe.beans.descriptors.data;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.CafeProvider;

public class StringProvider {

    @CafeProvider
    String getString() {
        throw new NotImplementedException();
    }
}
