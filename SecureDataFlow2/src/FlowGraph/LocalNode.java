package FlowGraph;

import java.util.Map;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public class LocalNode extends ValueNode
{
	private IMethodBinding methodBinding;
	
    public LocalNode(String name, IMethodBinding methodBinding)
    {
        super(name, methodBinding);
        this.methodBinding = methodBinding;
    }

//    public Node Clone(NodeMap map)
//    { 
//    	LocalNode clonedNode = new LocalNode(name, methodBinding);
//    	for (Map.Entry<IVariableBinding, FieldNode> f : this.fields.entrySet()) {
//    		clonedNode.fields.put(f.getKey(), f.getValue());
//    	}
//    	return clonedNode;
//    }

    public String NodeChar()
    {
        return "Local";
    }
}

