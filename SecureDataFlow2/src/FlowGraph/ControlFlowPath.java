package FlowGraph;

import java.util.List;

public class ControlFlowPath extends Edge
{
    public ControlFlowPath(Node src, Node dest, List why) 
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
        return "-->*";
    }
}


