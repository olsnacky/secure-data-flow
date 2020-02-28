package QUT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ProvidesDirective;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.UsesDirective;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import FlowGraph.ArrayAccessNode;
import FlowGraph.ChannelNode;
import FlowGraph.DataFlowPath;
import FlowGraph.Edge;
import FlowGraph.ExpressionNode;
import FlowGraph.FieldNode;
import FlowGraph.Graph;
import FlowGraph.LocalNode;
import FlowGraph.MethodContext;
import FlowGraph.MethodFoo;
import FlowGraph.Node;
import FlowGraph.SecurityLevel;
import FlowGraph.SymbolNode;
import FlowGraph.TypeNode;

public class DataflowVisitor extends ASTVisitor {
	private MethodFoo current_method;
	public Node this_node;
	public ITypeBinding currentTypeBinding;

	public static Map<QualifiedName, QualifiedName> moduleMappings = new Hashtable<QualifiedName, QualifiedName>();
	public static Map<String, String> contractMappings = new Hashtable<String, String>(); // interface, contract
	// public static Map<QualifiedName, Map<QualifiedName, Boolean>> verifications =
	// new Hashtable<QualifiedName, Map<QualifiedName, Boolean>>();
	public static List<IMethodBinding> requiresVerification = new ArrayList<IMethodBinding>();

	public static Map<IBinding, Node> nodes = new Hashtable<IBinding, Node>();
	public static Map<Node, ITypeBinding> typeBindings = new Hashtable<Node, ITypeBinding>();
	public static Map<String, ITypeBinding> classNameTypeBindings = new Hashtable<String, ITypeBinding>();
	public static Map<IMethodBinding, MethodFoo> methods = new Hashtable<IMethodBinding, MethodFoo>();
	public static Map<MethodFoo, IMethodBinding> reverseLookupMethodBindings = new Hashtable<MethodFoo, IMethodBinding>();
	public static Map<ITypeBinding, Map<String, String>> fieldMappings = new Hashtable<ITypeBinding, Map<String, String>>();

	private static IMethodBinding programEntryPoint;

	public static boolean isChannel(Node node) {
		ITypeBinding binding = QUT.DataflowVisitor.typeBindings.get(node);
		return binding != null && binding.getQualifiedName().startsWith("com.qutifc.securitycontracts.types.Channel");
	}

	public static void explode() {
		for (Map.Entry<IMethodBinding, MethodFoo> m : methods.entrySet()) {
			IMethodBinding binding = m.getKey();
			MethodFoo method = m.getValue();
			Graph graph = method.graph;

			// explode this
			explodeNode(method.context.recv, binding, graph, false);

			// explode formal arguments
			for (Node node : method.context.args) {
				explodeNode(node, binding, method.graph, true);
			}

			// explode return
			Node returnValue = method.context.return_value;
			if (typeBindings.containsKey(returnValue)) {
				explodeNode(returnValue, binding, graph, true);
			}

//			System.out.println("Finally Method " + binding.getDeclaringClass().getQualifiedName() + "::" + binding);

//			Graph graph = method.graph;
//			graph.Closure();
			// graph.Dotty(binding.getName());
		}
	}

	private static void explodeNode(Node node, IMethodBinding methodBinding, Graph graph, boolean useContracts) {
		ITypeBinding nodeTypeBinding = typeBindings.get(node);

		ITypeBinding typeBindingForExplosion = null;
		if (useContracts) {
			String contractName = getContractNameForInterface(nodeTypeBinding.getQualifiedName());
			if (contractName != null) {
				typeBindingForExplosion = classNameTypeBindings.get(contractName);
			}
		} else {
			typeBindingForExplosion = nodeTypeBinding;
		}

		if (typeBindingForExplosion != null) {
			IVariableBinding[] fieldVariableBindings = typeBindingForExplosion.getDeclaredFields();
			for (IVariableBinding fieldVariableBinding : fieldVariableBindings) {
				Node fieldNode = GetFieldNode(node, fieldVariableBinding, fieldMappings, methodBinding,
						nodeTypeBinding);
				graph.AddControlFlowEdge(node, fieldNode);

				// handle children
				if (!useContracts) {
					ITypeBinding fieldTypeBinding = typeBindings.get(fieldNode);
					// we only don't use contracts if it's a THIS node
					// so here we continue to not use contracts if the type
					// is the same type as THIS
					if (fieldTypeBinding.equals(nodeTypeBinding)) {
						explodeNode(fieldNode, methodBinding, graph, false);
					} else {
						explodeNode(fieldNode, methodBinding, graph, true);
					}
				} else {
					explodeNode(fieldNode, methodBinding, graph, true);
				}
			}
		}
	}

