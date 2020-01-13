package FlowGraph;

import java.util.List;

public class DataFlowPath extends Edge
{
    public DataFlowPath(Node src, Node dest, List why)
    {
        super(src, dest, why);
    }

    protected String arrow()
    {
        return "==>*";
    }
}


