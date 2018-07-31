package FlowGraph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public /* partial */ class Graph implements NodeMap {
	boolean changed;
	boolean inconsistent;

	// high and low node singletons
	private Node high = new ExpressionNode("HIGH");
	private Node low = new ExpressionNode("LOW");

	// Edges
	public List<MethodCall> methods = new ArrayList<MethodCall>();
	public List<ControlFlowEdge> controlFlowEdges = new ArrayList<ControlFlowEdge>();
	public List<DataFlowEdge> dataFlowEdges = new ArrayList<DataFlowEdge>();
	public List<PointsToSameEdge> pointsToSameEdges = new ArrayList<PointsToSameEdge>();
	public List<AliasEdge> aliasEdges = new ArrayList<AliasEdge>();

	public void AddControlFlowEdge(Node src, Node dest) {
		AddControlFlowEdge(new ControlFlowEdge(src, dest, null));
	}

	public void AddDataFlowEdge(Node src, Node dest) {
		AddDataFlowEdge(new DataFlowEdge(src, dest, null));
	}

	public void AddMethodCallNode(ASTNode callpoint, IMethodBinding method, Node recv, List<Node> args, Node tr,
			SubGraph subgraph) {
		MethodCall node = new MethodCall(callpoint, recv, method, args, tr);
		node.subgraph = subgraph;
		methods.add(node);
	}

	private void AddControlFlowEdge(ControlFlowEdge edge) {
		if (!edge.src.IsControlFlowTo(edge.dest)) {
			controlFlowEdges.add(edge);
			edge.src.AddControlFlowSrc(edge);

			if (edge.isInconsistent())
				inconsistent = true;
			changed = true;
		}
	}

	private void AddDataFlowEdge(DataFlowEdge edge) {
		if (!edge.src.IsDataFlowTo(edge.dest)) {
			dataFlowEdges.add(edge);
			edge.src.AddDataFlowSrc(edge);

			changed = true;
		}
	}

	private void AddPointsToSameEdge(PointsToSameEdge edge) {
		if (!edge.src.PointsToSame(edge.dest)) {
			pointsToSameEdges.add(edge);
			edge.src.AddPointsToSameSrc(edge);
			edge.dest.AddPointsToSameDest(edge);
			changed = true;

			// TestProgram.WriteLine(edge);
		}
	}

	private void AddAliasEdge(AliasEdge edge) {
		if (!edge.src.IsAlias(edge.dest)) {
			aliasEdges.add(edge);
			edge.src.AddAliasSrc(edge);
			edge.dest.AddAliasDest(edge);
			changed = true;

			// TestProgram.WriteLine(edge);
		}
	}

	/*
	 * public void AddDataFlowEdge(Node src, Node dest) { AddDataFlowEdge(new
	 * DataFlowEdge(src, dest, null)); }
	 * 
	 * public void AddDataFlowEdges(SrcNodes src, SrcNodes dest) { // done
	 * AddDataFlowEdges(src, dest.dataFlow); }
	 * 
	 * public void AddDataFlowEdges(SrcNodes src, Node dest) { // done
	 * AddDataFlowEdge(new DataFlowEdge(src.dataFlow, dest, null));
	 * AddControlFlowEdges(src.controlFlow, dest); }
	 * 
	 * public void AddControlFlowEdge(Node src, Node dest) { AddControlFlowEdge(new
	 * ControlFlowEdge(src, dest, null)); }
	 * 
	 * public void AddControlFlowEdges(SrcNodes src, Node dest) { // done
	 * AddControlFlowEdge(new ControlFlowEdge(src.dataFlow, dest, null));
	 * AddControlFlowEdges(src.controlFlow, dest); }
	 * 
	 * public void AddControlFlowEdges(NodeSet src, Node dest) { for (Node s :
	 * src.GetEnumerator()) AddControlFlowEdge(new ControlFlowEdge(s, dest, null));
	 * }
	 * 
	 * public void AddControlFlowEdges(Node src, SrcNodes dest) { for (Node child :
	 * dest.controlFlow.GetEnumerator()) AddControlFlowEdge(src, child); }
	 * 
	 * public void AddControlFlowEdges(Node src, NodeSet dest) { for (Node child :
	 * dest.GetEnumerator()) AddControlFlowEdge(src, child); }
	 * 
	 * public void AddControlFlowEdges(Node src, org.eclipse.jdt.core.dom.Statement
	 * dest) { for (Node child :
	 * securedataflow.handlers.DataFlowVisitor.statements.get(dest).GetEnumerator())
	 * AddControlFlowEdge(src, child); }
	 * 
	 * public void AddMethodCallNode(MethodInvocation callpoint, IMethodBinding
	 * method, Node recv, List<Node> args, Node tr, SubGraph subgraph) { MethodCall
	 * node = new MethodCall(callpoint, recv, method, args, tr); node.subgraph =
	 * subgraph; methods.add(node); changed = true;
	 * 
	 * //TestProgram.WriteLine(node); }
	 */
	List<Node> nodes = new ArrayList<Node>();

	private void Append(Node node) {
		if (!nodes.contains(node))
			nodes.add(node);
	}

	public void Dotty(String name) {
		System.out.println("digraph " + name + " {");

		nodes = new ArrayList<Node>();

		for (DataFlowEdge edge : dataFlowEdges) {
			Append(edge.src);
			Append(edge.dest);
			System.out.println(edge.dotty() + "[style=solid];");
		}
		for (ControlFlowEdge edge : controlFlowEdges) {
			Append(edge.src);
			Append(edge.dest);
			System.out.println(edge.dotty() + "[style=dashed];");
		}
		for (PointsToSameEdge edge : pointsToSameEdges) {
			Append(edge.src);
			Append(edge.dest);
			System.out.println(edge.dotty() + "[style=dotted];");
		}
		for (AliasEdge edge : aliasEdges) {
			Append(edge.src);
			Append(edge.dest);
			System.out.println(edge.dotty() + "[style=tapered];");
		}

		for (Node node : nodes)
			System.out.println("\t" + node.dotty() + "[label=\"" + node.toString() + "\"];");

		for (MethodCall method : methods) {
			// don't print this out cos buggy and not needed?
			// System.out.println("\t" + method.dotty() + ";");
		}

		System.out.println("}");
	}

	public void Print() {
		for (DataFlowEdge edge : dataFlowEdges)
			System.out.println(edge);

		for (ControlFlowEdge edge : controlFlowEdges)
			System.out.println(edge);

		for (PointsToSameEdge edge : pointsToSameEdges)
			System.out.println(edge);

		for (AliasEdge edge : aliasEdges)
			System.out.println(edge);

		for (MethodCall method : methods)
			System.out.println(method);

		System.out.println("  -----------------------------------------------------");
	}

	public void Closure() {
		inconsistent = false;

		while (!inconsistent && changed) {
			changed = false;

			// Compute points to same
			for (DataFlowEdge edge1 : dataFlowEdges)
				if (edge1.src instanceof ValueNode) {
					AddPointsToSameEdge(new PointsToSameEdge(edge1.src, edge1.dest, Arrays.asList(edge1)));
					for (DataFlowEdge edge2 : edge1.src.dataFlowEdges.values())
						AddPointsToSameEdge(new PointsToSameEdge(edge1.dest, edge2.dest, Arrays.asList(edge1, edge2)));
				}

			// Data flow implies Control flow
			for (DataFlowEdge edge : dataFlowEdges)
				AddControlFlowEdge(new ControlFlowEdge(edge.src, edge.dest, Arrays.asList(edge)));

			// Compute aliases
			for (PointsToSameEdge edge : pointsToSameEdges)
				for (Map.Entry<IVariableBinding, FieldNode> f : edge.src.fields.entrySet()) {
					IVariableBinding field_name = f.getKey();
					if (edge.dest.fields.containsKey(field_name))
						AddAliasEdge(
								new AliasEdge(f.getValue(), edge.dest.fields.get(field_name), Arrays.asList(edge)));
				}

			// Data flow transitivity without aliasing
			for (DataFlowEdge edge1 : new ArrayList<DataFlowEdge>(dataFlowEdges))
				if (edge1.dest instanceof ValueNode)
					for (DataFlowEdge edge2 : edge1.dest.dataFlowEdges.values())
						AddDataFlowEdge(new DataFlowEdge(edge1.src, edge2.dest, Arrays.asList(edge1, edge2)));

			// Data flow transitivity with aliasing
			for (DataFlowEdge edge1 : new ArrayList<DataFlowEdge>(dataFlowEdges))
				if (edge1.dest instanceof ValueNode)
					for (Map.Entry<Node, AliasEdge> item2 : edge1.dest.aliasEdges.entrySet()) {
						Node edge2_dest = item2.getKey();
						AliasEdge edge2 = item2.getValue();
						for (DataFlowEdge edge3 : edge2_dest.dataFlowEdges.values())
							AddDataFlowEdge(
									new DataFlowEdge(edge1.src, edge3.dest, Arrays.asList(edge1, edge2, edge3)));
					}

			// Control flow transitivity without aliasing
			for (ControlFlowEdge edge1 : new ArrayList<ControlFlowEdge>(controlFlowEdges))
				if (edge1.dest instanceof ValueNode)
					for (ControlFlowEdge edge2 : edge1.dest.controlFlowEdges.values())
						AddControlFlowEdge(new ControlFlowEdge(edge1.src, edge2.dest, Arrays.asList(edge1, edge2)));

			// Control flow transitivity with aliasing
			for (ControlFlowEdge edge1 : new ArrayList<ControlFlowEdge>(controlFlowEdges))
				if (edge1.dest instanceof ValueNode)
					for (Map.Entry<Node, AliasEdge> item2 : edge1.dest.aliasEdges.entrySet()) {
						Node edge2_dest = item2.getKey();
						AliasEdge edge2 = item2.getValue();
						for (ControlFlowEdge edge3 : edge2_dest.controlFlowEdges.values())
							AddControlFlowEdge(
									new ControlFlowEdge(edge1.src, edge3.dest, Arrays.asList(edge1, edge2, edge3)));
					}

			// Method closure
			for (MethodCall method : new ArrayList<MethodCall>(methods))
				if (method.recv == null) // constructor
				{
					InlineMethodCall(method);
				} else {
					for (DataFlowEdge typeflow : new ArrayList<DataFlowEdge>(dataFlowEdges)) {
						if (typeflow.src instanceof TypeNode && typeflow.dest == method.recv) {
							if (QUT.DataflowVisitor.hasMethod(method.method)) {
								InlineMethodCall(method, (TypeNode) typeflow.src);
							}
						}
					}
				}
		}

		if (inconsistent)
			System.out.println("        Inconsistent\n");
		else
			System.out.println("        Consistent\n");

		for (ControlFlowEdge edge : controlFlowEdges)
			if (edge.isInconsistent())
				edge.Explain(0);
	}

	private void InlineMethodCall(MethodCall method_invocation, TypeNode C) {
		if (method_invocation.already_expanded.contains(C))
			return;

		// TestProgram.WriteLine("Expand {0}", method_invocation.method);

		method_invocation.already_expanded.add(C);
		// Fixme - your TypeNode C
		MethodFoo method_body = QUT.DataflowVisitor.getMethod(method_invocation.method);

		SubGraph cloned = GetContour(method_body, method_invocation);
		MethodContext cloned_method = cloned.context;

		for (DataFlowEdge edge : cloned.method.graph.dataFlowEdges)
			AddDataFlowEdge(edge.src, edge.dest);

		for (ControlFlowEdge edge : cloned.method.graph.controlFlowEdges)
			AddControlFlowEdge(edge.src, edge.dest);

		AddDataFlowEdge(method_invocation.recv, cloned_method.recv);

		for (int i = 0; i < cloned_method.args.size(); i++)
			AddDataFlowEdge(method_invocation.args.get(i), cloned_method.args.get(i));

		AddDataFlowEdge(cloned_method.return_value, method_invocation.return_value);
		if (method_invocation.method.getName().equals("readHigh")) {
			AddDataFlowEdge(high, method_invocation.return_value);
		} else if (method_invocation.method.getName().equals("readLow")) {
			AddDataFlowEdge(low, method_invocation.return_value);
		} else if (method_invocation.method.getName().equals("writeLow")) {
			for (Node arg : new ArrayList<Node>(method_invocation.args)) {
				AddDataFlowEdge(arg, low);
			}
		} else if (method_invocation.method.getName().equals("writeHigh")) {
			for (Node arg : new ArrayList<Node>(method_invocation.args)) {
				AddDataFlowEdge(arg, high);
			}
		}

		// AddControlFlowEdges(method_invocation.pc, cloned_method.sp); // Fixme:
		// propagate control flow from method call to method body
	}

	private void InlineMethodCall(MethodCall method_invocation) {
		if (method_invocation.already_expanded.contains(null))
			return;

		// TestProgram.WriteLine("Expand {0}", method_invocation.method);

		method_invocation.already_expanded.add(null);

		// Fixme - implicit constructor
		if (QUT.DataflowVisitor.hasMethod(method_invocation.method)) {
			MethodFoo method_body = QUT.DataflowVisitor.getMethod(method_invocation.method);

			MethodContext cloned_method = GetContour(method_body, method_invocation).context;

			for (int i = 0; i < cloned_method.args.size(); i++)
				AddDataFlowEdge(method_invocation.args.get(i), cloned_method.args.get(i));

			AddDataFlowEdge(cloned_method.return_value, method_invocation.return_value);

			// AddControlFlowEdges(method_invocation.pc, cloned_method.sp); // Fixme
		}
	}

	private SubGraph GetContour(MethodFoo method_body, MethodCall method_invocation) {
		if (method_invocation.subgraph != null) {
			SubGraph existing = method_invocation.subgraph.GetContour(method_body, method_invocation);

			if (existing != null)
				return existing;
		}
		return CloneMethodBody(method_body, method_invocation);
	}

	public SubGraph CloneMethodBody(MethodFoo method_body, MethodCall method_invocation) {
		SubGraph clone = new SubGraph(method_body, method_invocation.callpoint, method_invocation.subgraph);

		node_mapping.clear();

		clone.context.recv = map(method_body.context.recv);
		// clone.context.sp = map(method_body.context.sp);
		clone.context.return_value = map(method_body.context.return_value);

		for (int i = 0; i < method_body.context.args.size(); i++)
			clone.context.args.add(map(method_body.context.args.get(i)));

		for (DataFlowEdge edge : method_body.graph.dataFlowEdges)
			AddDataFlowEdge(map(edge.src), map(edge.dest));

		for (ControlFlowEdge edge : method_body.graph.controlFlowEdges)
			AddControlFlowEdge(map(edge.src), map(edge.dest));

		for (MethodCall method : method_body.graph.methods)
			AddMethodCallNode(method.callpoint, method.method, method.recv == null ? null : map(method.recv),
					Substitute(method.args), map(method.return_value), clone);

		return clone;
	}

	private Map<Node, Node> node_mapping = new HashMap<Node, Node>();

	public Node map(Node n) {
		if (node_mapping.containsKey(n))
			return node_mapping.get(n);
		else {
			Node clone = n.Clone(this);
			node_mapping.put(n, clone);
			return clone;
		}
	}

	private List<Node> Substitute(List<Node> list) {
		List<Node> new_list = new ArrayList<Node>();
		for (Node arg : list)
			new_list.add(map(arg));
		return new_list;
	}
}