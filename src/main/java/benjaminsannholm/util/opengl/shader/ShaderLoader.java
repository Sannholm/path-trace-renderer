package benjaminsannholm.util.opengl.shader;

import java.io.IOException;

public interface ShaderLoader
{
    String getShaderSource(String path) throws IOException;
}