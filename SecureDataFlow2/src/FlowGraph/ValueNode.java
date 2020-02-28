package FlowGraph;

import org.eclipse.jdt.core.dom.IMethodBinding;

public abstract class ValueNode extends Node
{
    public ValueNode(String name, IMethodBinding methodBinding)
    {
        super(name, methodBinding);
    }
}
