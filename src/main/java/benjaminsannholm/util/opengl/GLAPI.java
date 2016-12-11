package benjaminsannholm.util.opengl;

import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.GLDebugMessageCallbackI;
import org.lwjgl.system.APIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GLAPI
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GLAPI.class);

    private static final boolean DEBUG_PRINTING = true;

    public static void setupDebugPrinting()
    {
        if (DEBUG_PRINTING)
        {
            if ((getInteger(GL30.GL_CONTEXT_FLAGS) & GL43.GL_CONTEXT_FLAG_DEBUG_BIT) == 0)
                LOGGER.warn("A non-debug context may not produce any debug output.");
            
            setDebugOutput(true);
            setDebugOutputSynchronous(true);
            setDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, null, true);
            
            setDebugMessageCallback(GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) ->
            {
                final String output = "[OpenGL] "
                        + String.format("[%s][%s][%s] 0x%X: ",
                                getDebugSource(source),
                                getDebugSeverity(severity),
                                getDebugType(type),
                                id)
                        + GLDebugMessageCallback.getMessage(length, message);
                
                LOGGER.warn(output, new Throwable());
            }), NULL);
        }
    }
    
    private static String getDebugSource(int source)
    {
        switch (source)
        {
            case GL43.GL_DEBUG_SOURCE_API:
                return "API";
            case GL43.GL_DEBUG_SOURCE_WINDOW_SYSTEM:
                return "Window system";
            case GL43.GL_DEBUG_SOURCE_SHADER_COMPILER:
                return "Shader compiler";
            case GL43.GL_DEBUG_SOURCE_THIRD_PARTY:
                return "Third party";
            case GL43.GL_DEBUG_SOURCE_APPLICATION:
                return "Application";
            case GL43.GL_DEBUG_SOURCE_OTHER:
                return "Other";
            default:
                return APIUtil.apiUnknownToken(source);
        }
    }
    
    private static String getDebugType(int type)
    {
        switch (type)
        {
            case GL43.GL_DEBUG_TYPE_ERROR:
                return "Error";
            case GL43.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR:
                return "Deprecated behavior";
            case GL43.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR:
                return "Undefined behavior";
            case GL43.GL_DEBUG_TYPE_PORTABILITY:
                return "Portability";
            case GL43.GL_DEBUG_TYPE_PERFORMANCE:
                return "Performance";
            case GL43.GL_DEBUG_TYPE_OTHER:
                return "Other";
            case GL43.GL_DEBUG_TYPE_MARKER:
                return "Marker";
            default:
                return APIUtil.apiUnknownToken(type);
        }
    }
    
    private static String getDebugSeverity(int severity)
    {
        switch (severity)
        {
            case GL43.GL_DEBUG_SEVERITY_HIGH:
                return "High";
            case GL43.GL_DEBUG_SEVERITY_MEDIUM:
                return "Medium";
            case GL43.GL_DEBUG_SEVERITY_LOW:
                return "Low";
            case GL43.GL_DEBUG_SEVERITY_NOTIFICATION:
                return "Notification";
            default:
                return APIUtil.apiUnknownToken(severity);
        }
    }

    @Deprecated
    private static void checkError()
    {
    }
    
    @Deprecated
    private static int getError()
    {
        return GL11.glGetError();
    }
    
    private static void setDebugMessageCallback(GLDebugMessageCallbackI callback, long userParam)
    {
        GL43.glDebugMessageCallback(callback, userParam);
        checkError();
    }

    private static void setDebugMessageControl(int source, int type, int severity, IntBuffer ids, boolean enabled)
    {
        GL43.glDebugMessageControl(source, type, severity, ids, enabled);
        checkError();
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
    
    public static void setDebugOutput(boolean enabled)
    {
        setCapabilityState(GL43.GL_DEBUG_OUTPUT, enabled);
    }
    
    public static void setDebugOutputSynchronous(boolean enabled)
    {
        setCapabilityState(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS, enabled);
    }

    private static int getInteger(int parameter)
    {
        final int result = GL11.glGetInteger(parameter);
        checkError();
        return result;
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
    
    public static int createTexture(int target)
    {
        final int result = GL45.glCreateTextures(target);
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
        GL45.glBindTextureUnit(unit, handle);
        checkError();
        
        /*GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
        checkError();
        GL11.glBindTexture(target, handle);
        checkError();*/
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
    
    public static void getProgramBinary(int program, IntBuffer length, IntBuffer format, ByteBuffer binary)
    {
        GL41.glGetProgramBinary(program, length, format, binary);
        checkError();
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
    
    public static void setUniform1f(int location, float v0)
    {
        GL20.glUniform1f(location, v0);
        checkError();
    }
    
    public static void setUniform2f(int location, float v0, float v1)
    {
        GL20.glUniform2f(location, v0, v1);
        checkError();
    }
    
    public static void setUniform3f(int location, float v0, float v1, float v2)
    {
        GL20.glUniform3f(location, v0, v1, v2);
        checkError();
    }
    
    public static void setUniform4f(int location, float v0, float v1, float v2, float v3)
    {
        GL20.glUniform4f(location, v0, v1, v2, v3);
        checkError();
    }

    public static void setUniform1i(int location, int v0)
    {
        GL20.glUniform1i(location, v0);
        checkError();
    }
    
    public static void setUniform2i(int location, int v0, int v1)
    {
        GL20.glUniform2i(location, v0, v1);
        checkError();
    }
    
    public static void setUniform3i(int location, int v0, int v1, int v2)
    {
        GL20.glUniform3i(location, v0, v1, v2);
        checkError();
    }
    
    public static void setUniform4i(int location, int v0, int v1, int v2, int v3)
    {
        GL20.glUniform4i(location, v0, v1, v2, v3);
        checkError();
    }
    
    public static void setUniform1fv(int location, FloatBuffer buffer)
    {
        GL20.glUniform1fv(location, buffer);
        checkError();
    }
    
    public static void setUniform2fv(int location, FloatBuffer buffer)
    {
        GL20.glUniform2fv(location, buffer);
        checkError();
    }
    
    public static void setUniform3fv(int location, FloatBuffer buffer)
    {
        GL20.glUniform3fv(location, buffer);
        checkError();
    }
    
    public static void setUniform4fv(int location, FloatBuffer buffer)
    {
        GL20.glUniform4fv(location, buffer);
        checkError();
    }
    
    public static void setUniform1iv(int location, IntBuffer buffer)
    {
        GL20.glUniform1iv(location, buffer);
        checkError();
    }
    
    public static void setUniform2iv(int location, IntBuffer buffer)
    {
        GL20.glUniform2iv(location, buffer);
        checkError();
    }
    
    public static void setUniform3iv(int location, IntBuffer buffer)
    {
        GL20.glUniform3iv(location, buffer);
        checkError();
    }
    
    public static void setUniform4iv(int location, IntBuffer buffer)
    {
        GL20.glUniform4iv(location, buffer);
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