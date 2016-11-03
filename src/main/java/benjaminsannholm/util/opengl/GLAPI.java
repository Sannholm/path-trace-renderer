package benjaminsannholm.util.opengl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public final class GLAPI
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GLAPI.class);

    private static final boolean ERROR_REPORTING = true;

    private static void checkError()
    {
        if (ERROR_REPORTING)
        {
            final TIntList errors = new TIntArrayList();

            int error;
            while ((error = getError()) != GL11.GL_NO_ERROR)
            {
                errors.add(error);
            }

            if (!errors.isEmpty())
            {
                final StringBuilder message = new StringBuilder();
                message.append("########## GL ERROR ##########\n");
                
                for (TIntIterator it = errors.iterator(); it.hasNext();)
                {
                    message.append(error + ": " + getErrorString(it.next()) + '\n');
                }
                
                message.append(Throwables.getStackTraceAsString(new Throwable()));

                LOGGER.warn(message.toString());
            }
        }
    }
    
    private static int getError()
    {
        return GL11.glGetError();
    }
    
    private static String getErrorString(int error)
    {
        switch (error)
        {
            case GL11.GL_INVALID_ENUM:
                return "Invalid enum";
            case GL11.GL_INVALID_VALUE:
                return "Invalid value";
            case GL11.GL_INVALID_OPERATION:
                return "Invalid operation";
            case GL11.GL_STACK_OVERFLOW:
                return "Stack overflow";
            case GL11.GL_STACK_UNDERFLOW:
                return "Stack underflow";
            case GL11.GL_OUT_OF_MEMORY:
                return "Out of memory";
            case GL30.GL_INVALID_FRAMEBUFFER_OPERATION:
                return "Invalid framebuffer operation";
            case GL45.GL_CONTEXT_LOST:
                return "Context lost";
            default:
                throw new IllegalArgumentException("Unknown error code " + error);
        }
    }
    
    private static void setCapabilityState(int glEnum, boolean enabled)
    {
        if (enabled)
        {
            GL11.glEnable(glEnum);
        }
        else
        {
            GL11.glDisable(glEnum);
        }
        checkError();
    }
    
    public static void setDepthTest(boolean enabled)
    {
        setCapabilityState(GL11.GL_DEPTH_TEST, enabled);
    }
    
    public static void setCullFace(boolean enabled)
    {
        setCapabilityState(GL11.GL_CULL_FACE, enabled);
    }
    
    public static void setTexture1D(boolean enabled)
    {
        setCapabilityState(GL11.GL_TEXTURE_1D, enabled);
    }
    
    public static void setTexture2D(boolean enabled)
    {
        setCapabilityState(GL11.GL_TEXTURE_2D, enabled);
    }
    
    public static void setTexture3D(boolean enabled)
    {
        setCapabilityState(GL12.GL_TEXTURE_3D, enabled);
    }
    
    public static void setFramebufferSRGB(boolean enabled)
    {
        setCapabilityState(GL30.GL_FRAMEBUFFER_SRGB, enabled);
    }

    public static int genFramebuffer()
    {
        final int result = GL30.glGenFramebuffers();
        checkError();
        return result;
    }
    
    public static void deleteFramebuffer(int handle)
    {
        GL30.glDeleteFramebuffers(handle);
        checkError();
    }
    
    public static void bindFramebuffer(int handle)
    {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, handle);
        checkError();
    }
    
    public static int genTexture()
    {
        final int result = GL11.glGenTextures();
        checkError();
        return result;
    }
    
    public static void deleteTexture(int handle)
    {
        GL11.glDeleteTextures(handle);
        checkError();
    }
    
    public static void bindTexture(int target, int unit, int handle)
    {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
        checkError();
        GL11.glBindTexture(target, handle);
        checkError();
    }
    
    public static void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format)
    {
        GL42.glBindImageTexture(unit, texture, level, layered, layer, access, format);
        checkError();
    }
    
    public static void bindImageTexture(int unit, int texture, int access, int format)
    {
        bindImageTexture(unit, texture, 0, false, 0, access, format);
    }
    
    public static void initTexImage(int target, int level, int internalFormat, int width, int height, int depth, int format, int type, ByteBuffer pixels)
    {
        switch (target)
        {
            case GL11.GL_TEXTURE_2D:
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, internalFormat, width, height, 0, format, type, pixels);
                break;
            case GL12.GL_TEXTURE_3D:
                GL12.glTexImage3D(GL12.GL_TEXTURE_3D, level, internalFormat, width, height, depth, 0, format, type, pixels);
                break;
            default:
                throw new IllegalArgumentException("Unsupported texture target");
        }
        checkError();
    }
    
    public static void setTexParameteri(int target, int param, int value)
    {
        GL11.glTexParameteri(target, param, value);
        checkError();
    }
    
    public static void uploadTexImage(int target, int level, int x, int y, int z, int width, int height, int depth, int format, int type, ByteBuffer buffer)
    {
        switch (target)
        {
            case GL11.GL_TEXTURE_2D:
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, x, y, width, height, format, type, buffer);
                break;
            case GL12.GL_TEXTURE_3D:
                GL12.glTexSubImage3D(GL12.GL_TEXTURE_3D, level, x, y, z, width, height, depth, format, type, buffer);
                break;
            default:
                throw new IllegalArgumentException("Unsupported texture target");
        }
        checkError();
    }
    
    public static void generateMipmaps(int target)
    {
        GL30.glGenerateMipmap(target);
        checkError();
    }
    
    public static void setDrawBuffers(IntBuffer data)
    {
        GL20.glDrawBuffers(data);
        checkError();
    }
    
    public static void setFramebufferAttachment(int attachment, int target, int texture, int level, int layer)
    {
        switch (target)
        {
            case GL11.GL_TEXTURE_2D:
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, GL11.GL_TEXTURE_2D, texture, level);
                break;
            case GL12.GL_TEXTURE_3D:
                GL30.glFramebufferTexture3D(GL30.GL_FRAMEBUFFER, attachment, GL12.GL_TEXTURE_3D, texture, level, layer);
                break;
            default:
                throw new IllegalArgumentException("Unsupported texture target");
        }
        checkError();
    }
    
    public static void setFramebufferAttachment(int attachment, int target, int texture)
    {
        setFramebufferAttachment(attachment, target, texture, 0, 0);
    }
    
    public static void setViewport(int x, int y, int width, int height)
    {
        GL11.glViewport(x, y, width, height);
        checkError();
    }
    
    public static void clearColor(float red, float green, float blue, float alpha)
    {
        GL11.glClearColor(red, green, blue, alpha);
        checkError();
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        checkError();
    }
    
    public static void clearDepth(float depth)
    {
        GL11.glClearDepth(depth);
        checkError();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        checkError();
    }
    
    public static int createShader(int type)
    {
        final int result = GL20.glCreateShader(type);
        checkError();
        return result;
    }
    
    public static void deleteShader(int handle)
    {
        GL20.glDeleteShader(handle);
        checkError();
    }
    
    public static void attachShader(int program, int shader)
    {
        GL20.glAttachShader(program, shader);
        checkError();
    }
    
    public static void detachShader(int program, int shader)
    {
        GL20.glDetachShader(program, shader);
        checkError();
    }
    
    public static void setShaderSource(int handle, String source)
    {
        GL20.glShaderSource(handle, source);
        checkError();
    }
    
    public static void compileShader(int handle)
    {
        GL20.glCompileShader(handle);
        checkError();
    }
    
    public static int getShaderi(int handle, int parameter)
    {
        final int result = GL20.glGetShaderi(handle, parameter);
        checkError();
        return result;
    }
    
    public static String getShaderInfoLog(int handle)
    {
        final String result = GL20.glGetShaderInfoLog(handle);
        checkError();
        return result;
    }
    
    public static int createProgram()
    {
        final int result = GL20.glCreateProgram();
        checkError();
        return result;
    }
    
    public static void deleteProgram(int handle)
    {
        GL20.glDeleteProgram(handle);
        checkError();
    }
    
    public static void bindAttribLocation(int program, String name, int location)
    {
        GL20.glBindAttribLocation(program, location, name);
        checkError();
    }
    
    public static void bindFragDataLocation(int program, String name, int location)
    {
        GL30.glBindFragDataLocation(program, location, name);
        checkError();
    }
    
    public static void linkProgram(int handle)
    {
        GL20.glLinkProgram(handle);
        checkError();
    }
    
    public static int getProgrami(int handle, int parameter)
    {
        final int result = GL20.glGetProgrami(handle, parameter);
        checkError();
        return result;
    }
    
    public static String getProgramInfoLog(int handle)
    {
        final String result = GL20.glGetProgramInfoLog(handle);
        checkError();
        return result;
    }
    
    public static void useProgram(int handle)
    {
        GL20.glUseProgram(handle);
        checkError();
    }
    
    public static int getUniformLocation(int program, String name)
    {
        final int result = GL20.glGetUniformLocation(program, name);
        checkError();
        return result;
    }
    
    public static void setUniform1f(int location, float value)
    {
        GL20.glUniform1f(location, value);
        checkError();
    }
    
    public static void setUniform2f(int location, float value1, float value2)
    {
        GL20.glUniform2f(location, value1, value2);
        checkError();
    }
    
    public static void setUniform3f(int location, float value1, float value2, float value3)
    {
        GL20.glUniform3f(location, value1, value2, value3);
        checkError();
    }
    
    public static void setUniform4f(int location, float value1, float value2, float value3, float value4)
    {
        GL20.glUniform4f(location, value1, value2, value3, value4);
        checkError();
    }
    
    public static void setUniform1i(int location, int value)
    {
        GL20.glUniform1i(location, value);
        checkError();
    }
    
    public static void setUniformMatrix4(int location, FloatBuffer buffer)
    {
        GL20.glUniformMatrix4fv(location, false, buffer);
        checkError();
    }
    
    public static void dispatchCompute(int numGroupsX, int numGroupsY, int numGroupsZ)
    {
        GL43.glDispatchCompute(numGroupsX, numGroupsY, numGroupsZ);
        checkError();
    }
    
    public static void enableVertexAttribArray(int index)
    {
        GL20.glEnableVertexAttribArray(index);
        checkError();
    }
    
    public static void disableVertexAttribArray(int index)
    {
        GL20.glDisableVertexAttribArray(index);
        checkError();
    }
    
    public static void setVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset)
    {
        GL20.glVertexAttribPointer(index, size, type, normalized, stride, offset);
        checkError();
    }
    
    public static int genBuffer()
    {
        final int result = GL15.glGenBuffers();
        checkError();
        return result;
    }
    
    public static void bindBuffer(int target, int handle)
    {
        GL15.glBindBuffer(target, handle);
        checkError();
    }
    
    public static void deleteBuffer(int handle)
    {
        GL15.glDeleteBuffers(handle);
        checkError();
    }
    
    public static void initBufferData(int target, int size, int usage)
    {
        GL15.glBufferData(target, size, usage);
        checkError();
    }
    
    public static ByteBuffer mapBuffer(int target, int access, int offset, int length)
    {
        final ByteBuffer result = GL30.glMapBufferRange(target, offset, length, access, null);
        checkError();
        return result;
    }
    
    public static void unmapBuffer(int target)
    {
        GL15.glUnmapBuffer(target);
        checkError();
    }

    public static int genVertexArray()
    {
        final int result = GL30.glGenVertexArrays();
        checkError();
        return result;
    }
    
    public static void bindVertexArray(int handle)
    {
        GL30.glBindVertexArray(handle);
        checkError();
    }
    
    public static void deleteVertexArray(int handle)
    {
        GL30.glDeleteVertexArrays(handle);
        checkError();
    }

    public static void drawArrays(int mode, int start, int count)
    {
        GL11.glDrawArrays(mode, start, count);
        checkError();
    }
    
    public static void memoryBarrier(int barriers)
    {
        GL42.glMemoryBarrier(barriers);
        checkError();
    }
}