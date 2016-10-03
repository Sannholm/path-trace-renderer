package benjaminsannholm.util.opengl.geometry;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;

public class ModelInfo
{
    @SerializedName("id")
    private String id;
    @SerializedName("lods")
    private List<LODInfo> lods = new ArrayList<>();
    
    public String getId()
    {
        return id;
    }

    public List<LODInfo> getLods()
    {
        return lods;
    }
    
    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("lods", lods)
                .toString();
    }
    
    public static class LODInfo
    {
        @SerializedName("level")
        private int level;
        @SerializedName("mesh")
        private String mesh;

        public int getLevel()
        {
            return level;
        }
        
        public String getMesh()
        {
            return mesh;
        }
        
        @Override
        public String toString()
        {
            return MoreObjects.toStringHelper(this)
                    .add("level", getLevel())
                    .add("mesh", getMesh())
                    .toString();
        }
    }
}