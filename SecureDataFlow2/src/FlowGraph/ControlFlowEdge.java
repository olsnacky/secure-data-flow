package FlowGraph;

import java.util.List;

public class ControlFlowEdge extends Edge
{
    public ControlFlowEdge(Node src, Node dest, List why) 
    {
        super(src, dest, why);
    }

    protected String arrow()
    {
        return "-->";
    }
}


