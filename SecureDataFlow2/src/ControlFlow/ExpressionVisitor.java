package ControlFlow;

import org.eclipse.jdt.core.dom.*;

import FlowGraph.Graph;
import FlowGraph.Node;

public class ExpressionVisitor extends ASTVisitor
{
    private Node src;
    Graph graph;
    
    public ExpressionVisitor(Expression src, Graph graph)
    {
        this.src = QUT.DataflowVisitor.GetDataflowNode(src);
        this.graph = graph;
    }  
    
    @Override
    public boolean visit(Assignment node)
    {
        graph.AddControlFlowEdge(src, QUT.DataflowVisitor.GetDataflowNode(node.getLeftHandSide()));
        return super.visit(node);
    }
    
    @Override
    public boolean visit(VariableDeclarationFragment node)
    {
        graph.AddControlFlowEdge(src, QUT.DataflowVisitor.GetDataflowNode(node.getName()));
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodInvocation node)
    {
        graph.AddControlFlowEdge(src, QUT.DataflowVisitor.GetDataflowNode(node));
        return super.visit(node);
    }    
}
