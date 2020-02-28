package FlowGraph;

import java.util.Map;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public class SymbolNode extends ValueNode
{
    private IBinding binding;
    private IMethodBinding methodBinding;
    public SymbolNode(IBinding binding, IMethodBinding methodBinding)
    {
        super(binding.getName(), methodBinding);
        this.binding = binding;
        this.methodBinding = methodBinding;
    }

//    public Node Clone(NodeMap map)
//    { 
//    	SymbolNode clonedNode = new SymbolNode(binding, methodBinding);
//    	for (Map.Entry<IVariableBinding, FieldNode> f : this.fields.entrySet()) {
//    		clonedNode.fields.put(f.getKey(), f.getValue());
//    	}
//    	return clonedNode;
//    }

    public String NodeChar()
    {
        return "Local";
    }
    
    public IBinding getBinding() {
    	return this.binding;
    }
}

