package cn.edu.xmu.privilegegateway.util;

import cn.edu.xmu.privilegegateway.util.cuckoo.CuckooFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Zhiliang Li
 * @date 2021/11/21
 */
public class CuckooFilterTest {
    @Autowired
    private CuckooFilter<String> stringFilter;
    @Autowired
    private CuckooFilter<Integer> intFilter;

    @Test
    public void filterTest() {
        String filterFor = "testString";

        stringFilter.deleteFilter(filterFor);

        assertFalse(stringFilter.newFilter(filterFor, -1, null, null, null));
        assertFalse(stringFilter.newFilter(filterFor, 100, -1, null, null));
        assertFalse(stringFilter.newFilter(filterFor, 100, 3, -1, null));
        assertFalse(stringFilter.newFilter(filterFor, 100, 3, 20, -1));
        assertFalse(stringFilter.newFilter(filterFor, 10, 10, 20, null));

        assertTrue(stringFilter.newFilter(filterFor, 100, null, null, null));
        assertTrue(stringFilter.checkFilter(filterFor));
        assertTrue(stringFilter.deleteFilter(filterFor));
        assertFalse(stringFilter.checkFilter(filterFor));
        assertFalse(stringFilter.deleteFilter(filterFor));

        filterFor = "testInteger";
        intFilter.deleteFilter(filterFor);

        assertTrue(intFilter.newFilter(filterFor, 100, null, null, null));
        assertTrue(intFilter.checkFilter(filterFor));
        assertTrue(intFilter.deleteFilter(filterFor));
        assertFalse(intFilter.checkFilter(filterFor));

        assertTrue(intFilter.newFilter(filterFor, 100, 3, null, null));
        assertTrue(intFilter.deleteFilter(filterFor));

        assertTrue(intFilter.newFilter(filterFor, 100, 3, 100, null));
        assertTrue(intFilter.deleteFilter(filterFor));

        assertTrue(intFilter.newFilter(filterFor, 100, 3, null, 20));
        assertTrue(intFilter.deleteFilter(filterFor));

        assertTrue(intFilter.newFilter(filterFor, 100, 3, 100, 20));
        assertTrue(intFilter.deleteFilter(filterFor));

        assertFalse(intFilter.deleteFilter(filterFor));
    }

    @Test
    public void valueTest() {
        String filterFor = "testString1";

        if (!stringFilter.checkFilter(filterFor)) {
            stringFilter.newFilter(filterFor, 100, null, null, null);
        }

        assertFalse(stringFilter.checkValue(filterFor, "1"));
        assertTrue(stringFilter.addValue(filterFor, "1"));
        assertTrue(stringFilter.checkValue(filterFor, "1"));
        assertTrue(stringFilter.deleteValue(filterFor,"1"));
        assertFalse(stringFilter.checkValue(filterFor, "1"));
        assertTrue(stringFilter.addValue(filterFor, "1"));
        assertTrue(stringFilter.addValue(filterFor, "1"));

        stringFilter.deleteFilter(filterFor);

        filterFor = "testInt1";
        if (!intFilter.checkFilter(filterFor)) {
            intFilter.newFilter(filterFor, 100, null, null, null);
        }

        assertFalse(intFilter.checkValue(filterFor, 1));
        assertTrue(intFilter.addValue(filterFor, 1));
        assertTrue(intFilter.checkValue(filterFor, 1));
        assertTrue(intFilter.addValue(filterFor, 1));

        intFilter.deleteFilter(filterFor);
    }
}