	public static void verifyNoninterferencePolicy() {
		MethodFoo entry = methods.get(programEntryPoint);
		Graph programGraph = entry.graph;
		programGraph.Closure();
		programGraph.Dotty("\"Implementation:" + programEntryPoint.getName() + "\"", entry.context);
		if (programGraph.hasNoninterferenceViolations()) {
			System.out.println("Program violates noninterference");
		} else {
			System.out.println("Program does not violate noninterference");
		}
	}

	public static List<String> Verify() {
		///////////// MANUAL CONFIG
		boolean shouldVerifySingleTarget = false;
		String targetClassName = "com.qutifc.implementations.parser.json.components.JSONParser";
		String targetMethodName = "parse";
		////////////
		///////////

		List<String> verificationErrors = new ArrayList<String>();
		for (IMethodBinding impMethBinding : requiresVerification) {
			System.out.println("Staring verification of " + impMethBinding.getDeclaringClass().getQualifiedName() + ":"
					+ impMethBinding.getName());
			ITypeBinding[] methodClassInterfaces = impMethBinding.getDeclaringClass().getInterfaces();
			if (methodClassInterfaces.length != 1) {
				System.out.println("Only 1 interface allowed");
				continue;
			}

			String interfaceName = methodClassInterfaces[0].getQualifiedName();
			String contractName = getContractNameForInterface(interfaceName);
			Map.Entry<IMethodBinding, MethodFoo> contractMethod = GetContractMethod(contractName,
					impMethBinding.getName());

			if (contractMethod == null) {
				verificationErrors.add("Cannot verify " + impMethBinding.getDeclaringClass().getQualifiedName() + ":"
						+ impMethBinding.getName() + " as there is no corresponding method in the contract");
				continue;
			} else {
				IMethodBinding conMethBinding = contractMethod.getKey();
				MethodFoo impMethFoo = methods.get(impMethBinding);
				MethodFoo conMethFoo = contractMethod.getValue();

				if ((!shouldVerifySingleTarget)
						|| (impMethBinding.getDeclaringClass().getQualifiedName().equals(targetClassName)
								&& impMethBinding.getName().equals(targetMethodName))) {
					System.out.println("Will verify " + impMethBinding.getDeclaringClass().getQualifiedName() + ":"
							+ impMethBinding.getName() + " against "
							+ conMethBinding.getDeclaringClass().getQualifiedName() + ":" + conMethBinding.getName());

					// determine if parameters are the same
					ITypeBinding[] impParamTypes = impMethBinding.getParameterTypes();
					ITypeBinding[] conParamTypes = conMethBinding.getParameterTypes();
					boolean signaturesMeet = impParamTypes.length == conParamTypes.length
							&& impMethFoo.context.args.size() == conMethFoo.context.args.size();
					if (signaturesMeet) {
						for (int i = 0; i < impParamTypes.length; i++) {
							signaturesMeet &= impParamTypes[i].getQualifiedName()
									.equals(conParamTypes[i].getQualifiedName())
									&& impMethFoo.context.args.get(i).name.equals(conMethFoo.context.args.get(i).name);
							if (!signaturesMeet)
								break;
						}

					}

					// return type matches
					signaturesMeet &= impMethBinding.getReturnType().getQualifiedName()
							.equals(conMethBinding.getReturnType().getQualifiedName());

					if (!signaturesMeet) {
						verificationErrors.add("Cannot verify " + impMethBinding.getDeclaringClass().getQualifiedName()
								+ ":" + impMethBinding.getName()
								+ " as its signature is to different to that of the contract");
						continue;
					}

					explode();

					Graph impGraph = impMethFoo.getVerificationGraph(true);
					Graph conGraph = conMethFoo.getVerificationGraph(false);

					boolean result = verifyMethodEdges(impGraph.dataFlowPaths, conGraph.dataFlowPaths,
							impMethFoo.context, conMethFoo.context, verificationErrors, impMethFoo.getFullName());
					result &= verifyMethodEdges(impGraph.controlFlowPaths, conGraph.controlFlowPaths,
							impMethFoo.context, conMethFoo.context, verificationErrors, impMethFoo.getFullName());
					result &= verifyMethodEdges(impGraph.pointsToSameEdges, conGraph.pointsToSameEdges,
							impMethFoo.context, conMethFoo.context, verificationErrors, impMethFoo.getFullName());

					System.out.println("Full Graph");
					impMethFoo.graph.Dotty("\"Implementation:" + impMethBinding.getName() + "\"", impMethFoo.context);
					conMethFoo.graph.Dotty("\"Contract:" + conMethBinding.getName() + "\"", conMethFoo.context);
					System.out.println("Verification Graph");
					impGraph.Dotty("\"Implementation:" + impMethBinding.getName() + "\"", impMethFoo.context);
					conGraph.Dotty("\"Contract:" + conMethBinding.getName() + "\"", conMethFoo.context);
				}
			}
		}

		return verificationErrors;
	}

