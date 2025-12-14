package org.taranix.cafe.beans.metadata.data;

/**
 * Test fixture with two constructors to trigger the multiple-constructor error.
 */
public class TwoConstructorsClass {

    public TwoConstructorsClass() {
    }

    public TwoConstructorsClass(int x) {
    }
}