package FlowGraph;

import java.util.Map;

import org.eclipse.jdt.core.dom.IMethodBinding;

public class MethodFoo {
	public MethodContext context = new MethodContext();
	public Graph graph;
	
	private final IMethodBinding methodBinding;
	
	public MethodFoo(IMethodBinding methodBinding) {
		this.methodBinding = methodBinding;
		graph = new Graph(methodBinding);
	}

	public Graph getVerificationGraph(boolean isImplementation) {
//		for (Node node : context.args) {
//			addKnownNodeAndFields(node);
//		}
//
//		addKnownNodeAndFields(this.context.recv);
//		addKnownNodeAndFields(this.context.return_value);

		this.graph.Closure();

		if (isImplementation) {
			Graph externalGraph = new Graph(this.methodBinding);
			for (DataFlowPath dataFlowPath : graph.dataFlowPaths) {
				if (dataFlowPath.isExternal(context)) {
					externalGraph.dataFlowPaths.add(dataFlowPath);
				}
//			} else if (dataFlowPath.shouldBeExternal(context)) {
//				for (Map.Entry<Node, AliasEdge> alias : dataFlowPath.dest.aliasEdges.entrySet()) {
//					Node aliasedNode = alias.getKey();
//					if (aliasedNode.isExternalNode(context)) {
//						externalGraph.AddDataFlowEdge(dataFlowPath.src, aliasedNode);
//					}
//				}
//			}
			}

			for (ControlFlowPath controlFlowPath : graph.controlFlowPaths) {
				if (controlFlowPath.isExternal(context)) {
					externalGraph.controlFlowPaths.add(controlFlowPath);
				}
//			} else if (cfe.shouldBeExternal(context)) {
//				for (Map.Entry<Node, AliasEdge> alias : cfe.dest.aliasEdges.entrySet()) {
//					Node aliasedNode = alias.getKey();
//					if (aliasedNode.isExternalNode(context)) {
//						externalGraph.AddControlFlowEdge(cfe.src, aliasedNode);
//					}
//				}
//			}
			}
			
			for (PointsToSameEdge edge : graph.pointsToSameEdges) {
				if (edge.isExternal(context)) {
					externalGraph.pointsToSameEdges.add(edge);
				}
			}

			return externalGraph;
		}
		
		return this.graph;
	}
	
	public String getFullName() {
		return methodBinding.getDeclaringClass().getQualifiedName() + "." + methodBinding.getName();
	}

//	private void addKnownNodeAndFields(Node node) {
//		this.graph.AddAliasIdentityEdge(node);
//		for (Node field : node.fields.values()) {
//			addKnownNodeAndFields(field);
//		}
//	}
}
