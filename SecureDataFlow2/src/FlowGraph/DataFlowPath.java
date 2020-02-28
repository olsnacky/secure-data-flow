package FlowGraph;

import java.util.List;

public class DataFlowPath extends Edge {
	public final boolean isWellFormed;

	public DataFlowPath(Node src, Node dest, List why, boolean isWellFormed) {
		super(src, dest, why);
		this.isWellFormed = isWellFormed;
	}

	@Override
	protected String arrow() {
		return "==>*";
	}
}
