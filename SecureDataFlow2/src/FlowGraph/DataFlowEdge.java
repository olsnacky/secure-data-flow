package FlowGraph;

import java.util.List;

public class DataFlowEdge extends Edge
{
    public DataFlowEdge(Node src, Node dest, List why)
    {
        super(src, dest, why);
    }

    protected String arrow()
    {
        return "==>";
    }
}


