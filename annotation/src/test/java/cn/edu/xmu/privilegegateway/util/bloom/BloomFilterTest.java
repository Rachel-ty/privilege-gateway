package cn.edu.xmu.privilegegateway.util.bloom;

import cn.edu.xmu.privilegegateway.AnnotationApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jianjian Chan
 * @date 2021-11-20
 * @sn 22920192204170
 */
@SpringBootTest(classes = {AnnotationApplication.class})
public class BloomFilterTest {
    @Autowired
    private BloomFilter<String> filter;

    @Autowired
    private BloomFilter<Long> filterInt;

    @Test
    public void filterTest() {
        String filterFor = "test";

        filter.deleteFilter(filterFor);

        assertFalse(filter.newFilter(filterFor, -0.01, 100));
        assertFalse(filter.newFilter(filterFor, 1.01, 100));
        assertFalse(filter.newFilter(filterFor, 0.001, -1));

        assertTrue(filter.newFilter(filterFor, 0.001, 100));
        assertTrue(filter.checkFilter(filterFor));
        assertTrue(filter.deleteFilter(filterFor));
        assertFalse(filter.checkFilter(filterFor));
        assertFalse(filter.deleteFilter(filterFor));

        assertTrue(filter.newFilter(filterFor, null, null));
        filter.deleteFilter(filterFor);
    }

    @Test
    public void valueTest() {
        String filterFor = "test";

        if(!filter.checkFilter(filterFor)) {
            filter.newFilter(filterFor, 0.001, 100);
        }

        assertFalse(filter.checkValue(filterFor, "1"));
        assertTrue(filter.addValue(filterFor, "1"));
        assertTrue(filter.checkValue(filterFor, "1"));
        assertFalse(filter.addValue(filterFor, "1"));

        filter.deleteFilter(filterFor);
    }

    @Test
    public void valueTestInteger() {
        String filterFor = "testI";

        if(!filterInt.checkFilter(filterFor)) {
            filterInt.newFilter(filterFor, 0.001, 100);
        }

        assertFalse(filterInt.checkValue(filterFor, 1L));
        assertTrue(filterInt.addValue(filterFor, 1L));
        assertTrue(filterInt.checkValue(filterFor, 1L));
        assertFalse(filterInt.addValue(filterFor, 1L));

        filterInt.deleteFilter(filterFor);
    }
}
