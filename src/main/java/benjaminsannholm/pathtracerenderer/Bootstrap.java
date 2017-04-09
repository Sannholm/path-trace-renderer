package benjaminsannholm.pathtracerenderer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_T;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_COMPAT_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
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
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
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

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL42;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benjaminsannholm.util.math.FastMath;
import benjaminsannholm.util.math.Matrix4;
import benjaminsannholm.util.math.Quaternion;
import benjaminsannholm.util.math.Transform;
import benjaminsannholm.util.math.Vector3;
import benjaminsannholm.util.math.Vector4;
import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.debug.Query;
import benjaminsannholm.util.opengl.debug.Query.Type;
import benjaminsannholm.util.opengl.geometry.FullscreenQuadRenderer;
import benjaminsannholm.util.opengl.shader.ShaderManager;
import benjaminsannholm.util.opengl.shader.ShaderProgram;
import benjaminsannholm.util.opengl.texture.FrameBuffer;
import benjaminsannholm.util.opengl.texture.Texture.Access;
import benjaminsannholm.util.opengl.texture.Texture.Format;
import benjaminsannholm.util.opengl.texture.Texture2D;
import benjaminsannholm.util.opengl.texture.TextureManager;
import benjaminsannholm.util.resource.ClasspathResourceLocator;
import benjaminsannholm.util.resource.FallbackResourceLocator;
import benjaminsannholm.util.resource.FileResourceLocator;
import benjaminsannholm.util.resource.PrefixedResourceLocator;
import benjaminsannholm.util.resource.ResourceLocator;

