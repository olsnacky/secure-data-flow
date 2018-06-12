package FlowGraph;

import java.util.List;

public class PointsToSameEdge extends Edge
{
    public PointsToSameEdge(Node src, Node dest, List why)
    {
        super(src, dest, why);
    }

    protected String arrow()
    {
        return "==";
    }
}
