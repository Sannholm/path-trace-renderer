package benjaminsannholm.util.opengl.shader;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL41;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.shader.Shader.ShaderCompilationException;
import benjaminsannholm.util.opengl.shader.Shader.Type;
import benjaminsannholm.util.opengl.shader.ShaderProgram.ShaderProgramLinkException;
import benjaminsannholm.util.resource.ResourceLocator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class ShaderManager implements ShaderLoader
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ShaderManager.class);

    private static final String VERSION_HEADER_PREFIX = "#version ";
    private static final boolean PRINT_DEBUG = true;

    private final ResourceLocator shaderLocator;
    private final String versionHeader;

    private final Map<String, ShaderProgram> programs = new THashMap<>();

    public ShaderManager(ResourceLocator shaderLocator, int version, boolean compatibility)
    {
        this.shaderLocator = Preconditions.checkNotNull(shaderLocator, "shaderLocator");
        versionHeader = VERSION_HEADER_PREFIX + version + (compatibility ? " compatibility" : "");
    }

    public void clearPrograms()
    {
        for (ShaderProgram program : programs.values())
            program.dispose();
        programs.clear();
    }

    public ShaderProgram getProgram(String name)
    {
        ShaderProgram program = programs.get(name);
        if (program == null)
        {
            try
            {
                final ShaderProgramConfig config = getProgramConfig(name);

                final Optional<String> vertexSource = getComposedShaderSource(name + "_vert");
                final Optional<String> fragmentSource = getComposedShaderSource(name + "_frag");
                final Optional<String> computeSource = getComposedShaderSource(name + "_comp");

                if (!vertexSource.isPresent() && !fragmentSource.isPresent() && !computeSource.isPresent())
                    throw new IOException("No shader files found");
                
                final Set<Shader> shaders = new THashSet<>();
                try
                {
                    if (vertexSource.isPresent())
                        shaders.add(new Shader(Type.VERTEX, vertexSource.get()));
                    if (fragmentSource.isPresent())
                        shaders.add(new Shader(Type.FRAGMENT, fragmentSource.get()));
                    if (computeSource.isPresent())
                        shaders.add(new Shader(Type.COMPUTE, computeSource.get()));

                    program = new ShaderProgram(shaders, config.vertexInputs, config.fragmentOutputs);
                }
                finally
                {
                    for (Shader shader : shaders)
                        shader.dispose();
                }
                
                if (PRINT_DEBUG)
                {
                    final Path folderPath = Paths.get("shader_program_debug");
                    Files.createDirectories(folderPath);
                    
                    final ByteBuffer binary = memAlloc(GLAPI.getProgrami(program.getHandle(), GL41.GL_PROGRAM_BINARY_LENGTH));
                    try (MemoryStack stack = stackPush())
                    {
                        final IntBuffer length = stack.mallocInt(1);
                        final IntBuffer format = stack.mallocInt(1);

                        program.getBinary(length, format, binary);
                        binary.limit(length.get(0));

                        try (FileChannel channel = FileChannel.open(folderPath.resolve(name + ".bin"), StandardOpenOption.WRITE, StandardOpenOption.CREATE))
                        {
                            channel.write(binary);
                        }
                    }
                    finally
                    {
                        memFree(binary);
                    }

                    if (vertexSource.isPresent())
                        Files.write(folderPath.resolve(name + ".vert"), vertexSource.get().getBytes(StandardCharsets.UTF_8));
                    if (fragmentSource.isPresent())
                        Files.write(folderPath.resolve(name + ".frag"), fragmentSource.get().getBytes(StandardCharsets.UTF_8));
                    if (computeSource.isPresent())
                        Files.write(folderPath.resolve(name + ".comp"), computeSource.get().getBytes(StandardCharsets.UTF_8));
                }
            }
            catch (ShaderCompilationException | ShaderProgramLinkException | IOException e)
            {
                LOGGER.error("Failed to load shader program " + name, e);
                program = new DummyShaderProgram();
            }

            programs.put(name, program);
        }
        return program;
    }

    private Optional<String> getComposedShaderSource(String name) throws IOException
    {
        String baseSource = null;
        try
        {
            baseSource = getShaderSource(name);
        }
        catch (IOException e)
        {
            return Optional.empty();
        }

        return Optional.of(new ShaderComposer(this)
                .append(versionHeader)
                .append(baseSource)
                .compose());
    }

    @Override
    public String getShaderSource(String path) throws IOException
    {
        try (InputStream stream = shaderLocator.locate(path + ".msf").openStream())
        {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        }
    }

    private static final Gson GSON = new Gson();

    private ShaderProgramConfig getProgramConfig(String path)
    {
        try (Reader reader = new BufferedReader(new InputStreamReader(shaderLocator.locate(path + ".json").openStream(), StandardCharsets.UTF_8)))
        {
            return GSON.fromJson(reader, ShaderProgramConfig.class);
        }
        catch (IOException e)
        {
            return new ShaderProgramConfig();
        }
    }

    private static class ShaderProgramConfig
    {
        public List<String> vertexInputs = ImmutableList.of("in_position");
        public List<String> fragmentOutputs = ImmutableList.of("out_color");
    }
}