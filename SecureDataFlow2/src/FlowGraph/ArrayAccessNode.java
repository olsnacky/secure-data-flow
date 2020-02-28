package FlowGraph;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;

public class ArrayAccessNode extends ExpressionNode 
{
	public Node lhs;
	
    public ArrayAccessNode(Node lhs, Object name, IMethodBinding methodBinding)
    {
    	super(name.toString(), methodBinding);
    	this.lhs = lhs;
    }
    
    public ArrayAccessNode(Node lhs, String name, IMethodBinding methodBinding) {
    	super(name, methodBinding);
    	this.lhs = lhs;
    }

    public String NodeChar()
    {
        return "ArrayAccess";
    }
}