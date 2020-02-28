package FlowGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public /* partial */ class Graph implements NodeMap {
	boolean changed;
	boolean inconsistent;

	// high and low node singletons
	private Node high = new ExpressionNode("HIGH", null);
	private Node low = new ExpressionNode("LOW", null);

	private IMethodBinding methodBinding;

	// arrays
	public Map<Node, Set<Node>> arrayAccesses = new HashMap<Node, Set<Node>>();

	// Edges
	public List<MethodCall> methods = new ArrayList<MethodCall>();
	public List<ControlFlowEdge> controlFlowEdges = new ArrayList<ControlFlowEdge>();
	public List<ControlFlowPath> controlFlowPaths = new ArrayList<ControlFlowPath>();
	public List<DataFlowEdge> dataFlowEdges = new ArrayList<DataFlowEdge>();
	public List<DataFlowPath> dataFlowPaths = new ArrayList<DataFlowPath>();
	public List<PointsToSameEdge> pointsToSameEdges = new ArrayList<PointsToSameEdge>();
	public List<AliasEdge> aliasEdges = new ArrayList<AliasEdge>();

	public Graph(IMethodBinding methodBinding) {
		this.methodBinding = methodBinding;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.methodBinding);
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Graph) && (((Graph) o).methodBinding).equals(this.methodBinding);
	}

	public void AddControlFlowEdge(Node src, Node dest) {
		AddControlFlowEdge(new ControlFlowEdge(src, dest, null));
	}

	public void AddControlFlowPath(Node src, Node dest, boolean isWellFormed) {
		AddControlFlowPath(new ControlFlowPath(src, dest, null, isWellFormed));
	}

	public void AddDataFlowEdge(Node src, Node dest) {
		AddDataFlowEdge(new DataFlowEdge(src, dest, null));
	}

	public void AddDataFlowPath(Node src, Node dest, boolean isWellFormed) {
		AddDataFlowPath(new DataFlowPath(src, dest, null, isWellFormed));
	}

	public void AddMethodCallNode(ASTNode callpoint, IMethodBinding method, Node recv, List<Node> args, Node tr,
			SubGraph subgraph) {
		MethodCall node = new MethodCall(callpoint, recv, method, args, tr);
		node.subgraph = subgraph;
		methods.add(node);
	}

	private void AddControlFlowEdge(ControlFlowEdge edge) {
//		AddAliasIdentityEdge(edge.src);
//		AddAliasIdentityEdge(edge.dest);
		if (!edge.src.IsControlFlowTo(this, edge.dest)) {
			controlFlowEdges.add(edge);
			edge.src.AddControlFlowSrc(this, edge);

			changed = true;
		}
	}

	private void AddControlFlowPath(ControlFlowPath path) {
		if (!path.src.IsControlFlowPathTo(this, path.dest)) {
			controlFlowPaths.add(path);
			path.src.AddControlFlowPathSrc(this, path);

//			if (path.isInconsistent())
//				inconsistent = true;
			changed = true;
		}
	}

	private void AddDataFlowEdge(DataFlowEdge edge) {
//		AddAliasIdentityEdge(edge.src);
//		AddAliasIdentityEdge(edge.dest);
		if (!edge.src.IsDataFlowTo(this, edge.dest)) {
			dataFlowEdges.add(edge);
			edge.src.AddDataFlowSrc(this, edge);

			changed = true;
		}
	}

	private void AddDataFlowPath(DataFlowPath path) {
		if (!path.src.IsDataFlowPathTo(this, path.dest)) {
			dataFlowPaths.add(path);
			path.src.AddDataFlowPathSrc(this, path);

			changed = true;
		}
	}

	private void AddPointsToSameEdge(PointsToSameEdge edge) {
		if (!edge.src.PointsToSame(this, edge.dest)) {
			pointsToSameEdges.add(edge);
			edge.src.AddPointsToSameSrc(this, edge);
			edge.dest.AddPointsToSameDest(this, edge);
			changed = true;

			// TestProgram.WriteLine(edge);
		}
	}

