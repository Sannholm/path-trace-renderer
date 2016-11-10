package benjaminsannholm.util.resource;

import com.google.common.base.Preconditions;

public class PrefixedResourceLocator implements ResourceLocator
{
    private final ResourceLocator delegate;
    private final String prefix;

    public PrefixedResourceLocator(ResourceLocator delegate, String prefix)
    {
        this.delegate = Preconditions.checkNotNull(delegate, "delegate");
        this.prefix = Preconditions.checkNotNull(prefix, "prefix");
    }

    @Override
    public Resource locate(String path)
    {
        return delegate.locate(prefix + path);
    }
}