public class Bootstrap
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private static final Random RAND = ThreadLocalRandom.current();
    
    private static final int WINDOW_WIDTH = 256;
    private static final int WINDOW_HEIGHT = 256;
    
    private static final int PASSES_PER_FRAME = 100;

    private static final ResourceLocator BASE_RESOURCE_LOCATOR = new PrefixedResourceLocator(
            new ClasspathResourceLocator(), "/");

    private final TextureManager textureManager = new TextureManager(
            new PrefixedResourceLocator(BASE_RESOURCE_LOCATOR, "textures/"));

    private final ShaderManager shaderManager = new ShaderManager(new FallbackResourceLocator(
            new PrefixedResourceLocator(new FileResourceLocator(), "shaders/"),
            new PrefixedResourceLocator(BASE_RESOURCE_LOCATOR, "shaders/")), 430, false);
    
    private long window;
    private int width;
    private int height;
    
    private double prevLoopTime;
    private double timeElapsed;
    
    private double lastFPSTime;
    private int fpsCounter;
    private int fps;
    
    private double lastFrameTime;
    
    private int numPasses;
    private float sceneTime;
    
    private Texture2D mainFrameBufferTex;

    private Transform cameraTransform;
    private Matrix4 projectionMatrix;
    private Matrix4 viewMatrix;
    private Vector4 cam00, cam10, cam01, cam11;

    private Query qSetup, qCompute, qBarrier, qComposite;
    private long totalFrameTime;
    
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
        setupGLWindow();
        //GLAPI.setFramebufferSRGB(true);
        
        qSetup = new Query(Type.TIME_ELAPSED);
        qCompute = new Query(Type.TIME_ELAPSED);
        qBarrier = new Query(Type.TIME_ELAPSED);
        qComposite = new Query(Type.TIME_ELAPSED);

        setupScene(sceneTime);
    }

    private void setupGLWindow()
    {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        glfwWindowHint(GLFW_SRGB_CAPABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Renderer", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        final GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - WINDOW_WIDTH) / 2, (vidmode.height() - WINDOW_HEIGHT) / 2);

        glfwMakeContextCurrent(window);
        GL.createCapabilities(true);
        GLAPI.setupDebugPrinting();
        glfwSwapInterval(1);

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) ->
        {
            if (action == GLFW_RELEASE)
            {
                switch (key)
                {
                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(window, true);
                        break;
                    case GLFW_KEY_R:
                        resetRender();
                        shaderManager.clearPrograms();
                        break;
                    case GLFW_KEY_T:
                        resetRender();
                        break;
                    default:
                        break;
                }
            }
        });

        glfwSetFramebufferSizeCallback(window, this::onResize);

        glfwShowWindow(window);
    }
    
    private void onResize(long window, int width, int height)
    {
        if ((width != this.width || height != this.height)
                && width != 0 && height != 0)
        {
            this.width = width;
            this.height = height;

            if (mainFrameBufferTex != null)
                mainFrameBufferTex.dispose();
            mainFrameBufferTex = null;

            cameraTransform = null;
            resetRender();
        }
    }
    
    private void resetRender()
    {
        numPasses = 0;
        totalFrameTime = 0;

        setupScene(sceneTime);
    }

    private void loop()
    {
        prevLoopTime = glfwGetTime();
        while (!glfwWindowShouldClose(window))
        {
            final double delta = glfwGetTime() - prevLoopTime;
            timeElapsed += delta;
            prevLoopTime = glfwGetTime();

            fpsCounter++;
            if (glfwGetTime() - lastFPSTime >= 1)
            {
                fps = fpsCounter;
                fpsCounter = 0;
                lastFPSTime = glfwGetTime();
            }

            if (timeElapsed % 1 < 0.05)
            {
                System.out.println(fps);
            }

            /*if (numPasses == PASSES_PER_FRAME)
            {
                sceneTime += 1 / 30F;
                //sceneTime += glfwGetTime() - lastFrameTime;
                lastFrameTime = glfwGetTime();

                resetRender();
            }*/

            render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void setupScene(double time)
    {
        updateCameraTransform(time);
    }

    private void updateCameraTransform(double time)
    {
        final float angle = 20 * (float)Math.sin(Math.toRadians(time * 100));
        setCameraTransform(Transform.create(
                Vector3.create(0, 0, 40).rotateY(angle),
                Quaternion.fromAxisAngle(Vector3.Y_AXIS, -angle),
                Vector3.ONE));
        /*setCameraTransform(Transform.create(
                Vector3.create(0, 0, 50),
                Quaternion.IDENTITY,
                Vector3.ONE));*/
    }

    private void setCameraTransform(Transform transform)
    {
        if (cameraTransform == null || !cameraTransform.equals(transform))
        {
            cameraTransform = transform;
            updateCamera();
        }
    }
    
    private void updateCamera()
    {
        projectionMatrix = Matrix4.createPerspectiveProjection(0.1F, 1000, 110, (float)width / height);
        viewMatrix = cameraTransform.getRot().toMatrix4();
        final Matrix4 invViewProjMatrix = projectionMatrix.multiply(viewMatrix).invert();

        cam00 = Vector4.create(-1, -1, 0, 1).multiply(invViewProjMatrix);
        cam00 = cam00.divide(cam00.getW());
        cam10 = Vector4.create(1, -1, 0, 1).multiply(invViewProjMatrix);
        cam10 = cam10.divide(cam10.getW());
        cam01 = Vector4.create(-1, 1, 0, 1).multiply(invViewProjMatrix);
        cam01 = cam01.divide(cam01.getW());
        cam11 = Vector4.create(1, 1, 0, 1).multiply(invViewProjMatrix);
        cam11 = cam11.divide(cam11.getW());
    }
    
    private void render()
    {
        if (mainFrameBufferTex == null)
        {
            mainFrameBufferTex = Texture2D.builder(width, height)
                    .format(Format.RGBA32F)
                    .build();
        }

        if (numPasses < PASSES_PER_FRAME)
        {
            qSetup.begin();

            mainFrameBufferTex.bindImage(0, Access.READ_WRITE, Format.RGBA32F);
            textureManager.getTexture("environment/table_mountain_2_1k.exr").bind(0);

            final ShaderProgram program1 = shaderManager.getProgram("compute_draw");
            program1.setUniform("framebuffer", 0);
            program1.setUniform("numPasses", numPasses + 1);
            program1.setUniform("randInit", Vector4.create(RAND.nextFloat(), RAND.nextFloat(), RAND.nextFloat(), RAND.nextFloat()));
            program1.setUniform("time", (float)timeElapsed);
            program1.setUniform("cam00", cam00);
            program1.setUniform("cam10", cam10);
            program1.setUniform("cam01", cam01);
            program1.setUniform("cam11", cam11);
            program1.setUniform("camPos", cameraTransform.getPos());
            program1.setUniform("environmentTex", 0);
            //program1.setUniform("stratifiedGridIndices", generateStratifiedGridIndices());
            program1.use();

            final int numGroupsX = FastMath.fastCeil((float)mainFrameBufferTex.getWidth() / program1.getWorkgroupSizeX());
            final int numGroupsY = FastMath.fastCeil((float)mainFrameBufferTex.getHeight() / program1.getWorkgroupSizeY());

            qSetup.end();

            qCompute.begin();
            program1.dispatchCompute(numGroupsX, numGroupsY, 1);
            qCompute.end();

            qBarrier.begin();
            GLAPI.memoryBarrier(GL42.GL_TEXTURE_FETCH_BARRIER_BIT | GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
            qBarrier.end();
        }
        
        if (numPasses < PASSES_PER_FRAME)
        {
            qComposite.begin();

            FrameBuffer.unBind();
            GLAPI.setViewport(0, 0, width, height);

            mainFrameBufferTex.bind(0);

            final ShaderProgram program2 = shaderManager.getProgram("post_composite");
            program2.setUniform("tex", 0);
            program2.setUniform("exposure", 1F);
            //program2.setUniform("exposure", (FastMath.sin((float)timeElapsed * 2) * 0.5F + 0.5F) * 100);
            program2.use();

            FullscreenQuadRenderer.render();

            qComposite.end();
        }

        if (numPasses < PASSES_PER_FRAME)
        {
            final long tSetup = qSetup.getResult();
            final long tCompute = qCompute.getResult();
            final long tBarrier = qBarrier.getResult();
            final long tComposite = qComposite.getResult();
            final long frameTime = tSetup + tCompute + tBarrier + tComposite;
            totalFrameTime += frameTime;
            System.out.println("Frame (" + width + "x" + height + "): " + String.format(Locale.ENGLISH, "%.2f", frameTime / 1e6) + " (" + String.format(Locale.ENGLISH, "%.2f", totalFrameTime / 1e6 / (numPasses + 1)) + ") ms (#" + (numPasses + 1) + ")"
                    + "\n    Setup:\t" + tSetup / 1e6 + " ms"
                    + "\n    Compute:\t" + tCompute / 1e6 + " ms"
                    + "\n    Barrier:\t" + tBarrier / 1e6 + " ms"
                    + "\n  Composite:\t" + tComposite / 1e6 + " ms");
        }
        
        if (numPasses < PASSES_PER_FRAME)
            numPasses++;
    }

    /*private static final int MAX_STRATIFIED_DIMENSIONS = 5;
    private static final int SAMPLES_PER_PIXEL = 100;
    private final int[] stratifiedGridIndices = new int[MAX_STRATIFIED_DIMENSIONS * SAMPLES_PER_PIXEL];

    private int[] generateStratifiedGridIndices()
    {
        for (int i = 0; i < MAX_STRATIFIED_DIMENSIONS; i++)
        {
            final int[] indices = new int[SAMPLES_PER_PIXEL];

            for (int j = 0; j < SAMPLES_PER_PIXEL; j++)
                indices[j] = j;
    
            for (int k = SAMPLES_PER_PIXEL; k > 1; k--)
            {
                final int rand = RAND.nextInt(k);
                final int tmp = indices[rand];
                indices[rand] = indices[k - 1];
                indices[k - 1] = tmp;
            }

            System.arraycopy(indices, 0, stratifiedGridIndices, i * SAMPLES_PER_PIXEL, SAMPLES_PER_PIXEL);
        }

        return stratifiedGridIndices;
    }*/
}
