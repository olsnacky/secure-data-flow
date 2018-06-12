package FlowGraph;

public class LocalNode extends ValueNode
{
    public LocalNode(String name)
    {
        super(name);
    }

    public Node Clone(NodeMap map)
    { 
        return new LocalNode(name);
    }

    public String NodeChar()
    {
        return "Local";
    }
}

