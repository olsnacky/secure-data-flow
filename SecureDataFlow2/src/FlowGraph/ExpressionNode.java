package FlowGraph;

import org.eclipse.jdt.core.dom.Expression;

public class ExpressionNode extends ValueNode 
{
    public ExpressionNode(Object name)
    {
        super(name.toString());
    }

    public String NodeChar()
    {
        return "Expression";
    }
}