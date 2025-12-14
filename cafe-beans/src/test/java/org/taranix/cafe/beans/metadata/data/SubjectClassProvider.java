package org.taranix.cafe.beans.metadata.data;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.CafeProvider;

public class SubjectClassProvider {
    @CafeProvider
    private SubjectClass producer() {
        throw new NotImplementedException();
    }

}
