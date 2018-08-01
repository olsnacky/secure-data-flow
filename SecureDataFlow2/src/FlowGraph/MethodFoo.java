package FlowGraph;

import java.util.Map;

public class MethodFoo
{
    public MethodContext context = new MethodContext();
    public Graph graph = new Graph();
    
	public Graph getExternalGraph() {
		// TODO: create nodes for this.x
		Graph externalGraph = new Graph();
		for (DataFlowEdge dfe : graph.dataFlowEdges) {
			if (dfe.isExternal(context)) {
				externalGraph.dataFlowEdges.add(dfe);
			} else if (dfe.shouldBeExternal(context)) {
				for (Map.Entry<Node, AliasEdge> alias : dfe.dest.aliasEdges.entrySet()) {
					Node aliasedNode = alias.getKey();
					if (aliasedNode.isExternalNode(context)) {
						externalGraph.AddDataFlowEdge(dfe.src, aliasedNode);
					}
				}
			}
		}

		for (ControlFlowEdge cfe : graph.controlFlowEdges) {
			if (cfe.isExternal(context)) {
				externalGraph.controlFlowEdges.add(cfe);
			} else if (cfe.shouldBeExternal(context)) {
				for (Map.Entry<Node, AliasEdge> alias : cfe.dest.aliasEdges.entrySet()) {
					Node aliasedNode = alias.getKey();
					if (aliasedNode.isExternalNode(context)) {
						externalGraph.AddControlFlowEdge(cfe.src, aliasedNode);
					}
				}
			}
		}

		return externalGraph;
	}
}
