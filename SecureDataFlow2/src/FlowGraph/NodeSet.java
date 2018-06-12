package FlowGraph;

import java.util.*;

/*
public class NodeSet 
{
    private Set<Node> Set = new HashSet<Node>();

    public NodeSet(Object ... list)
    {
        for (Object item : list)
        {
            if (item instanceof Node)
                Add((Node)item);
            else if (item instanceof NodeSet)
                UnionWith((NodeSet)item);
            else if (item instanceof SrcNodes)
            {
                Add(((SrcNodes)item).dataFlow);
                UnionWith(((SrcNodes)item).controlFlow);
            }
            else
                throw new UnsupportedOperationException();
        }
    }

    public NodeSet Substitute(NodeMap map)
    {
        NodeSet new_set = new NodeSet();
        for (Node node : Set)
            new_set.Add(map.apply(node));
        return new_set;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Node node : Set)
        {
            if (builder.length() > 1)
                builder.append(",");
            builder.append(node);
        }
        builder.append("}");
        return builder.toString();
    }

    public void Add(Node node)
    {
        Set.add(node);
    }

    public void UnionWith(NodeSet Set)
    {
        this.Set.addAll(Set.Set);
    }

    public Set<Node> GetEnumerator()
    {
        return Set;
    }
}
*/
