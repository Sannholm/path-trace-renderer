package benjaminsannholm.util.opengl.shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

public class ShaderComposer
{
    private final ShaderLoader loader;
    
    private final List<String> sources = new ArrayList<>();
    
    public ShaderComposer(ShaderLoader loader)
    {
        this.loader = Preconditions.checkNotNull(loader, "loader");
    }
    
    public ShaderComposer append(String string)
    {
        sources.add(string);
        return this;
    }
    
    public String compose() throws IOException
    {
        final StringBuilder stringBuilder = new StringBuilder(1000);

        for (String source : sources)
        {
            try (BufferedReader reader = new BufferedReader(new StringReader(source)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (!line.isEmpty() && !line.startsWith("//"))
                    {
                        if (line.startsWith("#include "))
                        {
                            final String path = line.substring(9);
                            stringBuilder.append(new ShaderComposer(loader).append(loader.getShaderSource(path)).compose()).append('\n');
                        }
                        else
                        {
                            stringBuilder.append(line).append('\n');
                        }
                    }
                }
            }
        }

        return stringBuilder.toString();
    }
}