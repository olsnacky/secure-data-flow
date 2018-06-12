package FlowGraph;

import org.eclipse.jdt.core.dom.IBinding;

public class SymbolNode extends ValueNode
{
    private IBinding binding;
    public SymbolNode(IBinding binding)
    {
        super(binding.getName());
        this.binding = binding;
    }

    public Node Clone(NodeMap map)
    { 
        return new SymbolNode(binding);
    }

    public String NodeChar()
    {
        return "Local";
    }
}

