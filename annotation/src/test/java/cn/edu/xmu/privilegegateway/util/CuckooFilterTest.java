package cn.edu.xmu.privilegegateway.util;

import cn.edu.xmu.privilegegateway.util.cuckoo.CuckooFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CuckooFilterTest {
    @Autowired
    private CuckooFilter<String> stringFilter;
    @Autowired
    private CuckooFilter<Integer> intFilter;

    @Test
    public void filterTest() {
        String filterFor = "testString";

        stringFilter.deleteFilter(filterFor);

        assertFalse(stringFilter.newFilter(filterFor, -0.01, 100));
        assertFalse(stringFilter.newFilter(filterFor, 1.01, 100));
        assertFalse(stringFilter.newFilter(filterFor, 0.001, -1));

        assertTrue(stringFilter.newFilter(filterFor, 0.001, 100));
        assertTrue(stringFilter.checkFilter(filterFor));
        assertTrue(stringFilter.deleteFilter(filterFor));
        assertFalse(stringFilter.checkFilter(filterFor));
        assertFalse(stringFilter.deleteFilter(filterFor));

        filterFor = "testInteger";
        intFilter.deleteFilter(filterFor);

        assertFalse(intFilter.newFilter(filterFor, -0.01, 100));
        assertFalse(intFilter.newFilter(filterFor, 1.01, 100));
        assertFalse(intFilter.newFilter(filterFor, 0.001, -1));

        assertTrue(intFilter.newFilter(filterFor, 0.001, 100));
        assertTrue(intFilter.checkFilter(filterFor));
        assertTrue(intFilter.deleteFilter(filterFor));
        assertFalse(intFilter.checkFilter(filterFor));
        assertFalse(intFilter.deleteFilter(filterFor));
    }

    @Test
    public void valueTest() {
        String filterFor = "testString";

        if (!stringFilter.checkFilter(filterFor)) {
            stringFilter.newFilter(filterFor, 0.001, 100);
        }

        assertFalse(stringFilter.checkValue(filterFor, "1"));
        assertTrue(stringFilter.addValue(filterFor, "1"));
        assertTrue(stringFilter.checkValue(filterFor, "1"));
        assertFalse(stringFilter.addValue(filterFor, "1"));

        stringFilter.deleteFilter(filterFor);

        filterFor = "testInt";
        if (!intFilter.checkFilter(filterFor)) {
            intFilter.newFilter(filterFor, 0.001, 100);
        }

        assertFalse(intFilter.checkValue(filterFor, 1));
        assertTrue(intFilter.addValue(filterFor, 1));
        assertTrue(intFilter.checkValue(filterFor, 1));
        assertFalse(intFilter.addValue(filterFor, 1));

        intFilter.deleteFilter(filterFor);
    }
}
