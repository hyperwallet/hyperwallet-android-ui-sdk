package com.hyperwallet.android.ui.transfermethod.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class FieldMapKeyTest {

    @Test
    public void testEquals_withSameReference() {
        FieldMapKey thisKey = new FieldMapKey("US", "USD", "BANK_ACCOUNT");
        assertEquals(thisKey, thisKey);
    }

    @Test
    public void testEquals_withSameReference_withAnotherConstructor() {
        FieldMapKey thisKey = new FieldMapKey("BANK_ACCOUNT");
        assertEquals(thisKey, thisKey);
    }

    @Test
    public void testEquals_withDifferentObjectType() {
        FieldMapKey thisKey = new FieldMapKey("US", "USD", "BANK_ACCOUNT");
        Object thatKey = new Object();
        assertNotEquals(thisKey, thatKey);
    }

    @Test
    public void testEquals_withDifferentReferencesSameValues() {
        FieldMapKey thisKey = new FieldMapKey("US", "USD", "BANK_ACCOUNT");
        FieldMapKey thatKey = new FieldMapKey("US", "USD", "BANK_ACCOUNT");
        assertEquals(thisKey, thatKey);
    }

    @Test
    public void testEquals_withDifferentReferencesDifferentValues() {
        FieldMapKey thisKey = new FieldMapKey("US", "USD", "BANK_ACCOUNT");
        FieldMapKey thatKey = new FieldMapKey("CA", "CAD", "BANK_ACCOUNT");
        assertNotEquals(thisKey, thatKey);
    }
    @Test
    public void testEquals_withDifferentReferencesDifferentValues_withConstructor() {
        FieldMapKey thisKey = new FieldMapKey("BANK_ACCOUNT");
        FieldMapKey thatKey = new FieldMapKey("BANK_CARD");
        assertNotEquals(thisKey, thatKey);
    }
}
