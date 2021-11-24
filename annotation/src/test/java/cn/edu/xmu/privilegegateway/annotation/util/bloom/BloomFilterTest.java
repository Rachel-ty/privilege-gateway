package cn.edu.xmu.privilegegateway.annotation.util.bloom;

import cn.edu.xmu.privilegegateway.annotation.AnnotationApplication;
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
    private BloomFilter<String> stringFilter;

    @Autowired
    private BloomFilter<Long> longFilter;

    /**
     * 测试对filter的操作
     */
    @Test
    public void filterTest() {
        String filterFor = "test";

        stringFilter.deleteFilter(filterFor);

        //测试参数不正确时的状况
        assertFalse(stringFilter.newFilter(filterFor, -0.01, 100));
        assertFalse(stringFilter.newFilter(filterFor, 1.01, 100));
        assertFalse(stringFilter.newFilter(filterFor, 0.001, -1));

        //测试新建filter成功的状况
        assertTrue(stringFilter.newFilter(filterFor, 0.001, 100));

        //测试重复新建filter的状况
        assertFalse(stringFilter.newFilter(filterFor, 0.001, 100));

        //测试新建的filter是否存在
        assertTrue(stringFilter.checkFilter(filterFor));

        //测试是否可以删除新建的filter
        assertTrue(stringFilter.deleteFilter(filterFor));

        //测试删除的filter是否存在
        assertFalse(stringFilter.checkFilter(filterFor));

        //测试重复删除filter的情况
        assertFalse(stringFilter.deleteFilter(filterFor));

        //测试以默认参数新建filter的情况
        assertTrue(stringFilter.newFilter(filterFor, null, null));

        stringFilter.deleteFilter(filterFor);
    }

    /**
     * 测试对StringFilter中的值操作
     */
    @Test
    public void stringValueTest() {
        String filterFor = "test";

        if(stringFilter.checkFilter(filterFor)) {
            stringFilter.deleteFilter(filterFor);
        }

        //检测不存在的filter中是否存在值
        assertFalse(stringFilter.checkValue(filterFor, "1"));

        stringFilter.newFilter(filterFor, 0.001, 100);

        //检测新建的filter中是否存在值
        assertFalse(stringFilter.checkValue(filterFor, "1"));

        //检测是否可以往新建的filter中插入值
        assertTrue(stringFilter.addValue(filterFor, "1"));

        //检测插入的值是否存在
        assertTrue(stringFilter.checkValue(filterFor, "1"));

        //检测是否可以往filter中重复插入值
        assertFalse(stringFilter.addValue(filterFor, "1"));

        stringFilter.deleteFilter(filterFor);
    }

    /**
     * 测试对LongFilter中的值操作，与stringValueTest基本相同
     */
    @Test
    public void longValueTest() {
        String filterFor = "test";

        if(longFilter.checkFilter(filterFor)) {
            longFilter.deleteFilter(filterFor);
        }

        //检测不存在的filter中是否存在值
        assertFalse(longFilter.checkValue(filterFor, 1L));

        longFilter.newFilter(filterFor, 0.001, 100);

        assertFalse(longFilter.checkValue(filterFor, 1L));
        assertTrue(longFilter.addValue(filterFor, 1L));
        assertTrue(longFilter.checkValue(filterFor, 1L));
        assertFalse(longFilter.addValue(filterFor, 1L));

        longFilter.deleteFilter(filterFor);
    }
}
