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
            field.setAccessible(true);
            try
            {
                if (field.getType() == boolean.class)
                {
                    final boolean val = field.getBoolean(GL.getCapabilities());
                    System.out.println(field.getName() + ": " + val);
                }
                else if (field.getType() == long.class)
                {
                    final long val = field.getLong(GL.getCapabilities());
                    System.out.println(field.getName() + ": " + val);
                }
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }
}
