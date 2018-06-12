package FlowGraph;

import java.util.*;
/*
public class SrcNodes
{
    public Node dataFlow;
    public NodeSet controlFlow = new NodeSet();

    @SuppressWarnings("unchecked")
    public SrcNodes(Node dataFlow, Object ... controlFlow)
    {
        this.dataFlow = dataFlow;
        for (Object c : controlFlow)
        {
            if (c instanceof SrcNodes)
            {
                SrcNodes cs = (SrcNodes)c;
                this.controlFlow.Add(cs.dataFlow);
                this.controlFlow.UnionWith(cs.controlFlow);
            }
            else if (c instanceof List<?>)
            {
                for (SrcNodes cs : (List<SrcNodes>)c)
                {
                    this.controlFlow.Add(cs.dataFlow);
                    this.controlFlow.UnionWith(cs.controlFlow);
                }
            }
            else if (c instanceof NodeSet)
                this.controlFlow.UnionWith((NodeSet)c);
            else if (c instanceof Node)
                this.controlFlow.Add((Node)c);
            else
                throw new UnsupportedOperationException();
        }
    }

    public SrcNodes Substitute(NodeMap map)
    {
        return new SrcNodes(map.apply(dataFlow), controlFlow.Substitute(map));
    }

    public String toString()
    {
        return "<" + dataFlow + "," + controlFlow + ">";
    }
}
*/
