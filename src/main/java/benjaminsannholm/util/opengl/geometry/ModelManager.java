package benjaminsannholm.util.opengl.geometry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import benjaminsannholm.util.JsonUtils;
import benjaminsannholm.util.resource.ResourceLocator;
import gnu.trove.map.hash.THashMap;

public class ModelManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelManager.class);
    
    private static final Gson GSON = new Gson();
    private static final String MODEL_INDEX_PATH = "index.json";
    
    private final ResourceLocator modelLocator, meshLocator;
    
    private final Map<String, ModelInfo> modelInfos = new THashMap<>();
    private final Map<String, StaticMeshData> staticMeshes = new THashMap<>();
    
    public ModelManager(ResourceLocator modelLocator, ResourceLocator meshLocator)
    {
        this.modelLocator = Preconditions.checkNotNull(modelLocator, "modelLocator");
        this.meshLocator = Preconditions.checkNotNull(meshLocator, "meshLocator");
    }
    
    public void loadModels()
    {
        try (Reader indexReader = new BufferedReader(new InputStreamReader(modelLocator.locate(MODEL_INDEX_PATH).openStream(), StandardCharsets.UTF_8)))
        {
            for (JsonElement pathElement : new JsonParser().parse(indexReader).getAsJsonArray())
            {
                final String path = JsonUtils.getString(pathElement, "path") + ".json";
                
                try (Reader reader = new BufferedReader(new InputStreamReader(modelLocator.locate(path).openStream(), StandardCharsets.UTF_8)))
                {
                    final ModelInfo modelInfo = GSON.fromJson(reader, ModelInfo.class);
                    modelInfos.put(modelInfo.getId(), modelInfo);
                }
                catch (Exception e)
                {
                    LOGGER.warn("Failed to load model at " + path, e);
                }
            }
            
            LOGGER.info(modelInfos.size() + " models loaded");
        }
        catch (Exception e)
        {
            LOGGER.warn("Failed to load models", e);
        }
    }
    
    public Optional<ModelInfo> getModelInfo(String id)
    {
        return Optional.fromNullable(modelInfos.get(id));
    }
    
    public void clearCaches()
    {
        staticMeshes.clear();
    }
    
    public StaticMeshData getStaticMeshData(String path)
    {
        StaticMeshData mesh = staticMeshes.get(path);
        if (mesh == null)
        {
            try (InputStream stream = meshLocator.locate(path).openStream())
            {
                mesh = new WavefrontLoader().load(stream);
            }
            catch (Exception e)
            {
                LOGGER.warn("Failed to load static mesh at " + path, e);
                mesh = getStaticMeshData("missing.obj");
            }
            
            staticMeshes.put(path, mesh);
        }
        return mesh;
    }
    
    public static enum MeshType
    {
        WAVEFRONT("obj");
        
        private final String extension;
        
        private MeshType(String extension)
        {
            this.extension = extension;
        }
        
        public String getExtension()
        {
            return extension;
        }
        
        private static final Map<String, MeshType> EXTENSION_TO_TYPE = new THashMap<>();
        
        static
        {
            for (MeshType type : values())
                EXTENSION_TO_TYPE.put(type.getExtension(), type);
        }
        
        public static MeshType getTypeByExtension(String extension)
        {
            return EXTENSION_TO_TYPE.get(extension.toLowerCase());
        }
    }
}