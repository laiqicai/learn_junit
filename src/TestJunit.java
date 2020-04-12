import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

@RunWith(MultiThreadedRunner.class)
public class TestJunit {

    @Test
    public void testAdd1() throws Exception {
        String str= "Junit is working fine";
        assertEquals("Junit is working fine",str);

    }
    @Test
    public void testAdd() {
        String str= "Junit is working fine";
        assertEquals("Junit is working fine",str);

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