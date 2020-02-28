package FlowGraph;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public class FieldNode extends ValueNode {
	public Node lhs;
	public IVariableBinding binding;
	public String mapsTo;

	public FieldNode(Node lhs, IVariableBinding binding, IMethodBinding methodBinding) {
		super(binding.getName(), methodBinding);
		this.binding = binding;
		this.lhs = lhs;
	}

	@Override
	public String toString() {
		return lhs + "." + binding.getName();
	}

//    public Node Clone(NodeMap map)
//    {
//        return map.map(lhs).getField(binding, null);
//    }

	@Override
	public String NodeChar() {
		return "Field";
	}
}
