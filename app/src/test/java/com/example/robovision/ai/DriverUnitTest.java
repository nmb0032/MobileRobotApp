package com.example.robovision.ai;

import com.example.robovision.ai.Driver;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class DriverUnitTest {
    @Test
    public void string_builder_test(){
        String result = Driver.buildString(100, 100);
        assertEquals("+100#+100#", result);
        result = Driver.buildString(-84, 230);
        assertEquals("+230#-084#", result);
    }
}
