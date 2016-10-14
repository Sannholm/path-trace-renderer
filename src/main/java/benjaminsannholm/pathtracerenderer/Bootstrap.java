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

import java.util.concurrent.TimeUnit;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL42;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import benjaminsannholm.util.math.MathUtils;
import benjaminsannholm.util.math.Matrix4;
import benjaminsannholm.util.math.Quaternion;
import benjaminsannholm.util.math.Transform;
import benjaminsannholm.util.math.Vector2;
import benjaminsannholm.util.math.Vector3;
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
    
    //private static final int WINDOW_WIDTH = 1280 / 2;
    //private static final int WINDOW_HEIGHT = 720 / 2;
    private static final int WINDOW_WIDTH = 256;
    private static final int WINDOW_HEIGHT = 256;

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
    private double lastFPSTime;
    private int fpsCounter;
    private int fps;
    
    private Texture2D mainFrameBufferTex;

    private Transform cameraTransform;
    private Matrix4 projectionMatrix;
    private Matrix4 viewMatrix;
    
    private int numFrames;
    
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
        mainFrameBufferTex = Texture2D.builder(this.width, this.height)
                .format(Format.RGBA32F)
                .build();
        numFrames = 0;
    }
    
    private void loop()
    {
        prevFrameTime = glfwGetTime();
        while (!glfwWindowShouldClose(window))
        {
            final double delta = glfwGetTime() - prevFrameTime;
            timeElapsed += delta;
            prevFrameTime = glfwGetTime();

            fpsCounter++;
            if (glfwGetTime() - lastFPSTime >= 1)
            {
                fps = fpsCounter;
                fpsCounter = 0;
                lastFPSTime = glfwGetTime();
            }
            
            update();
            render();
            
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
    
    private void update()
    {
        if (timeElapsed % 1 < 0.05)
        {
            shaderManager.clearPrograms();
        }

        System.out.println(fps);
        
        /*final float angle = 20 * (float) Math.sin(Math.toRadians(timeElapsed * 100));
        cameraTransform = Transform.create(
                Vector3.create(0, 0, 50).rotateY(-angle),
                Quaternion.fromAxisAngle(Vector3.Y_AXIS, angle),
                Vector3.ONE);*/
        cameraTransform = Transform.create(
                Vector3.create(0, 0, 30),
                Quaternion.IDENTITY,
                Vector3.ONE);
        
        projectionMatrix = Matrix4.createPerspectiveProjection(0.1F, 1000, 90, (float) mainFrameBufferTex.getWidth() / mainFrameBufferTex.getHeight());
        viewMatrix = cameraTransform.getRot().toMatrix4();
    }
    
    private void render()
    {
        numFrames++;
        
        final Matrix4 invViewProjMatrix = projectionMatrix.multiply(viewMatrix).invert();

        mainFrameBufferTex.bindImage(0, Access.WRITE, Format.RGBA32F);

        final ShaderProgram program1 = shaderManager.getProgram("compute_draw");
        program1.setUniform("framebuffer", 0);
        program1.setUniform("framebufferSize", Vector2.create(mainFrameBufferTex.getWidth(), mainFrameBufferTex.getHeight()));
        program1.setUniform("time", (float) timeElapsed);
        program1.setUniform("invViewProjMatrix", invViewProjMatrix);
        program1.setUniform("camPos", cameraTransform.getPos());
        program1.use();

        final Stopwatch sw = Stopwatch.createStarted();

        final int WORKGROUP_SIZE = 16;
        GLAPI.dispatchCompute(MathUtils.nextPoT(mainFrameBufferTex.getWidth() / WORKGROUP_SIZE),
                MathUtils.nextPoT(mainFrameBufferTex.getHeight() / WORKGROUP_SIZE), 1);
        GLAPI.memoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

        System.out.println("Compute: " + sw.elapsed(TimeUnit.MICROSECONDS) + " us");

        FrameBuffer.unBind();
        GLAPI.setViewport(0, 0, width, height);

        mainFrameBufferTex.bind(0);

        /*final ShaderProgram program2 = shaderManager.getProgram("fullscreen_texture_average");
        program2.setUniform("tex", 0);
        program2.setUniform("divisor", (float) numFrames);
        program2.use();*/
        
        final ShaderProgram program2 = shaderManager.getProgram("fullscreen_texture");
        program2.setUniform("tex", 0);
        program2.use();

        FullscreenQuadRenderer.render();

        Texture2D.unbind(0);
    }
}
