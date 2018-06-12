package FlowGraph;

import org.eclipse.jdt.core.dom.IVariableBinding;


public class FieldNode extends ValueNode
{
    public Node lhs;
    IVariableBinding binding;

    public FieldNode(Node lhs, IVariableBinding binding)
    {
        super(binding.getName());
        this.binding = binding;
        this.lhs = lhs;
    }

    public String toString()
    {
        return lhs + "." + binding.getName();
    }

    public Node Clone(NodeMap map)
    {
        return map.map(lhs).getField(binding);
    }

    public String NodeChar()
    {
        return "Field";
    }
}

