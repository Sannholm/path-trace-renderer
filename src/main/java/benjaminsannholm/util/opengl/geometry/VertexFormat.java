package benjaminsannholm.util.opengl.geometry;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import benjaminsannholm.util.opengl.GLAPIEnum;

public class VertexFormat
{
    private final List<VertexAttribute> attributes;
    
    private final int bytesPerVertex;
    private final int[] offsets;
    
    private VertexFormat(List<VertexAttribute> attributes)
    {
        this.attributes = ImmutableList.copyOf(Preconditions.checkNotNull(attributes, "attributes"));
        Preconditions.checkArgument(!this.attributes.isEmpty(), "Attributes cannot be empty");
        
        offsets = new int[this.attributes.size()];
        
        int bytes = 0;
        for (int i = 0; i < this.attributes.size(); i++)
        {
            offsets[i] = bytes;
            bytes += this.attributes.get(i).getSize();
        }
        
        bytesPerVertex = bytes;
    }
    
    public List<VertexAttribute> getAttributes()
    {
        return attributes;
    }
    
    public int getBytesPerVertex()
    {
        return bytesPerVertex;
    }
    
    public int getOffset(int index)
    {
        return offsets[index];
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static class Builder
    {
        private final List<VertexAttribute> attributes = new ArrayList<>();
        
        private Builder()
        {
        }
        
        public Builder attribute(DataType dataType, int count, boolean normalize)
        {
            attributes.add(new VertexAttribute(dataType, count, normalize));
            return this;
        }
        
        public VertexFormat build()
        {
            return new VertexFormat(attributes);
        }
    }
    
    public static class VertexAttribute
    {
        private final DataType dataType;
        private final int count;
        private final boolean normalize;
        
        private final int size;
        
        private VertexAttribute(DataType dataType, int count, boolean normalize)
        {
            this.dataType = Preconditions.checkNotNull(dataType, "dataType");
            Preconditions.checkArgument(count > 0, "Count cannot be <= 0");
            Preconditions.checkArgument(count <= 4, "Count cannot be > 4");
            this.count = count;
            this.normalize = normalize;
            
            size = dataType.getSize() * count;
            Preconditions.checkArgument(size >= 4, "Attribute size cannot be < 4 bytes");
        }
        
        public DataType getDataType()
        {
            return dataType;
        }
        
        public int getCount()
        {
            return count;
        }
        
        public int getSize()
        {
            return size;
        }
        
        public boolean shouldNormalize()
        {
            return normalize;
        }
    }
    
    public static enum DataType implements GLAPIEnum
    {
        BYTE(Byte.BYTES, GL11.GL_BYTE),
        SHORT(Short.BYTES, GL11.GL_SHORT),
        INT(Integer.BYTES, GL11.GL_INT),
        UBYTE(Byte.BYTES, GL11.GL_UNSIGNED_BYTE),
        USHORT(Short.BYTES, GL11.GL_UNSIGNED_SHORT),
        UINT(Integer.BYTES, GL11.GL_UNSIGNED_INT),
        FLOAT(Float.BYTES, GL11.GL_FLOAT);
        
        private final int size;
        private final int glEnum;
        
        private DataType(int size, int glEnum)
        {
            Preconditions.checkArgument(size > 0, "Size cannot be <= 0");
            this.size = size;
            this.glEnum = glEnum;
        }
        
        public int getSize()
        {
            return size;
        }
        
        @Override
        public int getEnum()
        {
            return glEnum;
        }
    }
}