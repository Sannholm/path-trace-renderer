package benjaminsannholm.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileResourceLocator implements ResourceLocator
{
    @Override
    public Resource locate(final String path)
    {
        return new Resource(path)
        {
            @Override
            public InputStream openStream() throws IOException
            {
                return Files.newInputStream(Paths.get(path));
            }
        };
    }
}