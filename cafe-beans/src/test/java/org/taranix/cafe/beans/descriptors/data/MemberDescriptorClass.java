package org.taranix.cafe.beans.descriptors.data;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;

public class MemberDescriptorClass {
    public static final String SOME_EXAMPLE_ID = "some-example-id";
    private Integer integer;

    @CafeName(value = SOME_EXAMPLE_ID)
    public String getString() {
        throw new NotImplementedException();
    }

    public String getString(String value) {
        throw new NotImplementedException();
    }
}
