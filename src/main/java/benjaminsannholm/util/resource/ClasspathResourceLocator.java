package benjaminsannholm.util.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ClasspathResourceLocator implements ResourceLocator
{
    @Override
    public Resource locate(final String path)
    {
        return new Resource(path)
        {
            @Override
            public InputStream openStream() throws IOException
            {
                final InputStream stream = getClass().getResourceAsStream(path);
                if (stream == null)
                    throw new FileNotFoundException(path);
                return stream;
            }
        };
    }
}