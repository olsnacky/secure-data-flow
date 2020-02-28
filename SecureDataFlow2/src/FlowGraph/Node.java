package FlowGraph;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public abstract class Node implements java.util.Comparator<Node> {
	public static int NEXTID = 0;
	public String name;
	public int id;

	public Map<IVariableBinding, FieldNode> fields = new Hashtable<IVariableBinding, FieldNode>();

	public Map<Graph, Map<Node, DataFlowEdge>> dataFlowEdges = new Hashtable<Graph, Map<Node, DataFlowEdge>>();
	public Map<Graph, Map<Node, DataFlowPath>> dataFlowPaths = new Hashtable<Graph, Map<Node, DataFlowPath>>();
	public Map<Graph, Map<Node, ControlFlowEdge>> controlFlowEdges = new Hashtable<Graph, Map<Node, ControlFlowEdge>>();
	public Map<Graph, Map<Node, ControlFlowPath>> controlFlowPaths = new Hashtable<Graph, Map<Node, ControlFlowPath>>();
	public Map<Graph, Map<Node, PointsToSameEdge>> pointsToEdges = new Hashtable<Graph, Map<Node, PointsToSameEdge>>();
	public Map<Graph, Map<Node, AliasEdge>> aliasEdges = new Hashtable<Graph, Map<Node, AliasEdge>>();

	private IMethodBinding methodBinding;

//	public Node(IBinding binding, IMethodBinding methodBinding) {
//		this(binding.getName(), methodBinding);
//	}

	public Node(IBinding binding) {
		this(binding.getName(), null);
	}

	public Node(String name, IMethodBinding methodBinding) {
		this.id = NEXTID++;
		this.name = name;
		this.methodBinding = methodBinding;
		/* this.subgraph = null; */
	}

	public Node Clone(NodeMap map) {
		return this;
	}

//	public FieldNode getField(IVariableBinding name, Map<String, String> fieldMappings) {
//		if (!fields.containsKey(name)) {
//			FieldNode node = new FieldNode(this, name);
//			if (fieldMappings != null && fieldMappings.containsKey(node.name)) {
//				node.mapsTo = fieldMappings.get(node.name);
//			}
//			fields.put(name, node);
//			return node;
//		} else
//			return fields.get(name);
//	}

	public void AddDataFlowSrc(Graph graph, DataFlowEdge edge) {
		if (!dataFlowEdges.containsKey(graph)) {
			dataFlowEdges.put(graph, new Hashtable<Node, DataFlowEdge>());
		}

		dataFlowEdges.get(graph).put(edge.dest, edge);
	}

	public void AddDataFlowPathSrc(Graph graph, DataFlowPath path) {
		if (!dataFlowPaths.containsKey(graph)) {
			dataFlowPaths.put(graph, new Hashtable<Node, DataFlowPath>());
		}

		dataFlowPaths.get(graph).put(path.dest, path);
	}

	public void AddControlFlowSrc(Graph graph, ControlFlowEdge edge) {
		if (!controlFlowEdges.containsKey(graph)) {
			controlFlowEdges.put(graph, new Hashtable<Node, ControlFlowEdge>());
		}

		controlFlowEdges.get(graph).put(edge.dest, edge);
	}

	public void AddControlFlowPathSrc(Graph graph, ControlFlowPath path) {
		if (!controlFlowPaths.containsKey(graph)) {
			controlFlowPaths.put(graph, new Hashtable<Node, ControlFlowPath>());
		}

		controlFlowPaths.get(graph).put(path.dest, path);
	}

	public void AddPointsToSameSrc(Graph graph, PointsToSameEdge edge) {
		if (!pointsToEdges.containsKey(graph)) {
			pointsToEdges.put(graph, new Hashtable<Node, PointsToSameEdge>());
		}

		pointsToEdges.get(graph).put(edge.dest, edge);
	}

	public void AddPointsToSameDest(Graph graph, PointsToSameEdge edge) {
		if (!pointsToEdges.containsKey(graph)) {
			pointsToEdges.put(graph, new Hashtable<Node, PointsToSameEdge>());
		}

		pointsToEdges.get(graph).put(edge.src, edge);
	}

	public void AddAliasSrc(Graph graph, AliasEdge edge) {
		if (!aliasEdges.containsKey(graph)) {
			aliasEdges.put(graph, new Hashtable<Node, AliasEdge>());
		}

		aliasEdges.get(graph).put(edge.dest, edge);
	}

	public void AddAliasDest(Graph graph, AliasEdge edge) {
		if (!aliasEdges.containsKey(graph)) {
			aliasEdges.put(graph, new Hashtable<Node, AliasEdge>());
		}

		aliasEdges.get(graph).put(edge.src, edge);
	}

	public boolean IsDataFlowTo(Graph graph, Node dest) {
		Map<Node, DataFlowEdge> edges = dataFlowEdges.get(graph);

		if (edges != null) {
			return edges.containsKey(dest);
		} else {
			return false;
		}
	}

	public boolean IsDataFlowPathTo(Graph graph, Node dest) {
		Map<Node, DataFlowPath> paths = dataFlowPaths.get(graph);

		if (paths != null) {
			return paths.containsKey(dest);
		} else {
			return false;
		}
	}

	public boolean IsAlias(Graph graph, Node dest) {
		Map<Node, AliasEdge> edges = aliasEdges.get(graph);

		if (edges != null) {
			return edges.containsKey(dest);
		} else {
			return false;
		}
	}

	public boolean IsControlFlowTo(Graph graph, Node dest) {
		Map<Node, ControlFlowEdge> edges = controlFlowEdges.get(graph);

		if (edges != null) {
			return edges.containsKey(dest);
		} else {
			return false;
		}
	}

	public boolean IsControlFlowPathTo(Graph graph, Node dest) {
		Map<Node, ControlFlowPath> paths = controlFlowPaths.get(graph);

		if (paths != null) {
			return paths.containsKey(dest);
		} else {
			return false;
		}
	}

	public boolean isHigh() {
		return this.name.equals("HIGH");
	}

	public boolean isExternalNode(MethodContext context) {
		// CONFIRMED
		return isHigh() || isLow() || context.args.contains(this) || context.return_value.equals(this)
				|| context.recv.equals(this) || context.entry.equals(this)
				|| (this.isField() && ((FieldNode) this).lhs.isExternalNode(context))
				|| (this.isArrayAccess() && ((ArrayAccessNode) this).lhs.isExternalNode(context));
	}

	public boolean isLow() {
		return this.name.equals("LOW");
	}

	public boolean isThis() {
		return this.name.equals("this");
	}

	public boolean isArrayAccess() {
		return this instanceof ArrayAccessNode;
	}

	public boolean isField() {
		return this instanceof FieldNode;
	}

	boolean PointsToSame(Graph graph, Node other) {
		Map<Node, PointsToSameEdge> edges = pointsToEdges.get(graph);

		if (edges != null) {
			return edges.containsKey(other);
		} else {
			return false;
		}
	}

	abstract public String NodeChar();

	public String dotty() {
		return String.format("n%04x", id);
	}

	public String contextAwareToString(MethodContext methodContext) {
		return (this.isExternalNode(methodContext) ? "KN - " : "") + this.toString();
	}

	@Override
	public String toString() {
		return getMethodNameText() + ":" + NodeChar() + ":" + this.escapedName();
	}

	public String escapedName() {
		return name.replace("\"", "").replace("[", "").replace("]", "").replace("\\", "");
	}

	public int CompareTo(Node o) {
		return this.id - o.id;
	}

	@Override
	public int compare(Node o1, Node o2) {
		return o1.id - o2.id;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Node) && ((Node) o).id == this.id;
	}

	public boolean mapsTo(Node contractNode, MethodContext impContext, MethodContext conContext) {
		if (this.isHigh()) {
			return contractNode.isHigh(); // CONFIRMED
		} else if (this.isLow()) {
			return contractNode.isLow(); // CONFIRMED
		} else if (impContext.args.contains(this) && conContext.args.contains(contractNode)) {
			return impContext.args.indexOf(this) == conContext.args.indexOf(contractNode); // CONFIRMED
		} else if (impContext.return_value.equals(this)) {
			return conContext.return_value.equals(contractNode); // CONFIRMED
		} else if (impContext.entry.equals(this)) {
			return conContext.entry.equals(contractNode); // CONFIRMED
		} else if (this.isThis()) {
			return contractNode.isThis(); // CONFIRMED
		} else if (this.isField()) {
			return (contractNode.isField() && contractNode.name.equals(this.name)) || (contractNode.isField()
					&& (((FieldNode) this).mapsTo != null && ((FieldNode) this).mapsTo.equals(contractNode.name))); // CONFIRMED
		} else if (this.isArrayAccess()) {
			if (contractNode.isArrayAccess()) {
				ArrayAccessNode me = (ArrayAccessNode) this;
				ArrayAccessNode contractArrayAccessNode = (ArrayAccessNode) contractNode;
				return me.lhs != null && contractArrayAccessNode.lhs != null
						&& me.lhs.mapsTo(contractArrayAccessNode.lhs, impContext, conContext); // CONFIRMED
			}

			return false;
		}

		return false;
	}

	private String getMethodNameText() {
		return this.methodBinding != null
				? this.methodBinding.getDeclaringClass().getName() + "." + this.methodBinding.getName()
				: "";
	}
}