	private static boolean verifyMethodEdges(List<? extends Edge> impEdges, List<? extends Edge> conEdges,
			MethodContext impContext, MethodContext conContext, List<String> verificationProblems, String methodName) {
		boolean result = true;

		for (Edge impEdge : impEdges) {
			boolean found = false;
			for (Edge conEdge : conEdges) {
				if (impEdge.correspondsTo(conEdge, impContext, conContext)) {
					found = true;

					if (impEdge instanceof DataFlowPath) {
						DataFlowPath idfp = (DataFlowPath) impEdge;
						DataFlowPath cdfp = (DataFlowPath) conEdge;

						if (idfp.isWellFormed && !cdfp.isWellFormed) {
							verificationProblems
									.add(impEdge + " in " + methodName + " is well-formed, but " + conEdge + " is not");
						}
					}

					break;
				}
			}

			if (!found) {
				result = false;
				verificationProblems.add("Found edge " + impEdge.getClass().getSimpleName() + " " + impEdge + " in "
						+ methodName + " without a corresponding edge in the contract");
			}
		}

		return result;
	}

	public void Dump() {
		for (Map.Entry<IMethodBinding, MethodFoo> m : methods.entrySet()) {
			System.out.println("  Method " + m.getKey().getDeclaringClass().getQualifiedName() + "::" + m.getKey());
			m.getValue().graph.Print();
		}
	}

	public static String getContractNameForInterface(String interfaceName) {
		for (Map.Entry<String, String> contractMapping : contractMappings.entrySet()) {
			String key = contractMapping.getKey();
			if (key.equals(interfaceName)) {
				return contractMapping.getValue();
			}
		}

		return null;
	}

	public static Map.Entry<IMethodBinding, MethodFoo> GetContractMethod(String contractName, String methodName) {
		for (Map.Entry<IMethodBinding, MethodFoo> m : methods.entrySet()) {
			IMethodBinding binding = m.getKey();
			if (binding.getDeclaringClass().getQualifiedName().equals(contractName)
					&& binding.getName().toString().equals(methodName)) {
				return m;
			}
		}

		return null;
	}

	public static Node GetDataflowNode(ASTNode node) {
		Node n = (Node) node.getProperty("qut");
		if (n == null) {
			if (!isMapsToAnnotation(node) && !isChannelConstructorArgument(node)) {
				throw new RuntimeException(
						"Failed to fetch node created for child " + node + " (" + node.getClass() + ")");
			}
		}
		return n;
	}

	private static boolean isMapsToAnnotation(ASTNode n) {
		if (n instanceof Annotation) {
			Annotation a = (Annotation) n;
			String annoTypeName = a.getTypeName().toString();
			return annoTypeName.equals("MapsTo") || annoTypeName.equals("Target");
		}

		return false;
	}

	private static boolean isChannelConstructorArgument(ASTNode n) {
		if (n instanceof LambdaExpression) {
			LambdaExpression expr = (LambdaExpression) n;
			ASTNode parent = expr.getParent();
			if (parent instanceof ClassInstanceCreation) {
				ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) parent;
				// if it is a lambda expression this is a write channel
				// and the wrote function should be the second argument
				if (classInstanceCreation.arguments().get(1).equals(expr)) {
					if (classInstanceCreation.getType().isSimpleType()) {
						SimpleType simpleType = (SimpleType) classInstanceCreation.getType();
						return simpleType.getName().getFullyQualifiedName().equals("Channel");
					}
				}
			}
		}

