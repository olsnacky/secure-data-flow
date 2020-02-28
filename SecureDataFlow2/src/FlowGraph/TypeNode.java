package FlowGraph;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class TypeNode extends Node
{
    public TypeNode(ITypeBinding binding)
    {
        super(binding);
    }

    public String NodeChar()
    {
        return "Type";
    }
}
