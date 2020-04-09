import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(MultiThreadedRunner.class)
public class TestJunit {
    @Rule
    public RetryRule retryRule = new RetryRule(2);
    @Test
    public void testAdd1() throws Exception {
        throw new IOException();

    }
    @Test
    public void testAdd() {
        String str= "Junit is working fine";
        assertEquals("Junit is working fine",str);
        List<Integer> list = new LinkedList<Integer>();
        Map<Integer, Integer> map = new TreeMap<Integer, Integer>();

    }
    @Test
    public void testAdd2() throws Exception{
        throw new IOException();

    }
    @Test
    public void testAdd3() throws Exception{
        throw new IOException();

    }


}