		return false;
	}

	private void SetDataflowNode(ASTNode node, Node exp) {
		node.setProperty("qut", exp);
	}

	public static Node GetNode(IBinding binding, IMethodBinding methodBinding) {
		if (nodes.containsKey(binding))
			return nodes.get(binding);
		else {
			Node node = null;
			if (binding instanceof ITypeBinding)
				node = new TypeNode((ITypeBinding) binding);
			else {
				node = new SymbolNode(binding, methodBinding);
				if (binding instanceof IVariableBinding)
					typeBindings.put(node, ((IVariableBinding) binding).getType());
			}
			nodes.put(binding, node);
			return node;
		}
	}

	public static FieldNode GetFieldNode(Node parentNode, IVariableBinding name,
			Map<ITypeBinding, Map<String, String>> fieldMappings, IMethodBinding methodBinding,
			ITypeBinding typeBinding) {
		if (!parentNode.fields.containsKey(name)) {
			FieldNode node = new FieldNode(parentNode, name, methodBinding);
			typeBindings.put(node, name.getType());
			if (fieldMappings != null) {
				Map<String, String> methodFieldMappings = fieldMappings.get(typeBinding);

				if (methodFieldMappings != null) {
					if (methodFieldMappings.containsKey(node.name)) {
						node.mapsTo = methodFieldMappings.get(node.name);
					}
				}
			}
			parentNode.fields.put(name, node);
			return node;
		} else
			return parentNode.fields.get(name);
	}

	// we use this check so that we can compare method bindings across compilation
	// units
	public static boolean hasMethod(IMethodBinding methodBinding) {
		for (IMethodBinding b : QUT.DataflowVisitor.methods.keySet()) {
			if (b.isEqualTo(methodBinding)) {
				return true;
			}
		}

		return false;
	}

	// we use this check so that we can compare method bindings across compilation
	// units
	public static MethodFoo getMethod(IMethodBinding methodBinding) {
		for (IMethodBinding b : QUT.DataflowVisitor.methods.keySet()) {
			if (b.isEqualTo(methodBinding)) {
				return QUT.DataflowVisitor.methods.get(b);
			}
		}

		return null;
	}

	private static boolean hasMethodBinding(List<IMethodBinding> bindings, IMethodBinding binding) {
		for (IMethodBinding b : bindings) {
			if (b.isEqualTo(binding)) {
				return true;
			}
		}

		return false;
	}

	// ---------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean visit(ImportDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(UsesDirective node) {
		return false;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(ProvidesDirective node) {
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		CompilationUnit cu = (CompilationUnit) node.getParent();
		String packageName = cu.getPackage().getName().toString();
		String typeName = node.getName().getFullyQualifiedName();
		String fullyQualifiedName = String.format("%s.%s", packageName, typeName);

		ITypeBinding typeBinding = node.resolveBinding();
		classNameTypeBindings.put(fullyQualifiedName, typeBinding);
		currentTypeBinding = typeBinding;

		return super.visit(node);
	}

	@Override
	public void endVisit(ProvidesDirective node) {
		super.endVisit(node);
		QualifiedName contractName = (QualifiedName) node.getName();
		// assume implementation only implement one contract
		QualifiedName implementationName = (QualifiedName) node.implementations().get(0);
		DataflowVisitor.moduleMappings.put(implementationName, contractName);
	}

	@Override
	public void endVisit(NormalAnnotation node) {
		String name = node.getTypeName().toString();
		if (name.equals("SecurityContract")) {
			String packageName = ((CompilationUnit) node.getParent().getParent()).getPackage().getName().toString();
			String itface = ((TypeDeclaration) node.getParent()).getName().toString();
			String securityContractName = ((StringLiteral) ((MemberValuePair) node.values().get(0)).getValue())
					.getLiteralValue();
			String fullyQualifiedName = String.format("%s.%s", packageName, itface);
			DataflowVisitor.contractMappings.put(fullyQualifiedName, securityContractName);
		} else if (name.equals("MapsTo")) {
			String contractFieldName = ((StringLiteral) ((MemberValuePair) node.values().get(0)).getValue())
					.getLiteralValue();
			String implementationFieldName = ((VariableDeclarationFragment) ((FieldDeclaration) node.getParent())
					.fragments().toArray()[0]).getName().toString();

			if (!fieldMappings.containsKey(currentTypeBinding)) {
				fieldMappings.put(currentTypeBinding, new Hashtable<String, String>());
			}

			Map<String, String> methodFieldMappings = fieldMappings.get(currentTypeBinding);
			methodFieldMappings.put(implementationFieldName, contractFieldName);
		}
	}

	// @Override
	// public void endVisit(EnhancedForStatement node) {
	// super.endVisit(node);
	// Node expr = GetDataflowNode(node.getExpression());
	// Node var = GetNode(node.getParameter().resolveBinding());
	// current_method.graph.AddControlFlowEdge(expr, var);
	//// current_method.graph.AddControlFlowEdge(args, exp);
	//// SetDataflowNode(node, exp);
	// }

	@Override
	public void endVisit(Assignment node) {
		super.endVisit(node);

		Expression left = node.getLeftHandSide();
		Expression right = node.getRightHandSide();

		Node lhs = GetDataflowNode(left);
		Node rhs = GetDataflowNode(right);
		Node exp = new ExpressionNode(node, getCurrentMethodBinding());

//		if (left instanceof ArrayAccess) {
//			// If we are writing to a[i] then we are writing to a (???)
//			Node array = GetDataflowNode(((ArrayAccess) left).getArray());
//			current_method.graph.AddControlFlowEdge(lhs, array); // WHY
//		}

		// current_method.graph.AddControlFlowEdge(lhs, exp); Fixme: ?
		current_method.graph.AddDataFlowEdge(rhs, exp); // WHY
		current_method.graph.AddDataFlowEdge(rhs, lhs); // CONFIRMED
		SetDataflowNode(node, exp);
	}

	@Override
	public void endVisit(ConditionalExpression node) {
		super.endVisit(node);
		ExpressionNode exp = new ExpressionNode(node, getCurrentMethodBinding());
		current_method.graph.AddControlFlowEdge(GetDataflowNode(node.getExpression()), exp);
		current_method.graph.AddDataFlowEdge(GetDataflowNode(node.getThenExpression()), exp);
		current_method.graph.AddDataFlowEdge(GetDataflowNode(node.getElseExpression()), exp);
		SetDataflowNode(node, exp);
	}

	@Override
	public void endVisit(VariableDeclarationExpression node) {
		super.endVisit(node);
		ExpressionNode exp = new ExpressionNode(node, getCurrentMethodBinding());
		for (Object var : node.fragments()) {
			VariableDeclarationFragment frag = (VariableDeclarationFragment) var;
			Node lhs = GetNode(frag.resolveBinding(), getCurrentMethodBinding());
			if (frag.getInitializer() != null) {
				Node rhs = GetDataflowNode(frag.getInitializer());
				current_method.graph.AddDataFlowEdge(rhs, lhs);
			}
		}
		SetDataflowNode(node, exp); // no actual value, so no in flows needed???
	}

	@Override
	public void endVisit(SimpleName node) {
		super.endVisit(node);
		IBinding binding = node.resolveBinding();
		if (binding instanceof IVariableBinding) {
			IVariableBinding varbinding = (IVariableBinding) binding;
			if (varbinding.isField() && this_node != null) {
				Node expr = GetFieldNode(this_node, varbinding, fieldMappings, getCurrentMethodBinding(),
						currentTypeBinding);
				SetDataflowNode(node, expr);
				return;
			}
		}

		SetDataflowNode(node, GetNode(binding, getCurrentMethodBinding()));
	}

	@Override
	public void endVisit(QualifiedName node) {
		super.endVisit(node);
		IBinding binding = node.resolveBinding();
		if (binding instanceof IVariableBinding) {
			IVariableBinding varbinding = (IVariableBinding) binding;
			if (varbinding.isField()) {
				Node object = GetDataflowNode(node.getQualifier());
				Node expr = GetFieldNode(object, varbinding, fieldMappings, getCurrentMethodBinding(),
						currentTypeBinding);
				current_method.graph.AddControlFlowEdge(object, expr);
				SetDataflowNode(node, expr);
				return;
			}
		}
		SetDataflowNode(node, GetNode(binding, getCurrentMethodBinding()));
	}

	@Override
	public void endVisit(LambdaExpression node) {
		super.endVisit(node);
		IBinding binding = node.resolveMethodBinding();
		SetDataflowNode(node, GetNode(binding, getCurrentMethodBinding()));
	}

	@Override
	public void endVisit(ArrayAccess node) {
		super.endVisit(node);

		Node array = GetDataflowNode(node.getArray());
		Node args = GetDataflowNode(node.getIndex());
		Node exp = new ArrayAccessNode(array, node, getCurrentMethodBinding());
		current_method.graph.AddControlFlowEdge(array, exp); // CONFIRMED
		current_method.graph.AddControlFlowEdge(args, exp); // CONFIRMED
		SetDataflowNode(node, exp);

		// create registry of array accesses - for aliasing later on
		if (!current_method.graph.arrayAccesses.containsKey(array)) {
			current_method.graph.arrayAccesses.put(array, new HashSet<Node>());
		}

		current_method.graph.arrayAccesses.get(array).add(exp);
	}

	@Override
	public void endVisit(ArrayInitializer node) {
		super.endVisit(node);
		Node exp = new ExpressionNode(node, getCurrentMethodBinding());
		for (Object element : node.expressions())
			current_method.graph.AddControlFlowEdge(GetDataflowNode((Expression) element), exp);
		SetDataflowNode(node, exp);
	}

	@Override
	public void endVisit(CastExpression node) {
		super.endVisit(node);
		SetDataflowNode(node, GetDataflowNode(node.getExpression()));
	}

	@Override
	public void endVisit(InstanceofExpression node) {
		super.endVisit(node);
		ExpressionNode exp = new ExpressionNode(node, getCurrentMethodBinding());
		Node child = GetDataflowNode(node.getLeftOperand());
		current_method.graph.AddControlFlowEdge(child, exp);
		SetDataflowNode(node, exp);
	}

	@Override
	public void endVisit(ParenthesizedExpression node) {
		super.endVisit(node);
		SetDataflowNode(node, GetDataflowNode(node.getExpression()));
	}

	@Override
	public void endVisit(PostfixExpression node) {
		super.endVisit(node);
		ExpressionNode exp = new ExpressionNode(node, getCurrentMethodBinding());
		Node child = GetDataflowNode(node.getOperand());
		current_method.graph.AddControlFlowEdge(child, exp); // CONFIRMED
		SetDataflowNode(node, exp);
	}

	@Override
	public void endVisit(PrefixExpression node) {
		super.endVisit(node);
		ExpressionNode exp = new ExpressionNode(node, getCurrentMethodBinding());
		Node child = GetDataflowNode(node.getOperand());
		current_method.graph.AddControlFlowEdge(child, exp); // CONFIRMED
		SetDataflowNode(node, exp);
	}

	@Override
	public void endVisit(InfixExpression node) {
		super.endVisit(node);
		ExpressionNode exp = new ExpressionNode(node, getCurrentMethodBinding());
		Node lhs = GetDataflowNode(node.getLeftOperand());
		Node rhs = GetDataflowNode(node.getRightOperand());
		current_method.graph.AddControlFlowEdge(lhs, exp); // CONFIRMED
		current_method.graph.AddControlFlowEdge(rhs, exp); // CONFIRMED
		for (Object operand : node.extendedOperands())
			current_method.graph.AddControlFlowEdge(GetDataflowNode((Expression) operand), exp); // CONFIRMED
		SetDataflowNode(node, exp);
	}

	@Override
	public void endVisit(ReturnStatement node) {
		super.endVisit(node);
		Node returnValue = GetDataflowNode(node.getExpression());
		current_method.graph.AddDataFlowEdge(returnValue, current_method.context.return_value); // CONFIRMED
	}

	@Override
	public void endVisit(SingleVariableDeclaration node) {
		super.endVisit(node);
		Node lhs = GetNode(node.resolveBinding(), getCurrentMethodBinding());
		if (node.getInitializer() != null) {
			Node rhs = GetDataflowNode(node.getInitializer());
			current_method.graph.AddDataFlowEdge(rhs, lhs);
		}
	}

	// --------------------------------------------------------------------------------------------------------------
	// Member expressions
	// --------------------------------------------------------------------------------------------------------------

	@Override
	public void endVisit(FieldAccess node) {
		super.endVisit(node);
		Node obj = GetDataflowNode(node.getExpression());
		FieldNode field = GetFieldNode(obj, node.resolveFieldBinding(), fieldMappings, getCurrentMethodBinding(),
				currentTypeBinding);

		SetDataflowNode(node, field);

		current_method.graph.AddControlFlowEdge(obj, field); // CONFIRMED
	}