//	public void AddAliasIdentityEdge(Node node) {
//		AliasEdge edge = new AliasEdge(node, node, null);
//		AddAliasEdge(edge);
//	}

	private void AddAliasEdge(AliasEdge edge) {
		if (!edge.src.IsAlias(this, edge.dest)) {
			aliasEdges.add(edge);

			edge.src.AddAliasSrc(this, edge);
			edge.src.AddAliasDest(this, edge);

			edge.dest.AddAliasSrc(this, edge);
			edge.dest.AddAliasDest(this, edge);

			changed = true;
		}
	}

//	private void AddAliasEdge(Node nodeA, Node nodeB) {
//		AddAliasEdge(new AliasEdge(nodeA, nodeB, null));
//		AddAliasEdge(new AliasEdge(nodeB, nodeA, null));
//	}

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

	public void Dotty(String name, MethodContext methodContext) {
		////////// MANUAL CONFIG
		boolean showEdges = true;
		boolean showPaths = true;
		boolean showControlFlowPaths = true;
		//////////
		//////////

		System.out.println("digraph " + name + " {");

		nodes = new ArrayList<Node>();

		if (showEdges) {
			for (DataFlowEdge edge : dataFlowEdges) {
				Append(edge.src);
				Append(edge.dest);
				System.out.println(edge.dotty() + "[color=\"darkgreen:invis:darkgreen\"];");
			}

			for (ControlFlowEdge edge : controlFlowEdges) {
				Append(edge.src);
				Append(edge.dest);
				System.out.println(edge.dotty() + "[color=\"darkviolet\"];");
			}

			for (AliasEdge edge : aliasEdges) {
				Append(edge.src);
				Append(edge.dest);
				System.out.println(edge.dotty() + "[dir=both,color=\"firebrick1:invis:firebrick1\"];");
			}
		}

		if (showPaths) {
			for (DataFlowPath path : dataFlowPaths) {
				Append(path.src);
				Append(path.dest);
				System.out.println(path.dotty() + "[arrowhead=onormal,color=\"darkgreen:invis:darkgreen\"];");
			}

			if (showControlFlowPaths) {
				for (ControlFlowPath path : controlFlowPaths) {
					Append(path.src);
					Append(path.dest);
					System.out.println(path.dotty() + "[arrowhead=onormal,color=\"darkviolet\"];");
				}
			}

			for (PointsToSameEdge edge : pointsToSameEdges) {
				Append(edge.src);
				Append(edge.dest);
				System.out.println(edge.dotty() + "[dir=both,color=firebrick1];");
			}
		}

		for (Node node : nodes)
			System.out.println("\t" + node.dotty() + "[label=\"" + node.contextAwareToString(methodContext) + "\"];");

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

		while (changed) {
			changed = false;

			// Compute points to same
			for (DataFlowPath dfPath1 : dataFlowPaths)
				if (dfPath1.src instanceof ValueNode) {
					AddPointsToSameEdge(new PointsToSameEdge(dfPath1.src, dfPath1.dest, Arrays.asList(dfPath1)));
					for (DataFlowPath dfPath2 : dfPath1.src.dataFlowPaths.get(this).values())
						AddPointsToSameEdge(
								new PointsToSameEdge(dfPath1.dest, dfPath2.dest, Arrays.asList(dfPath1, dfPath2)));
				}

			// Data flow implies Control flow
			for (DataFlowEdge edge : dataFlowEdges)
				AddControlFlowEdge(new ControlFlowEdge(edge.src, edge.dest, Arrays.asList(edge)));

			// Compute aliases
			for (PointsToSameEdge edge : pointsToSameEdges) {
				// field aliases
				Set<Entry<IVariableBinding, FieldNode>> fields = edge.src.fields.entrySet();
				for (Map.Entry<IVariableBinding, FieldNode> f : fields) {
					IVariableBinding field_name = f.getKey();
					if (edge.dest.fields.containsKey(field_name))
						AddAliasEdge(new AliasEdge(f.getValue(), edge.dest.fields.get(field_name), null));
				}

				// array aliases
				addArrayAccessAliases(edge.src, edge.dest);
				addArrayAccessAliases(edge.dest, edge.src);

				// identity array aliases
//				addArrayAccessAliases(edge.src, edge.src);
//				addArrayAccessAliases(edge.dest, edge.dest);
			}

			// Data flow transitivity without aliasing
//			for (DataFlowEdge edge1 : new ArrayList<DataFlowEdge>(dataFlowEdges))
//				if (edge1.dest instanceof ValueNode)
//					for (DataFlowEdge edge2 : edge1.dest.dataFlowEdges.values())
//						AddDataFlowEdge(new DataFlowEdge(edge1.src, edge2.dest, Arrays.asList(edge1, edge2)));

			// Data flow path
//			for (AliasEdge alias1 : new ArrayList<AliasEdge>(aliasEdges))
//				if (alias1.dest instanceof ValueNode)
//					for (Map.Entry<Node, AliasEdge> item2 : alias1.dest.aliasEdges.entrySet()) {
//						Node edge2_dest = item2.getKey();
//						AliasEdge edge2 = item2.getValue();
//						for (DataFlowEdge edge3 : edge2_dest.dataFlowEdges.values())
//							AddDataFlowEdge(
//									new DataFlowEdge(alias1.src, edge3.dest, Arrays.asList(alias1, edge2, edge3)));

//					}
//			for (AliasEdge alias1 : new ArrayList<AliasEdge>(aliasEdges))
//				findPathEnd(alias1.src, true);
			for (DataFlowEdge edge1 : new ArrayList<DataFlowEdge>(dataFlowEdges))
				findPathEnd(edge1.src, true);

			// Control flow transitivity without aliasing
//			for (ControlFlowEdge edge1 : new ArrayList<ControlFlowEdge>(controlFlowEdges))
//				if (edge1.dest instanceof ValueNode)
//					for (ControlFlowEdge edge2 : edge1.dest.controlFlowEdges.values())
//						AddControlFlowEdge(new ControlFlowEdge(edge1.src, edge2.dest, Arrays.asList(edge1, edge2)));

			// Control flow transitivity with aliasing
//			for (ControlFlowEdge edge1 : new ArrayList<ControlFlowEdge>(controlFlowEdges))
//				if (edge1.dest instanceof ValueNode)
//					for (Map.Entry<Node, AliasEdge> item2 : edge1.dest.aliasEdges.entrySet()) {
//						Node edge2_dest = item2.getKey();
//						AliasEdge edge2 = item2.getValue();
//						for (ControlFlowEdge edge3 : edge2_dest.controlFlowEdges.values())
//							AddControlFlowEdge(
//									new ControlFlowEdge(edge1.src, edge3.dest, Arrays.asList(edge1, edge2, edge3)));
//					}
			for (ControlFlowEdge edge1 : new ArrayList<ControlFlowEdge>(controlFlowEdges))
				findPathEnd(edge1.src, false);
//			for (AliasEdge alias1 : new ArrayList<AliasEdge>(aliasEdges))
//				findPathEnd(alias1.src, false);

			for (AliasEdge edge : new ArrayList<AliasEdge>(aliasEdges)) {
				findPathEnd(edge.src, true);
				findPathEnd(edge.src, false);
			}

			// Method closure
			for (MethodCall method : new ArrayList<MethodCall>(methods))
				if (method.recv == null) // constructor
				{
					InlineMethodCall(method);
				} else {
//					for (DataFlowPath typeflow : new ArrayList<DataFlowPath>(dataFlowPaths)) {
//						if (typeflow.src instanceof TypeNode && typeflow.dest == method.recv) {
					if (QUT.DataflowVisitor.hasMethod(method.method)) {
						ITypeBinding typeBinding = QUT.DataflowVisitor.typeBindings.get(method.recv);
						InlineMethodCall(method, typeBinding);
					} else if (QUT.DataflowVisitor.isChannel(method.recv)) {
						SymbolNode channelNode = (SymbolNode) method.recv;
						ITypeBinding binding = QUT.DataflowVisitor.typeBindings.get(channelNode);

						String bindingSecurity = binding.getTypeArguments()[0].getName().toLowerCase();
						SecurityLevel securityLevel = bindingSecurity.equals("high") ? SecurityLevel.HIGH
								: bindingSecurity.equals("low") ? SecurityLevel.LOW : SecurityLevel.UNKNOWN;

						Node securityLevelNode = null;
						if (securityLevel.equals(SecurityLevel.HIGH)) {
							securityLevelNode = high;
						} else if (securityLevel.equals(SecurityLevel.LOW)) {
							securityLevelNode = low;
						}

						if (method.method.getName().equals("readFromChannel")) {
							if (securityLevelNode == null)
								securityLevelNode = high;

							AddControlFlowEdge(method.recv, method.return_value); // CONFIRMED
							AddControlFlowEdge(securityLevelNode, method.return_value); // CONFIRMED
						} else if (method.method.getName().equals("writeToChannel")) {
							if (securityLevelNode == null)
								securityLevelNode = low;

							// the first arg should be the expression
							AddControlFlowEdge(method.args.get(0), method.return_value); // CONFIRMED
							AddControlFlowEdge(method.recv, method.return_value); // CONFIRMED
							AddControlFlowEdge(method.return_value, securityLevelNode); // CONFIRMED
						}
					} else {
						// we've called a method we don't have the sub-graph for
						// probably because it's a system or library method
						// perform a maximal conseverative analysis (everything inflkuences everything)

						// TODO: make this comprehensive
						// for now, only use unknown method where only the receiver influences the
						// outcome
						AddControlFlowEdge(method.recv, method.return_value);
					}
				}
//						}
//					}
		}
	}

