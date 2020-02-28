package FlowGraph;

import java.util.Objects;

public class BFSearchEntry {
	Node node;
	boolean canBeAliasEdge;

	public BFSearchEntry(Node node, boolean isAliasEdge) {
		this.node = node;
		this.canBeAliasEdge = isAliasEdge;
	}
	
	public int hashCode() {
		return Objects.hash(this.node.id, this.canBeAliasEdge);
	}
	
	public boolean equals(Object o) {
		return (o instanceof BFSearchEntry) && (((BFSearchEntry) o).node).equals(this.node)
				&& ((BFSearchEntry) o).canBeAliasEdge == this.canBeAliasEdge;
	}
}