//	public void endVisit(FieldDeclaration node) {
//		super.endVisit(node);
////		for (Object modifier : node.modifiers()) {
////			if (modifier instanceof NormalAnnotation) {
////				NormalAnnotation normalAnnotation = (NormalAnnotation) modifier;
////				if (normalAnnotation.getTypeName().toString().equals("MapsTo")) {
////					fieldMappings.put(((VariableDeclarationFragment)node.fragments().toArray()[0]).getName().toString(), ((MemberValuePair)normalAnnotation.values().toArray()[0]).getValue().toString());
////				}
////			}
////		}
//	}

	@Override
	public void endVisit(SuperFieldAccess node) {
		super.endVisit(node);
		FieldNode field = GetFieldNode(this_node, node.resolveFieldBinding(), fieldMappings, getCurrentMethodBinding(),
				currentTypeBinding);
		SetDataflowNode(node, field);
	}

	@Override
	public void endVisit(ArrayCreation node) {
		super.endVisit(node);

		List<Node> args = new ArrayList<Node>();
		for (Object arg : node.dimensions())
			args.add(GetDataflowNode((Expression) arg));

		Node expr = new ExpressionNode(node, getCurrentMethodBinding());
		current_method.graph.AddDataFlowEdge(GetNode(node.getType().resolveBinding(), getCurrentMethodBinding()), expr);
		SetDataflowNode(node, expr);
	}

	@Override
	public void endVisit(ClassInstanceCreation node) {
		super.endVisit(node);

		List<Node> args = new ArrayList<Node>();
		for (Object arg : node.arguments())
			args.add(GetDataflowNode((Expression) arg));

		Node expr = null;
		Type nodeType = node.getType();
		if (nodeType.isSimpleType()) {
			SimpleType nodeTypeAsSimpleType = (SimpleType) nodeType;
			if (nodeTypeAsSimpleType.getName().getFullyQualifiedName().equals("Channel")) {
				String givenSecurityLevel = ((QualifiedName) node.arguments().get(0)).getName().getIdentifier();
				SecurityLevel securityLevel = givenSecurityLevel.equals("HIGH") ? SecurityLevel.HIGH
						: givenSecurityLevel.equals("LOW") ? SecurityLevel.LOW : SecurityLevel.UNKNOWN;
				expr = new ChannelNode(node, getCurrentMethodBinding(), securityLevel);
			}
		}

		if (expr == null)
			expr = new ExpressionNode(node, getCurrentMethodBinding());

		current_method.graph.AddDataFlowEdge(
				GetNode(node.resolveConstructorBinding().getDeclaringClass(), getCurrentMethodBinding()), expr);
		current_method.graph.AddMethodCallNode(node, node.resolveConstructorBinding(), null, args, expr, null);
		SetDataflowNode(node, expr);
	}

	@Override
	public void endVisit(MethodInvocation node) {
		super.endVisit(node);

		Node recv;
		if (node.getExpression() != null)
			recv = GetDataflowNode(node.getExpression());
		else {
			recv = this_node;
			current_method.graph.AddDataFlowEdge(
					GetNode(node.resolveMethodBinding().getDeclaringClass(), getCurrentMethodBinding()), this_node);
		}

		List<Node> args = new ArrayList<Node>();
		for (Object arg : node.arguments())
			args.add(GetDataflowNode((Expression) arg));

		Node expr = new ExpressionNode(node, getCurrentMethodBinding());
//		current_method.graph.AddControlFlowEdge(recv, expr); // CONFIRMED MAYBE??
		IMethodBinding binding = node.resolveMethodBinding();
		current_method.graph.AddMethodCallNode(node, binding, recv, args, expr, null);
		SetDataflowNode(node, expr);
	}

	@Override
	public void endVisit(ConstructorInvocation node) {
		super.endVisit(node);
		List<Node> args = new ArrayList<Node>();
		for (Object arg : node.arguments())
			args.add(GetDataflowNode((Expression) arg));

		Node expr = new ExpressionNode(node, getCurrentMethodBinding());
		current_method.graph.AddMethodCallNode(node, node.resolveConstructorBinding(), this_node, args, expr, null);
		SetDataflowNode(node, expr);
	}

	@Override
	public void endVisit(SuperConstructorInvocation node) {
		super.endVisit(node);
		List<Node> args = new ArrayList<Node>();
		for (Object arg : node.arguments())
			args.add(GetDataflowNode((Expression) arg));

		Node expr = new ExpressionNode(node, getCurrentMethodBinding());
		current_method.graph.AddMethodCallNode(node, node.resolveConstructorBinding(), this_node, args, expr, null);
		SetDataflowNode(node, expr);
	}

	@Override
	public void endVisit(SuperMethodInvocation node) {
		super.endVisit(node);
		List<Node> args = new ArrayList<Node>();
		for (Object arg : node.arguments())
			args.add(GetDataflowNode((Expression) arg));

		Node expr = new ExpressionNode(node, getCurrentMethodBinding());
		current_method.graph.AddMethodCallNode(node, node.resolveMethodBinding(), this_node, args, expr, null);
		SetDataflowNode(node, expr);
	}

	@Override
	public void endVisit(ThisExpression node) {
		super.endVisit(node);
		SetDataflowNode(node, this_node);
	}

	// --------------------------------------------------------------------------------------------------------------
	// Literals
	// --------------------------------------------------------------------------------------------------------------

	@Override
	public void endVisit(TypeLiteral node) {
		super.endVisit(node);
		SetDataflowNode(node, new ExpressionNode(node.resolveTypeBinding(), getCurrentMethodBinding()));
	}

	@Override
	public void endVisit(BooleanLiteral node) {
		super.endVisit(node);
		SetDataflowNode(node, new ExpressionNode(node.booleanValue(), getCurrentMethodBinding()));
	}

	@Override
	public void endVisit(CharacterLiteral node) {
		super.endVisit(node);
		SetDataflowNode(node, new ExpressionNode(node.charValue(), getCurrentMethodBinding()));
	}

	@Override
	public void endVisit(NullLiteral node) {
		super.endVisit(node);
		SetDataflowNode(node, new ExpressionNode(node, getCurrentMethodBinding()));
	}

	@Override
	public void endVisit(NumberLiteral node) {
		super.endVisit(node);
		SetDataflowNode(node, new ExpressionNode(node.getToken(), getCurrentMethodBinding()));
	}

	@Override
	public void endVisit(StringLiteral node) {
		super.endVisit(node);
		SetDataflowNode(node, new ExpressionNode(node.getLiteralValue(), getCurrentMethodBinding()));
	}

	// ------------------------------------------------------------------------------------------------------------------------------
	// Declarations
	// ------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void endVisit(VariableDeclarationStatement node) {
		super.endVisit(node);
		for (Object var : node.fragments()) {
			VariableDeclarationFragment frag = (VariableDeclarationFragment) var;
			Node lhs = GetNode(frag.resolveBinding(), getCurrentMethodBinding());
			if (frag.getInitializer() != null) {
				Node rhs = GetDataflowNode(frag.getInitializer());
				current_method.graph.AddDataFlowEdge(rhs, lhs);
			}
		}
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		IMethodBinding binding = node.resolveBinding();
		current_method = new MethodFoo(binding);
		methods.put(binding, current_method);
		reverseLookupMethodBindings.put(current_method, binding);

		for (Object p : node.parameters()) {
			SingleVariableDeclaration param = (SingleVariableDeclaration) p;
			current_method.context.args.add(GetNode(param.resolveBinding(), getCurrentMethodBinding()));
		}

		current_method.context.recv = this_node = new LocalNode("this", binding);
		current_method.context.return_value = new LocalNode("return", binding);
		current_method.context.entry = new LocalNode("method-control-flow-entry", binding);

		// if implementation method, register as needing verification
		// relied on the module descriptor being analysed first
		for (Object modifier : node.modifiers()) {
			if (((IExtendedModifier) modifier).isModifier()) {
				if (((Modifier) modifier).isPublic()) {
					for (Map.Entry<QualifiedName, QualifiedName> mm : moduleMappings.entrySet()) {
						if (mm.getKey().toString().equals(binding.getDeclaringClass().getQualifiedName())) {
							if (!hasMethodBinding(requiresVerification, binding)) {
								requiresVerification.add(binding);
								break;
							}
						}
					}
					break;
				}
			}
		}

		// setup the entry point
		if (binding.getDeclaringClass().getQualifiedName().equals("com.qutifc.clients.components.Main")
				&& binding.getName().equals("run")) {
			programEntryPoint = binding;
		}

		// setup type bindings
		typeBindings.put(this_node, currentTypeBinding);
		Type returnType = node.getReturnType2();
		if (returnType != null) {
			typeBindings.put(current_method.context.return_value, returnType.resolveBinding());
		}

		return super.visit(node);
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		super.endVisit(node);

		current_method = null;
		this_node = null;
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		super.endVisit(node);
		currentTypeBinding = null;
	}

	@Override
	public void postVisit(ASTNode node) {
		// TODO Auto-generated method stub
		super.postVisit(node);
		if (shouldCheckExistence(node))
			GetDataflowNode(node); // test that we have created a node for every expression
	}

	private boolean shouldCheckExistence(ASTNode node) {
		return node instanceof Expression && !(node instanceof MarkerAnnotation) && !(node instanceof NormalAnnotation)
				&& !(node instanceof SingleMemberAnnotation);
	}

	private IMethodBinding getCurrentMethodBinding() {
		if (current_method != null) {
			return reverseLookupMethodBindings.get(current_method);
		}

		return null;
	}
}