//		if (inconsistent)
//			System.out.println("        Inconsistent\n");
//		else
//			System.out.println("        Consistent\n");
//
//		for (ControlFlowEdge edge : controlFlowEdges)
//			if (edge.isInconsistent())
//				edge.Explain(0);

	public boolean hasNoninterferenceViolations() {
		for (ControlFlowPath path : new ArrayList<ControlFlowPath>(controlFlowPaths)) {
			if (path.src.isHigh()) {
				if (path.violatesNoninterference()) {
					return true;
				}
			}
		}

		return false;
	}

	private void addArrayAccessAliases(Node src, Node dest) {
		if (arrayAccesses.containsKey(src)) {
			for (Node srcArrayAccess : arrayAccesses.get(src)) {
				if (arrayAccesses.containsKey(dest)) {
					for (Node destArrayAccess : arrayAccesses.get(dest)) {
						AddAliasEdge(new AliasEdge(srcArrayAccess, destArrayAccess, null)); // CONFIRMED
					}
				} else {
					// we need to have at least one dummy access to alias with
					Node exp = new ExpressionNode(dest.escapedName() + "[dummy]", methodBinding);
					Set<Node> arrayExpressionSet = new HashSet<Node>();
					arrayExpressionSet.add(exp);
					arrayAccesses.put(dest, arrayExpressionSet);
					AddAliasEdge(new AliasEdge(srcArrayAccess, exp, null)); // CONFIRMED
				}
			}
		}
	}

	private void findPathEnd(Node start, boolean isDataTransferPath) {
		Queue<BFSearchEntry> queue = new LinkedList<>();
		Set<BFSearchEntry> history = new HashSet<BFSearchEntry>();
		Set<Node> visited = new HashSet<Node>();
		boolean mayNotBeWellFormed = true;
		Set<Node> mayNotBeWellFormedSinks = new HashSet<Node>();

		queue.add(new BFSearchEntry(start, true));

		while (!queue.isEmpty()) {
			BFSearchEntry entry = queue.poll();
			history.add(entry);

			Node entryNode = entry.node;
			visited.add(entryNode);
			Set<Node> keys = null;

			if (isDataTransferPath) {
				Map<Node, DataFlowEdge> dataFlowEdges = entryNode.dataFlowEdges.get(this);
				if (dataFlowEdges != null) {
					keys = dataFlowEdges.keySet();
				}
			} else {
				Map<Node, ControlFlowEdge> controlFlowEdges = entryNode.controlFlowEdges.get(this);
				if (controlFlowEdges != null) {
					keys = controlFlowEdges.keySet();
				}
			}

			if (keys != null) {
				for (Node destination : keys) {
					BFSearchEntry newEntry = new BFSearchEntry(destination, true);
					if (!history.contains(newEntry)) {
						queue.add(newEntry);
					}
				}
			}

			if (entry.canBeAliasEdge) {
				Map<Node, AliasEdge> aliasEdges = entryNode.aliasEdges.get(this);
				if (aliasEdges != null) {
					keys = aliasEdges.keySet();

					for (Node destination : keys) {
						if (mayNotBeWellFormed) {
							mayNotBeWellFormedSinks.add(destination);
						}
						BFSearchEntry newEntry = new BFSearchEntry(destination, false);
						if (!history.contains(new BFSearchEntry(destination, true))) {
							if (!history.contains(newEntry)) {
								queue.add(newEntry);
							}
						}
					}
				}
			}

			mayNotBeWellFormed = false;
		}

		for (Node sink : visited) {
			if (isDataTransferPath)
				AddDataFlowPath(start, sink, !mayNotBeWellFormedSinks.contains(sink));
			else
				AddControlFlowPath(start, sink, !mayNotBeWellFormedSinks.contains(sink));
		}
	}

	// TODO: put this on the node class as an instance method
