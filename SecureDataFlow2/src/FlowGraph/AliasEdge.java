package FlowGraph;

import java.util.List;

public class AliasEdge extends Edge
{
    public AliasEdge(Node src, Node dest, List why)
    {
        super(src, dest, why);
    }

    @Override
    protected String arrow()
    {
        return "===";
    }
}
