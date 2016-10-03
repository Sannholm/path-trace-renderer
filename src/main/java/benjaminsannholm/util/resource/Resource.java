package benjaminsannholm.util.resource;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public abstract class Resource
{
    private final String path;

    public Resource(String path)
    {
        this.path = Preconditions.checkNotNull(path, "path");
    }

    public String getPath()
    {
        return path;
    }

    public abstract InputStream openStream() throws IOException;

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .addValue(path)
                .toString();
    }
}