//	private boolean isChannel(Node node) {
//		ITypeBinding binding = QUT.DataflowVisitor.typeBindings.get(node);
//		return binding != null && binding.getQualifiedName().startsWith("com.qutifc.securitycontracts.types.Channel");
//	}

	private void InlineMethodCall(MethodCall method_invocation, ITypeBinding C) {
		if (method_invocation.already_expanded.contains(C) || C == null)
			return;

		method_invocation.already_expanded.add(C);
		String methodName = method_invocation.method.getName();
		String contractName = QUT.DataflowVisitor.getContractNameForInterface(C.getQualifiedName());

		MethodFoo method_body;
		// we can get a contract version of this method
		if (contractName != null) {
			method_body = QUT.DataflowVisitor.GetContractMethod(contractName, methodName).getValue();
		} else {
			// this is a private method of an implementation
			method_body = QUT.DataflowVisitor.getMethod(method_invocation.method);
		}

		SubGraph cloned = GetContour(method_body, method_invocation);
		MethodContext cloned_method = cloned.context;

		for (DataFlowEdge edge : cloned.method.graph.dataFlowEdges)
			AddDataFlowEdge(edge.src, edge.dest);

		for (ControlFlowEdge edge : cloned.method.graph.controlFlowEdges)
			AddControlFlowEdge(edge.src, edge.dest);

		AddDataFlowEdge(method_invocation.recv, cloned_method.recv); // CONFIRMED

		for (int i = 0; i < cloned_method.args.size(); i++)
			AddDataFlowEdge(method_invocation.args.get(i), cloned_method.args.get(i)); // CONFIRMED

		AddDataFlowEdge(cloned_method.return_value, method_invocation.return_value); // CONFIRMED
		AddControlFlowEdge(method_invocation.return_value, cloned_method.entry); // CONFIRMED
		AddControlFlowEdge(method_invocation.recv, cloned_method.entry); // CONFIRMED
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
				AddDataFlowEdge(method_invocation.args.get(i), cloned_method.args.get(i)); // CONFIRMED

			AddDataFlowEdge(cloned_method.return_value, method_invocation.return_value); // CONFIRMED
			AddControlFlowEdge(method_invocation.return_value, cloned_method.entry); // CONFIRMED

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
		clone.context.entry = map(method_body.context.entry);

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

	@Override
	public Node map(Node n) {
		if (node_mapping.containsKey(n))
			return node_mapping.get(n);
		else {
			Node clone = n.Clone(this);
			if (clone instanceof SymbolNode) {
				IBinding binding = ((SymbolNode) clone).getBinding();
				if (binding instanceof IVariableBinding)
					QUT.DataflowVisitor.typeBindings.put(clone, ((IVariableBinding) binding).getType());
			}
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