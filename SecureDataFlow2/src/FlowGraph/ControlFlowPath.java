package FlowGraph;

import java.util.List;

public class ControlFlowPath extends Edge {
	public final boolean isWellFormed;

	public ControlFlowPath(Node src, Node dest, List why, boolean isWellFormed) {
		super(src, dest, why);
		this.isWellFormed = isWellFormed;
	}

	public boolean violatesNoninterference() {
		return src.isHigh() && dest.isLow();
	}

	@Override
	protected String arrow() {
		return "-->*";
	}
}
