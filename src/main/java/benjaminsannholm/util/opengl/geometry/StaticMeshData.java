package benjaminsannholm.util.opengl.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import gnu.trove.map.hash.THashMap;

public class StaticMeshData
{
    private final Map<String, StaticMeshSectionData> sections = new THashMap<>();
    
    public Collection<StaticMeshSectionData> getSections()
    {
        return Collections.unmodifiableCollection(sections.values());
    }
    
    public Optional<StaticMeshSectionData> getSection(String name)
    {
        return Optional.fromNullable(sections.get(name));
    }
    
    public void addSection(StaticMeshSectionData section)
    {
        Preconditions.checkNotNull(section, "section");
        sections.put(section.getName(), section);
    }
    
    public void removeEmptySections()
    {
        for (Iterator<StaticMeshSectionData> it = sections.values().iterator(); it.hasNext();)
            if (it.next().getVertices().size() < 3)
                it.remove();
    }

    public static class StaticMeshSectionData
    {
        private final String name;
        private final List<Vertex> vertices = new ArrayList<>();
        
        public StaticMeshSectionData(String name)
        {
            this.name = Preconditions.checkNotNull(name, "name");
        }
        
        public String getName()
        {
            return name;
        }
        
        public List<Vertex> getVertices()
        {
            return Collections.unmodifiableList(vertices);
        }
        
        public void addVertex(Vertex vertex)
        {
            vertices.add(vertex);
        }
    }
}