package ControlFlow;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import FlowGraph.MethodFoo;
import FlowGraph.Node;

public class ExpressionVisitor extends ASTVisitor {
	private Node src;
	MethodFoo method;

	public ExpressionVisitor(Expression src, MethodFoo method) {
		// if src == null then is entry point
		if (src != null) {
			this.src = QUT.DataflowVisitor.GetDataflowNode(src);
		}
		this.method = method;
	}

	@Override
	public boolean visit(Assignment node) {
		method.graph.AddControlFlowEdge(getSource(), QUT.DataflowVisitor.GetDataflowNode(node.getLeftHandSide())); // CONFIRMED
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		method.graph.AddControlFlowEdge(getSource(), method.context.return_value); // CONFIRMED
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		method.graph.AddControlFlowEdge(getSource(), QUT.DataflowVisitor.GetDataflowNode(node.getName()));
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		Node source = getSource();
		method.graph.AddControlFlowEdge(source, QUT.DataflowVisitor.GetDataflowNode(node)); // CONFIRMED
		Expression receiver = node.getExpression();
		if (receiver != null) {
			Node receiverNode = QUT.DataflowVisitor.GetDataflowNode(receiver);
			if (QUT.DataflowVisitor.isChannel(receiverNode)) {
				method.graph.AddControlFlowEdge(source, receiverNode); // CONFIRMED
			}
		}
		return super.visit(node);
	}

	private Node getSource() {
		return this.src != null ? this.src : method.context.entry;
	}
}
