package benjaminsannholm.util.opengl.geometry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import benjaminsannholm.util.math.Vector2;
import benjaminsannholm.util.math.Vector3;
import benjaminsannholm.util.opengl.geometry.StaticMeshData.StaticMeshSectionData;

public class WavefrontLoader
{
    public StaticMeshData load(InputStream stream) throws IOException
    {
        final List<Vector3> vertexPositions = new ArrayList<>();
        final List<Vector3> vertexNormals = new ArrayList<>();
        final List<Vector2> vertexUVs = new ArrayList<>();

        final StaticMeshData mesh = new StaticMeshData();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
        {
            StaticMeshSectionData currentSection = null;

            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("vn"))
                {
                    vertexNormals.add(parseNormal(line.substring(2).trim()));
                }
                else if (line.startsWith("vt"))
                {
                    vertexUVs.add(parseUV(line.substring(2).trim()));
                }
                else if (line.startsWith("v"))
                {
                    vertexPositions.add(parsePosition(line.substring(1).trim()));
                }
                else if (line.startsWith("f"))
                {
                    if (currentSection == null)
                        mesh.addSection(currentSection = new StaticMeshSectionData("Default"));
                    
                    parseFace(currentSection, vertexPositions, vertexNormals, vertexUVs, line.substring(1).trim());
                }
                else if (line.startsWith("g") || line.startsWith("o"))
                {
                    mesh.addSection(currentSection = new StaticMeshSectionData(line.substring(1).trim()));
                }
            }
        }
        
        mesh.removeEmptySections();

        return mesh;
    }

    private Vector3 parsePosition(String string)
    {
        final String[] split = string.split(" ");
        final float x = Float.parseFloat(split[0]);
        final float y = Float.parseFloat(split[1]);
        final float z = Float.parseFloat(split[2]);

        return Vector3.create(x, y, z);
    }

    private Vector3 parseNormal(String string)
    {
        final String[] split = string.split(" ");
        final float x = Float.parseFloat(split[0]);
        final float y = Float.parseFloat(split[1]);
        final float z = Float.parseFloat(split[2]);

        return Vector3.create(x, y, z);
    }

    private Vector2 parseUV(String string)
    {
        final String[] split = string.split(" ");
        final float x = Float.parseFloat(split[0]);
        final float y = Float.parseFloat(split[1]);

        return Vector2.create(x, 1 - y);
    }

    private void parseFace(StaticMeshSectionData section, List<Vector3> positions, List<Vector3> normals, List<Vector2> uvs, String string)
    {
        final String[] split = string.split(" ");
        final Vertex[] vertices = new Vertex[split.length];

        for (int i = 0; i < split.length; i++)
        {
            final String element = split[i];
            final String[] indexSplit = element.split("/");

            Vector3 pos;
            Vector3 normal = Vector3.ZERO;
            Vector2 uv = Vector2.ZERO;

            pos = positions.get(Integer.parseInt(indexSplit[0]) - 1);
            if (indexSplit.length >= 2 && !indexSplit[1].isEmpty())
                uv = uvs.get(Integer.parseInt(indexSplit[1]) - 1);
            if (indexSplit.length >= 3)
                normal = normals.get(Integer.parseInt(indexSplit[2]) - 1);
            
            vertices[i] = new Vertex(pos, normal, uv);
        }
        
        final boolean usesQuads = split.length == 4;
        if (usesQuads)
        {
            section.addVertex(vertices[0]);
            section.addVertex(vertices[1]);
            section.addVertex(vertices[3]);

            section.addVertex(vertices[1]);
            section.addVertex(vertices[2]);
            section.addVertex(vertices[3]);
        }
        else
        {
            for (Vertex vertex : vertices)
                section.addVertex(vertex);
        }
    }
}