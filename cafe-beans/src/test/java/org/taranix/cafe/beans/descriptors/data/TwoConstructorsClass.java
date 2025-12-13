package org.taranix.cafe.beans.descriptors.data;

/**
 * Test fixture with two constructors to trigger the multiple-constructor error.
 */
public class TwoConstructorsClass {

    public TwoConstructorsClass() {
    }

    public TwoConstructorsClass(int x) {
    }
}