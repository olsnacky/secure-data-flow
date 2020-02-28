package FlowGraph;

import org.eclipse.jdt.core.dom.IMethodBinding;

public class LabelNode extends Node
{
    public LabelNode(String name, IMethodBinding methodBinding)
    {
        super(name, methodBinding);
    }

    public String NodeChar()
    {
        return "Label";
    }
}
