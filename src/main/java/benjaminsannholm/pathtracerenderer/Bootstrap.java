package benjaminsannholm.pathtracerenderer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SRGB_CAPABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL42;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benjaminsannholm.util.math.Vector2;
import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.geometry.FullscreenQuadRenderer;
import benjaminsannholm.util.opengl.shader.ShaderManager;
import benjaminsannholm.util.opengl.shader.ShaderProgram;
import benjaminsannholm.util.opengl.texture.FrameBuffer;
import benjaminsannholm.util.opengl.texture.Texture.Access;
import benjaminsannholm.util.opengl.texture.Texture.Format;
import benjaminsannholm.util.opengl.texture.Texture2D;
import benjaminsannholm.util.opengl.texture.TextureManager;
import benjaminsannholm.util.resource.ClasspathResourceLocator;
import benjaminsannholm.util.resource.FileResourceLocator;
import benjaminsannholm.util.resource.PrefixedResourceLocator;
import benjaminsannholm.util.resource.ResourceLocator;
import benjaminsannholm.util.resource.StackedResourceLocator;

public class Bootstrap
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private static final int WINDOW_WIDTH = 1280 / 2;
    private static final int WINDOW_HEIGHT = 720 / 2;
    
    private static final ResourceLocator BASE_RESOURCE_LOCATOR = new PrefixedResourceLocator(
            new ClasspathResourceLocator(),
            "/");
    
    private final TextureManager textureManager = new TextureManager(
            new PrefixedResourceLocator(BASE_RESOURCE_LOCATOR, "textures/"));
    
    private final ShaderManager shaderManager = new ShaderManager(new StackedResourceLocator(
            new PrefixedResourceLocator(new FileResourceLocator(), "shaders/"),
            new PrefixedResourceLocator(BASE_RESOURCE_LOCATOR, "shaders/")), 430, false);

    private long window;
    private int width;
    private int height;

    private double prevFrameTime;
    private double timeElapsed;

    private Texture2D mainFrameBufferTex;

    public void run()
    {
        try
        {
            init();
            loop();

            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        }
        finally
        {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void init()
    {
        setupWindow();
        GLAPI.setFramebufferSRGB(true);
    }
    
    private void setupWindow()
    {
        GLFWErrorCallback.createPrint(System.err).set();
        
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_SRGB_CAPABLE, GLFW_TRUE);

        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Renderer", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        
        final GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - WINDOW_WIDTH) / 2, (vidmode.height() - WINDOW_HEIGHT) / 2);
        
        glfwMakeContextCurrent(window);
        GL.createCapabilities(true);
        glfwSwapInterval(1);
        
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) ->
        {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });
        
        GLFW.glfwSetFramebufferSizeCallback(window, this::onResize);

        glfwShowWindow(window);
    }

    private void onResize(long window, int width, int height)
    {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        
        if (mainFrameBufferTex != null)
            mainFrameBufferTex.dispose();
        mainFrameBufferTex = Texture2D.builder(this.width, this.height).build();
    }

    private void loop()
    {
        prevFrameTime = glfwGetTime();
        while (!glfwWindowShouldClose(window))
        {
            final double delta = glfwGetTime() - prevFrameTime;
            timeElapsed += delta;
            prevFrameTime = glfwGetTime();
            
            if (timeElapsed % 5 > 0.00001)
            {
                shaderManager.clearPrograms();
            }

            render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void render()
    {
        mainFrameBufferTex.bindImage(0, Access.WRITE, Format.RGBA8);

        final ShaderProgram program1 = shaderManager.getProgram("compute_draw");
        program1.setUniform("framebuffer", 0);
        program1.setUniform("framebufferSize", Vector2.create(mainFrameBufferTex.getWidth(), mainFrameBufferTex.getHeight()));
        program1.setUniform("time", (float) timeElapsed);
        program1.use();

        GLAPI.dispatchCompute(mainFrameBufferTex.getWidth(), mainFrameBufferTex.getHeight(), 1);
        GLAPI.memoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

        FrameBuffer.unBind();
        GLAPI.clearColor(0, 0, 0, 1);
        GLAPI.setViewport(0, 0, width, height);

        mainFrameBufferTex.bind(0);

        final ShaderProgram program2 = shaderManager.getProgram("fullscreen_texture");
        program2.setUniform("texture", 0);
        program2.use();

        FullscreenQuadRenderer.render();

        Texture2D.unbind(0);
    }
}
