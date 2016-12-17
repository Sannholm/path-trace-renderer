package benjaminsannholm.util.opengl;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.ARBTextureStorage;
import org.lwjgl.opengl.EXTDirectStateAccess;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.GLDebugMessageCallbackI;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.MemoryStack;
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
                switch (id)
                {
                    // NVidia
                    case 0x20071: // Buffer detailed info: 'will use VIDEO memory as the source for buffer object'
                    case 0x20092: // Program/shader state performance warning: Fragment Shader is going to be recompiled because the shader key based on GL state mismatches.
                    case 0x20072: // Buffer performance warning: Buffer object is being copied/moved from VIDEO memory to HOST memory.
                    case 0x20070: // Buffer info (memory usage)
                        return;
                }
                
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

    private static void setDebugMessageCallback(GLDebugMessageCallbackI callback, long userParam)
    {
        GL43.glDebugMessageCallback(callback, userParam);
    }
    
    private static void setDebugMessageControl(int source, int type, int severity, IntBuffer ids, boolean enabled)
    {
        GL43.glDebugMessageControl(source, type, severity, ids, enabled);
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
    }

    public static void setDepthTest(boolean enabled)
    {
        setCapabilityState(GL11.GL_DEPTH_TEST, enabled);
    }

    public static void setCullFace(boolean enabled)
    {
        setCapabilityState(GL11.GL_CULL_FACE, enabled);
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
    
    public static int getInteger(int param)
    {
        return GL11.glGetInteger(param);
    }

    public static void setViewport(int x, int y, int width, int height)
    {
        GL11.glViewport(x, y, width, height);
    }
    
    public static int createFramebuffer()
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
            return GL45.glCreateFramebuffers();
        if (caps.GL_ARB_direct_state_access)
            return ARBDirectStateAccess.glCreateFramebuffers();
        final int handle = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, handle);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        return handle;
    }

    public static void deleteFramebuffer(int framebuffer)
    {
        GL30.glDeleteFramebuffers(framebuffer);
    }

    public static void bindFramebuffer(int framebuffer)
    {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
    }

    public static void setFramebufferAttachment(int framebuffer, int attachment, int texture, int level)
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
        {
            GL45.glNamedFramebufferTexture(framebuffer, attachment, texture, level);
        }
        else if (caps.GL_ARB_direct_state_access)
        {
            ARBDirectStateAccess.glNamedFramebufferTexture(framebuffer, attachment, texture, level);
        }
        else if (caps.GL_EXT_direct_state_access)
        {
            EXTDirectStateAccess.glNamedFramebufferTextureEXT(framebuffer, attachment, texture, level);
        }
        else
        {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
            GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, attachment, texture, level);
        }
    }

    public static void setFramebufferAttachment(int framebuffer, int attachment, int texture)
    {
        setFramebufferAttachment(framebuffer, attachment, texture, 0);
    }

    public static void setDrawBuffers(int framebuffer, IntBuffer bufs)
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
        {
            GL45.glNamedFramebufferDrawBuffers(framebuffer, bufs);
        }
        else if (caps.GL_ARB_direct_state_access)
        {
            ARBDirectStateAccess.glNamedFramebufferDrawBuffers(framebuffer, bufs);
        }
        else if (caps.GL_EXT_direct_state_access)
        {
            EXTDirectStateAccess.glFramebufferDrawBuffersEXT(framebuffer, bufs);
        }
        else
        {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
            GL20.glDrawBuffers(bufs);
        }
    }

    public static int checkFramebufferStatus(int framebuffer)
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
            return GL45.glCheckNamedFramebufferStatus(framebuffer, GL30.GL_FRAMEBUFFER);
        if (caps.GL_ARB_direct_state_access)
            return ARBDirectStateAccess.glCheckNamedFramebufferStatus(framebuffer, GL30.GL_FRAMEBUFFER);
        if (caps.GL_EXT_direct_state_access)
            return EXTDirectStateAccess.glCheckNamedFramebufferStatusEXT(framebuffer, GL30.GL_FRAMEBUFFER);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        return GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
    }
    
    public static void clearBufferColor(int framebuffer, int drawbuffer, float r, float g, float b, float a)
    {
        try (MemoryStack stack = stackPush())
        {
            final FloatBuffer value = stack.floats(r, g, b, a);
            
            final GLCapabilities caps = GL.getCapabilities();
            if (caps.OpenGL45)
            {
                GL45.glClearNamedFramebufferfv(framebuffer, GL11.GL_COLOR, drawbuffer, value);
            }
            else if (caps.GL_ARB_direct_state_access)
            {
                ARBDirectStateAccess.glClearNamedFramebufferfv(framebuffer, GL11.GL_COLOR, drawbuffer, value);
            }
            else
            {
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
                GL30.glClearBufferfv(GL11.GL_COLOR, drawbuffer, value);
            }
        }
    }

    public static void clearBufferDepth(int framebuffer, float depth)
    {
        try (MemoryStack stack = stackPush())
        {
            final FloatBuffer value = stack.floats(depth);
            
            final GLCapabilities caps = GL.getCapabilities();
            if (caps.OpenGL45)
            {
                GL45.glClearNamedFramebufferfv(framebuffer, GL11.GL_DEPTH, 0, value);
            }
            else if (caps.GL_ARB_direct_state_access)
            {
                ARBDirectStateAccess.glClearNamedFramebufferfv(framebuffer, GL11.GL_DEPTH, 0, value);
            }
            else
            {
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
                GL30.glClearBufferfv(GL11.GL_DEPTH, 0, value);
            }
        }
    }

    public static int createTexture(int target)
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
            return GL45.glCreateTextures(target);
        if (caps.GL_ARB_direct_state_access)
            return ARBDirectStateAccess.glCreateTextures(target);
        final int handle = GL11.glGenTextures();
        GL11.glBindTexture(target, handle);
        GL11.glBindTexture(target, 0);
        return handle;
    }

    public static void deleteTexture(int texture)
    {
        GL11.glDeleteTextures(texture);
    }

    public static void bindTexture(int target, int unit, int texture)
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
        {
            GL45.glBindTextureUnit(unit, texture);
        }
        else if (caps.GL_ARB_direct_state_access)
        {
            ARBDirectStateAccess.glBindTextureUnit(unit, texture);
        }
        else if (caps.GL_EXT_direct_state_access)
        {
            EXTDirectStateAccess.glBindMultiTextureEXT(GL13.GL_TEXTURE0 + unit, target, texture);
        }
        else
        {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
            GL11.glBindTexture(target, texture);
        }
    }

    public static void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format)
    {
        GL42.glBindImageTexture(unit, texture, level, layered, layer, access, format);
    }

    public static void bindImageTexture(int unit, int texture, int access, int format)
    {
        bindImageTexture(unit, texture, 0, false, 0, access, format);
    }

    public static void initTextureImage(int texture, int target, int levels, int internalFormat, int width, int height, int depth)
    {
        final GLCapabilities caps = GL.getCapabilities();
        switch (target)
        {
            case GL11.GL_TEXTURE_2D:
                if (caps.OpenGL45)
                {
                    GL45.glTextureStorage2D(texture, levels, internalFormat, width, height);
                }
                else if (caps.GL_ARB_direct_state_access)
                {
                    ARBDirectStateAccess.glTextureStorage2D(texture, levels, internalFormat, width, height);
                }
                else if (caps.GL_ARB_texture_storage)
                {
                    ARBTextureStorage.glTextureStorage2DEXT(texture, target, levels, internalFormat, width, height);
                }
                else
                {
                    GL11.glBindTexture(target, texture);
                    GL42.glTexStorage2D(target, levels, internalFormat, width, height);
                }
                break;
            case GL12.GL_TEXTURE_3D:
                if (caps.OpenGL45)
                {
                    GL45.glTextureStorage3D(texture, levels, internalFormat, width, height, depth);
                }
                else if (caps.GL_ARB_direct_state_access)
                {
                    ARBDirectStateAccess.glTextureStorage3D(texture, levels, internalFormat, width, height, depth);
                }
                else if (caps.GL_ARB_texture_storage)
                {
                    ARBTextureStorage.glTextureStorage3DEXT(texture, target, levels, internalFormat, width, height, depth);
                }
                else
                {
                    GL11.glBindTexture(target, texture);
                    GL42.glTexStorage3D(target, levels, internalFormat, width, height, depth);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported texture target");
        }
    }

    public static void setTextureParameteri(int texture, int target, int param, int value)
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
        {
            GL45.glTextureParameteri(texture, param, value);
        }
        else if (caps.GL_ARB_direct_state_access)
        {
            ARBDirectStateAccess.glTextureParameteri(texture, param, value);
        }
        else if (caps.GL_EXT_direct_state_access)
        {
            EXTDirectStateAccess.glTextureParameteriEXT(texture, target, param, value);
        }
        else
        {
            GL11.glBindTexture(target, texture);
            GL11.glTexParameteri(target, param, value);
        }
    }

    public static void uploadTextureImage(int texture, int target, int level, int x, int y, int z, int width, int height, int depth, int format, int type, ByteBuffer buffer)
    {
        final GLCapabilities caps = GL.getCapabilities();
        switch (target)
        {
            case GL11.GL_TEXTURE_2D:
                if (caps.OpenGL45)
                {
                    GL45.glTextureSubImage2D(texture, level, x, y, width, height, format, type, buffer);
                }
                else if (caps.GL_ARB_direct_state_access)
                {
                    ARBDirectStateAccess.glTextureSubImage2D(texture, level, x, y, width, height, format, type, buffer);
                }
                else if (caps.GL_EXT_direct_state_access)
                {
                    EXTDirectStateAccess.glTextureSubImage2DEXT(texture, target, level, x, y, width, height, format, type, buffer);
                }
                else
                {
                    GL11.glBindTexture(target, texture);
                    GL11.glTexSubImage2D(target, level, x, y, width, height, format, type, buffer);
                }
                break;
            case GL12.GL_TEXTURE_3D:
                if (caps.OpenGL45)
                {
                    GL45.glTextureSubImage3D(texture, level, x, y, z, width, height, depth, format, type, buffer);
                }
                else if (caps.GL_ARB_direct_state_access)
                {
                    ARBDirectStateAccess.glTextureSubImage3D(texture, level, x, y, z, width, height, depth, format, type, buffer);
                }
                else if (caps.GL_EXT_direct_state_access)
                {
                    EXTDirectStateAccess.glTextureSubImage3DEXT(texture, target, level, x, y, z, width, height, depth, format, type, buffer);
                }
                else
                {
                    GL11.glBindTexture(target, texture);
                    GL12.glTexSubImage3D(target, level, x, y, z, width, height, depth, format, type, buffer);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported texture target");
        }
    }

    public static void generateTextureMipmaps(int texture, int target)
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
        {
            GL45.glGenerateTextureMipmap(texture);
        }
        else if (caps.GL_ARB_direct_state_access)
        {
            ARBDirectStateAccess.glGenerateTextureMipmap(texture);
        }
        else if (caps.GL_EXT_direct_state_access)
        {
            EXTDirectStateAccess.glGenerateTextureMipmapEXT(texture, target);
        }
        else
        {
            GL11.glBindTexture(target, texture);
            GL30.glGenerateMipmap(target);
        }
    }

    public static int createShader(int type)
    {
        return GL20.glCreateShader(type);
    }

    public static void deleteShader(int shader)
    {
        GL20.glDeleteShader(shader);
    }

    public static void setShaderSource(int shader, String source)
    {
        GL20.glShaderSource(shader, source);
    }

    public static void compileShader(int shader)
    {
        GL20.glCompileShader(shader);
    }

    public static int getShaderi(int shader, int param)
    {
        return GL20.glGetShaderi(shader, param);
    }

    public static String getShaderInfoLog(int shader)
    {
        return GL20.glGetShaderInfoLog(shader);
    }

    public static int createProgram()
    {
        return GL20.glCreateProgram();
    }

    public static void deleteProgram(int program)
    {
        GL20.glDeleteProgram(program);
    }

    public static void useProgram(int program)
    {
        GL20.glUseProgram(program);
    }

    public static void attachShader(int program, int shader)
    {
        GL20.glAttachShader(program, shader);
    }

    public static void detachShader(int program, int shader)
    {
        GL20.glDetachShader(program, shader);
    }

    public static void bindAttribLocation(int program, String name, int location)
    {
        GL20.glBindAttribLocation(program, location, name);
    }

    public static void bindFragDataLocation(int program, String name, int location)
    {
        GL30.glBindFragDataLocation(program, location, name);
    }

    public static void linkProgram(int program)
    {
        GL20.glLinkProgram(program);
    }

    public static int getProgrami(int program, int param)
    {
        return GL20.glGetProgrami(program, param);
    }

    public static String getProgramInfoLog(int program)
    {
        return GL20.glGetProgramInfoLog(program);
    }

    public static void getProgramBinary(int program, IntBuffer length, IntBuffer format, ByteBuffer binary)
    {
        GL41.glGetProgramBinary(program, length, format, binary);
    }

    public static int getUniformLocation(int program, String name)
    {
        return GL20.glGetUniformLocation(program, name);
    }

    public static void setUniform1f(int program, int location, float v0)
    {
        GL41.glProgramUniform1f(program, location, v0);
    }

    public static void setUniform2f(int program, int location, float v0, float v1)
    {
        GL41.glProgramUniform2f(program, location, v0, v1);
    }

    public static void setUniform3f(int program, int location, float v0, float v1, float v2)
    {
        GL41.glProgramUniform3f(program, location, v0, v1, v2);
    }

    public static void setUniform4f(int program, int location, float v0, float v1, float v2, float v3)
    {
        GL41.glProgramUniform4f(program, location, v0, v1, v2, v3);
    }
    
    public static void setUniform1i(int program, int location, int v0)
    {
        GL41.glProgramUniform1i(program, location, v0);
    }

    public static void setUniform2i(int program, int location, int v0, int v1)
    {
        GL41.glProgramUniform2i(program, location, v0, v1);
    }

    public static void setUniform3i(int program, int location, int v0, int v1, int v2)
    {
        GL41.glProgramUniform3i(program, location, v0, v1, v2);
    }

    public static void setUniform4i(int program, int location, int v0, int v1, int v2, int v3)
    {
        GL41.glProgramUniform4i(program, location, v0, v1, v2, v3);
    }

    public static void setUniform1fv(int program, int location, FloatBuffer buffer)
    {
        GL41.glProgramUniform1fv(program, location, buffer);
    }

    public static void setUniform2fv(int program, int location, FloatBuffer buffer)
    {
        GL41.glProgramUniform2fv(program, location, buffer);
    }

    public static void setUniform3fv(int program, int location, FloatBuffer buffer)
    {
        GL41.glProgramUniform3fv(program, location, buffer);
    }

    public static void setUniform4fv(int program, int location, FloatBuffer buffer)
    {
        GL41.glProgramUniform4fv(program, location, buffer);
    }

    public static void setUniform1iv(int program, int location, IntBuffer buffer)
    {
        GL41.glProgramUniform1iv(program, location, buffer);
    }

    public static void setUniform2iv(int program, int location, IntBuffer buffer)
    {
        GL41.glProgramUniform2iv(program, location, buffer);
    }

    public static void setUniform3iv(int program, int location, IntBuffer buffer)
    {
        GL41.glProgramUniform3iv(program, location, buffer);
    }

    public static void setUniform4iv(int program, int location, IntBuffer buffer)
    {
        GL41.glProgramUniform4iv(program, location, buffer);
    }

    public static void setUniformMatrix4(int program, int location, FloatBuffer buffer)
    {
        GL41.glProgramUniformMatrix4fv(program, location, false, buffer);
    }

    public static void dispatchCompute(int numGroupsX, int numGroupsY, int numGroupsZ)
    {
        GL43.glDispatchCompute(numGroupsX, numGroupsY, numGroupsZ);
    }

    public static int createBuffer()
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
            return GL45.glCreateBuffers();
        if (caps.GL_ARB_direct_state_access)
            return ARBDirectStateAccess.glCreateBuffers();
        return GL15.glGenBuffers(); // No need to bind
    }

    public static void deleteBuffer(int buffer)
    {
        GL15.glDeleteBuffers(buffer);
    }

    public static void bindBuffer(int target, int buffer)
    {
        GL15.glBindBuffer(target, buffer);
    }

    // TODO: Buffer storage
    public static void initBufferData(int buffer, int target, int size, int usage)
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
        {
            GL45.glNamedBufferData(buffer, size, usage);
        }
        else if (caps.GL_ARB_direct_state_access)
        {
            ARBDirectStateAccess.glNamedBufferData(buffer, size, usage);
        }
        else if (caps.GL_EXT_direct_state_access)
        {
            EXTDirectStateAccess.glNamedBufferDataEXT(buffer, size, usage);
        }
        else
        {
            GL15.glBindBuffer(target, buffer);
            GL15.glBufferData(target, size, usage);
        }
    }

    public static ByteBuffer mapBuffer(int buffer, int target, int access, int offset, int length)
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
            return GL45.glMapNamedBufferRange(buffer, offset, length, access);
        if (caps.GL_ARB_direct_state_access)
            return ARBDirectStateAccess.glMapNamedBufferRange(buffer, offset, length, access);
        if (caps.GL_EXT_direct_state_access)
            return EXTDirectStateAccess.glMapNamedBufferRangeEXT(buffer, offset, length, access);
        GL15.glBindBuffer(target, buffer);
        return GL30.glMapBufferRange(target, offset, length, access);
    }

    public static boolean unmapBuffer(int buffer, int target)
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
            return GL45.glUnmapNamedBuffer(buffer);
        if (caps.GL_ARB_direct_state_access)
            return ARBDirectStateAccess.glUnmapNamedBuffer(buffer);
        if (caps.GL_EXT_direct_state_access)
            return EXTDirectStateAccess.glUnmapNamedBufferEXT(buffer);
        return GL15.glUnmapBuffer(target);
    }
    
    public static int createVertexArray()
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
            return GL45.glCreateVertexArrays();
        if (caps.GL_ARB_direct_state_access)
            return ARBDirectStateAccess.glCreateVertexArrays();
        final int handle = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(handle);
        GL30.glBindVertexArray(0);
        return handle;
    }

    public static void deleteVertexArray(int vao)
    {
        GL30.glDeleteVertexArrays(vao);
    }

    public static void bindVertexArray(int vao)
    {
        GL30.glBindVertexArray(vao);
    }

    public static void enableVertexAttribArray(int vao, int index)
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
        {
            GL45.glEnableVertexArrayAttrib(vao, index);
        }
        else if (caps.GL_ARB_direct_state_access)
        {
            ARBDirectStateAccess.glEnableVertexArrayAttrib(vao, index);
        }
        else if (caps.GL_EXT_direct_state_access)
        {
            EXTDirectStateAccess.glEnableVertexArrayAttribEXT(vao, index);
        }
        else
        {
            GL30.glBindVertexArray(vao);
            GL20.glEnableVertexAttribArray(index);
        }
    }

    public static void disableVertexAttribArray(int vao, int index)
    {
        final GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL45)
        {
            GL45.glDisableVertexArrayAttrib(vao, index);
        }
        else if (caps.GL_ARB_direct_state_access)
        {
            ARBDirectStateAccess.glDisableVertexArrayAttrib(vao, index);
        }
        else if (caps.GL_EXT_direct_state_access)
        {
            EXTDirectStateAccess.glDisableVertexArrayAttribEXT(vao, index);
        }
        else
        {
            GL30.glBindVertexArray(vao);
            GL20.glDisableVertexAttribArray(index);
        }
    }

    // TODO: Replace with format and binding functions
    public static void setVertexAttribPointer(int vao, int index, int size, int type, boolean normalized, int stride, int offset)
    {
        GL30.glBindVertexArray(vao);
        GL20.glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }
    
    public static void drawArrays(int mode, int start, int count)
    {
        GL11.glDrawArrays(mode, start, count);
    }

    public static void memoryBarrier(int barriers)
    {
        GL42.glMemoryBarrier(barriers);
    }
}