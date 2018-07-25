package FlowGraph;

import java.util.List;

public class ControlFlowEdge extends Edge
{
    public ControlFlowEdge(Node src, Node dest, List why) 
    {
        super(src, dest, why);
    }

    public boolean isInconsistent()
    {
        if (src instanceof LabelNode && dest instanceof LabelNode)
        {
        	System.out.println(src.name == "high"  && dest.name == "low");
            return src.name == "high"  && dest.name == "low";
        }
        return false;
    }

    protected String arrow()
    {
        return "-->";
    }
}


