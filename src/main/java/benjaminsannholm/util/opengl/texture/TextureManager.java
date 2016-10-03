package benjaminsannholm.util.opengl.texture;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.texture.Texture.Format;
import benjaminsannholm.util.opengl.texture.Texture.MagnificationFilter;
import benjaminsannholm.util.opengl.texture.Texture.MinificationFilter;
import benjaminsannholm.util.resource.ResourceLocator;
import gnu.trove.map.hash.THashMap;

public class TextureManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TextureManager.class);

    private final ResourceLocator textureLocator;

    private final Map<String, Texture> textures = new THashMap<>();

    private ByteBuffer dataBuffer;

    public TextureManager(ResourceLocator textureLocator)
    {
        this.textureLocator = Preconditions.checkNotNull(textureLocator, "textureLocator");
    }

    public void clearTextures()
    {
        for (Texture texture : textures.values())
            texture.dispose();
        textures.clear();
    }

    private ByteBuffer getDataBuffer(int size)
    {
        if (dataBuffer == null || size > dataBuffer.capacity())
            dataBuffer = BufferUtils.createByteBuffer(size);

        dataBuffer.clear();
        return dataBuffer;
    }

    public Texture getTexture(String path)
    {
        Texture texture = textures.get(path);
        if (texture == null)
        {
            try
            {
                BufferedImage rawImage = loadImage(path);
                final int width = rawImage.getWidth();
                final int height = rawImage.getHeight();
                BufferedImage formattedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                final Graphics2D g = formattedImage.createGraphics();
                g.drawImage(rawImage, 0, 0, null);
                g.dispose();
                rawImage.flush();
                rawImage = null;
                
                final int[] data = ((DataBufferInt) formattedImage.getRaster().getDataBuffer()).getData();
                formattedImage.flush();
                formattedImage = null;

                final ByteBuffer buffer = getDataBuffer(data.length * 4);
                buffer.asIntBuffer().put(data);
                buffer.flip();

                final TextureConfig config = getTextureConfig(FilenameUtils.removeExtension(path));
                
                Texture.Builder<?, ?> builder = null;
                switch (config.type)
                {
                case "2d":
                    builder = Texture2D.builder(width, height);
                    break;
                case "3d":
                    builder = Texture3D.builder(width, height, height / width);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported texture type: " + config.type);
                }
                
                builder.format(config.sRGB ? Format.SRGB8_ALPHA8 : Format.RGBA8)
                        .magFilter(config.magBlur ? MagnificationFilter.LINEAR : MagnificationFilter.NEAREST)
                        .minFilter(config.mipmap ? (config.minBlur ? MinificationFilter.LINEAR_MIPMAP : MinificationFilter.NEAREST_MIPMAP) : (config.minBlur ? MinificationFilter.LINEAR : MinificationFilter.NEAREST));

                texture = builder.build();
                texture.bind(0);
                texture.upload(buffer, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV);

                if (config.mipmap)
                    GLAPI.generateMipmaps(texture.getType().getTarget());
            }
            catch (IOException e)
            {
                LOGGER.error("Failed to load texture " + path, e);
                texture = getTexture("missing.png");
            }
            
            textures.put(path, texture);
        }
        return texture;
    }
    
    private BufferedImage loadImage(String path) throws IOException
    {
        try (InputStream stream = new BufferedInputStream(textureLocator.locate(path).openStream()))
        {
            return ImageIO.read(stream);
        }
    }

    private static final Gson GSON = new Gson();

    private TextureConfig getTextureConfig(String path)
    {
        try (Reader reader = new BufferedReader(new InputStreamReader(textureLocator.locate(path + ".json").openStream(), StandardCharsets.UTF_8)))
        {
            return GSON.fromJson(reader, TextureConfig.class);
        }
        catch (IOException e)
        {
            return new TextureConfig();
        }
    }

    private static class TextureConfig
    {
        @SerializedName("type")
        public String type = "2d";
        @SerializedName("sRGB")
        public boolean sRGB = true;
        @SerializedName("mipmap")
        public boolean mipmap = true;
        @SerializedName("magBlur")
        public boolean magBlur = true;
        @SerializedName("minBlur")
        public boolean minBlur = true;
        @SerializedName("clamp")
        public boolean clamp = false;

        @Override
        public String toString()
        {
            return MoreObjects.toStringHelper(this)
                    .add("type", type)
                    .add("sRGB", sRGB)
                    .add("mipmap", mipmap)
                    .add("magBlur", magBlur)
                    .add("minBlur", minBlur)
                    .add("clamp", clamp)
                    .toString();
        }
    }
}