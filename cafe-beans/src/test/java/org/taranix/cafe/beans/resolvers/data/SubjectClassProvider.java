package org.taranix.cafe.beans.resolvers.data;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.CafeProvider;
import org.taranix.cafe.beans.metadata.data.SubjectClass;

public class SubjectClassProvider {
    @CafeProvider
    private SubjectClass producer() {
        throw new NotImplementedException();
    }

}
