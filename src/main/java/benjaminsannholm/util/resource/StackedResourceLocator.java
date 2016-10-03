package benjaminsannholm.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class StackedResourceLocator implements ResourceLocator
{
    private final List<ResourceLocator> stack;
    
    public StackedResourceLocator(ResourceLocator... stack)
    {
        Preconditions.checkArgument(stack.length >= 2, "Stack cannot contain < 2 resource locators");
        this.stack = ImmutableList.copyOf(Preconditions.checkNotNull(stack, "stack"));
    }
    
    @Override
    public Resource locate(final String path)
    {
        return new Resource(path)
        {
            @Override
            public InputStream openStream() throws IOException
            {
                InputStream stream = null;
                
                for (int i = 0; i < stack.size(); i++)
                {
                    try
                    {
                        stream = stack.get(i).locate(path).openStream();
                        break;
                    }
                    catch (Exception e)
                    {
                        // Only throw exceptions from the locator at
                        // the bottom of the stack, otherwise use next fallback
                        if (i >= stack.size() - 1)
                            throw e;
                    }
                }
                
                return stream;
            }
        };
    }
}