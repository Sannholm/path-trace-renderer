package benjaminsannholm.util;

import java.lang.reflect.Field;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

public class LwjglUtils
{
    public static void printExtensions()
    {
        for (Field field : GLCapabilities.class.getDeclaredFields())
        {
            if (field.getType() == boolean.class)
            {
                field.setAccessible(true);
                try
                {
                    final boolean val = field.getBoolean(GL.getCapabilities());
                    System.out.println(field.getName() + ": " + val);
                }
                catch (IllegalArgumentException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
