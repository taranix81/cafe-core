package org.taranix.cafe.beans.descriptors.data;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.CafeProvider;

import java.util.Date;

public class SubjectClassProviderWCADate {

    SubjectClassProviderWCADate(Date input) {

    }

    @CafeProvider
    private SubjectClass producer(String input) {
        throw new NotImplementedException();
    }

}
