package benjaminsannholm.util.opengl.texture;

import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyexr.EXRHeader;
import org.lwjgl.util.tinyexr.EXRImage;
import org.lwjgl.util.tinyexr.EXRVersion;
import org.lwjgl.util.tinyexr.TinyEXR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.math.IntMath;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import benjaminsannholm.util.opengl.texture.Texture.Builder;
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

    public Texture getTexture(String path)
    {
        Texture texture = textures.get(path);
        if (texture == null)
        {
            try
            {
                final BufferedImage rawImage = loadImage(path);
                final int width = rawImage.getWidth();
                final int height = rawImage.getHeight();
                
                final TextureConfig config = getTextureConfig(FilenameUtils.removeExtension(path));
                final Texture.Builder<?, ?> builder = setupTextureBuilder(width, height, config);
                
                ByteBuffer buffer = null;
                try
                {
                    if (rawImage.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_FLOAT) // XXX: Experimental
                    {
                        texture = builder.format(Format.RGB16F).build();

                        final float[] data = rawImage.getRaster().getPixels(0, 0, width, height, (float[])null);
                        
                        buffer = MemoryUtil.memAlloc(data.length * 4);
                        buffer.asFloatBuffer().put(data);
                        buffer.flip();
                        
                        texture.upload(buffer, GL11.GL_RGB, GL11.GL_FLOAT);
                    }
                    else
                    {
                        texture = builder.format(config.sRGB ? Format.SRGB8_ALPHA8 : Format.RGBA8).build();
                        
                        final BufferedImage formattedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        final Graphics2D g = formattedImage.createGraphics();
                        g.drawImage(rawImage, 0, 0, null);

                        final int[] data = ((DataBufferInt)formattedImage.getRaster().getDataBuffer()).getData();
                        
                        buffer = MemoryUtil.memAlloc(data.length * 4);
                        buffer.asIntBuffer().put(data);
                        buffer.flip();
                        
                        texture.upload(buffer, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV);
                    }
                }
                finally
                {
                    MemoryUtil.memFree(buffer);
                }
                
                if (config.mipmap)
                    texture.generateMipmaps();
            }
            catch (IOException e)
            {
                if (texture != null)
                    texture.dispose();

                LOGGER.error("Failed to load texture " + path, e);
                texture = getTexture("missing.png");
            }

            textures.put(path, texture);
        }
        return texture;
    }
    
    private Builder<?, ?> setupTextureBuilder(int width, int height, TextureConfig config)
    {
        Texture.Builder<?, ?> builder;
        
        switch (config.type)
        {
            case "2d":
                builder = Texture2D.builder(width, height);
                if (config.mipmap)
                    builder.levels(IntMath.log2(Math.max(width, height), RoundingMode.FLOOR) + 1);
                break;
            case "3d":
                builder = Texture3D.builder(width, height, height / width);
                if (config.mipmap)
                    builder.levels(IntMath.log2(Math.max(width, Math.max(height, height / width)), RoundingMode.FLOOR) + 1);
                break;
            default:
                throw new IllegalArgumentException("Unsupported texture type: " + config.type);
        }

        builder.magFilter(config.magBlur ? MagnificationFilter.LINEAR : MagnificationFilter.NEAREST)
                .minFilter(config.mipmap ? (config.minBlur ? MinificationFilter.LINEAR_MIPMAP : MinificationFilter.NEAREST_MIPMAP) : (config.minBlur ? MinificationFilter.LINEAR : MinificationFilter.NEAREST));
        
        return builder;
    }
    
    private BufferedImage loadImage(String path) throws IOException
    {
        try (InputStream stream = new BufferedInputStream(textureLocator.locate(path).openStream()))
        {
            final BufferedImage image = ImageIO.read(stream);
            if (image != null)
                return image;
        }
        
        if (FilenameUtils.getExtension(path).equalsIgnoreCase("exr"))
        {
            final byte[] fileArray;
            try (InputStream stream = textureLocator.locate(path).openStream())
            {
                fileArray = IOUtils.toByteArray(stream);
            }
            
            final ByteBuffer fileBuffer = MemoryUtil.memAlloc(fileArray.length);
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                fileBuffer.put(fileArray);
                fileBuffer.flip();
                
                final EXRVersion version = EXRVersion.mallocStack(stack);
                int ret = TinyEXR.ParseEXRVersionFromMemory(version, fileBuffer);
                if (ret != TinyEXR.TINYEXR_SUCCESS)
                    throw new IOException("Could not load EXR: " + ret);
                
                try (EXRHeader header = EXRHeader.malloc())
                {
                    try
                    {
                        TinyEXR.InitEXRHeader(header);
                        ret = TinyEXR.ParseEXRHeaderFromMemory(header, version, fileBuffer, stack.pointers(NULL));
                        if (ret != TinyEXR.TINYEXR_SUCCESS)
                            throw new IOException("Could not load EXR: " + ret);

                        final EXRImage image = EXRImage.mallocStack(stack);
                        try
                        {
                            TinyEXR.InitEXRImage(image);
                            ret = TinyEXR.LoadEXRImageFromMemory(image, header, fileBuffer, stack.pointers(NULL));
                            if (ret != TinyEXR.TINYEXR_SUCCESS)
                                throw new IOException("Could not load EXR: " + ret);

                            System.out.println(header.tiled());
                            System.out.println(image.num_tiles());
                            System.out.println(image.width());
                            System.out.println(image.height());

                            return new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                        }
                        finally
                        {
                            TinyEXR.FreeEXRImage(image);
                        }
                    }
                    finally
                    {
                        TinyEXR.FreeEXRHeader(header);
                    }
                }
            }
            finally
            {
                MemoryUtil.memFree(fileBuffer);
            }
        }
        
        throw new IOException("Unknown image format");
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