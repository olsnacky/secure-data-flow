package FlowGraph;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;

public class ExpressionNode extends ValueNode 
{
    public ExpressionNode(Object name, IMethodBinding methodBinding)
    {
        super(name.toString(), methodBinding);
    }
    
    public ExpressionNode(String name, IMethodBinding methodBinding) {
        super(name, methodBinding);
    }

    public String NodeChar()
    {
        return "Expression";
    }
}