package FlowGraph;

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
			}
		}

		for (ControlFlowEdge cfe : graph.controlFlowEdges) {
			if (cfe.isExternal(context)) {
				externalGraph.controlFlowEdges.add(cfe);
			}
		}

		return externalGraph;
	}
}
