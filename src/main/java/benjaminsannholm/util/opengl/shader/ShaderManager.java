package benjaminsannholm.util.opengl.shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

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