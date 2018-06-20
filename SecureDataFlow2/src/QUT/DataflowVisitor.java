package QUT;

import FlowGraph.*;

import org.eclipse.jdt.core.dom.*;

import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class DataflowVisitor extends ASTVisitor
{
    private MethodFoo current_method; 
    public Node this_node;   
    
    public static Map<IBinding, Node> nodes = new Hashtable<IBinding, Node>();
    public static Map<IMethodBinding, MethodFoo> methods = new Hashtable<IMethodBinding, MethodFoo>();
   
    public static void Closure()
    {
        for (Map.Entry<IMethodBinding, MethodFoo> m : methods.entrySet())
        {
            IMethodBinding binding = m.getKey();
            Graph graph = m.getValue().graph;
            System.out.println("Finally Method " + binding.getDeclaringClass().getQualifiedName() + "::" + binding);
            graph.Closure();
            graph.Dotty(binding.getName()); 
        }
    }
    
    public void Dump()
    {
        for (Map.Entry<IMethodBinding, MethodFoo> m : methods.entrySet())
        {
            System.out.println("  Method " + m.getKey().getDeclaringClass().getQualifiedName() + "::" + m.getKey());
            m.getValue().graph.Print();
        }
    }
    
    public static Node GetDataflowNode(ASTNode node) 
    {
        Node n = (Node)node.getProperty("qut");
        if (n == null)
            throw new RuntimeException("Failed to fetch node created for child " + node + " (" + node.getClass() + ")");
        return n;
    }
    
    private void SetDataflowNode(ASTNode node, Node exp)
    {
        node.setProperty("qut", exp);
    }
    
    public static Node GetNode(IBinding binding)
    {
        if (nodes.containsKey(binding))
            return nodes.get(binding);
        else
        {
            Node node = null;
            if (binding instanceof ITypeBinding)
                node = new TypeNode((ITypeBinding)binding);
            else
                node = new SymbolNode(binding);
            nodes.put(binding, node);
            return node;
        }
    }
    
    // we use this check so that we can compare method bindings across compilation units
    public static boolean hasMethod(IMethodBinding methodBinding) {
    	for (IMethodBinding b : QUT.DataflowVisitor.methods.keySet()) {
    		if (b.isEqualTo(methodBinding)) {
    			return true;
    		}
    	}
    	
    	return false;
    }

    // we use this check so that we can compare method bindings across compilation units
    public static MethodFoo getMethod(IMethodBinding methodBinding) {
    	for (IMethodBinding b : QUT.DataflowVisitor.methods.keySet()) {
    		if (b.isEqualTo(methodBinding)) {
    			return QUT.DataflowVisitor.methods.get(b);
    		}
    	}
    	
    	return null;
    }
    
    // ---------------------------------------------------------------------------------------------------------------------------

    @Override
    public void endVisit(Assignment node)
    {
        super.endVisit(node);
        
        Expression left = node.getLeftHandSide();
        Expression right = node.getRightHandSide();
        
        Node lhs = GetDataflowNode(left);
        Node rhs = GetDataflowNode(right);
        Node exp = new ExpressionNode(node);
        
        if (left instanceof ArrayAccess)
        {
            // If we are writing to a[i] then we are writing to a (???)
            Node array = GetDataflowNode(((ArrayAccess)left).getArray());
            current_method.graph.AddControlFlowEdge(lhs, array);
        }

        // current_method.graph.AddControlFlowEdge(lhs, exp);  Fixme: ?
        current_method.graph.AddDataFlowEdge(rhs, exp);
        current_method.graph.AddDataFlowEdge(rhs,lhs);
        SetDataflowNode(node, exp);
    }   
    
    @Override
    public void endVisit(ConditionalExpression node)
    {
        super.endVisit(node);
        ExpressionNode exp = new ExpressionNode(node);
        current_method.graph.AddControlFlowEdge(GetDataflowNode(node.getExpression()), exp);
        current_method.graph.AddDataFlowEdge(GetDataflowNode(node.getThenExpression()), exp);
        current_method.graph.AddDataFlowEdge(GetDataflowNode(node.getElseExpression()), exp);        
        SetDataflowNode(node, exp);        
    }

    @Override
    public void endVisit(VariableDeclarationExpression node)
    {
        super.endVisit(node);
        ExpressionNode exp = new ExpressionNode(node);
        for (Object var : node.fragments())
        {
            VariableDeclarationFragment frag = (VariableDeclarationFragment) var;
            Node lhs = GetNode(frag.resolveBinding());
            if (frag.getInitializer() != null)
            {
                Node rhs = GetDataflowNode(frag.getInitializer());
                current_method.graph.AddDataFlowEdge(rhs, lhs);
            }           
        }   
        SetDataflowNode(node, exp); // no actual value, so no in flows needed???
    }  

    @Override
    public void endVisit(SimpleName node)
    {
        super.endVisit(node);        
        IBinding binding = node.resolveBinding();
        if (binding instanceof IVariableBinding)
        {
            IVariableBinding varbinding = (IVariableBinding)binding;
            if (varbinding.isField() && this_node != null)
            {
                Node expr = this_node.getField(varbinding);
                SetDataflowNode(node, expr);
                return;
            }
        }
        
        SetDataflowNode(node, GetNode(binding));
    }
    
    @Override
    public void endVisit(QualifiedName node)
    {
        super.endVisit(node);
        IBinding binding = node.resolveBinding();
        if (binding instanceof IVariableBinding)
        {
            IVariableBinding varbinding = (IVariableBinding)binding;
            if (varbinding.isField())
            {
                Node object = GetDataflowNode(node.getQualifier());
                Node expr = object.getField(varbinding);
                current_method.graph.AddControlFlowEdge(object, expr);
                SetDataflowNode(node, expr);
                return;
            }
        }
        SetDataflowNode(node, GetNode(binding));
    }

    @Override
    public void endVisit(ArrayAccess node)
    {
        super.endVisit(node);
        Node exp = new ExpressionNode(node);
        Node array = GetDataflowNode(node.getArray());
        Node args = GetDataflowNode(node.getIndex());
        current_method.graph.AddControlFlowEdge(array, exp);
        current_method.graph.AddControlFlowEdge(args, exp);
        SetDataflowNode(node, exp);
    }

    @Override
    public void endVisit(ArrayInitializer node)
    {
        super.endVisit(node);
        Node exp = new ExpressionNode(node);
        for (Object element : node.expressions())
            current_method.graph.AddControlFlowEdge(GetDataflowNode((Expression)element), exp);
        SetDataflowNode(node, exp);        
    }

    @Override
    public void endVisit(CastExpression node)
    {
        super.endVisit(node);
        SetDataflowNode(node, GetDataflowNode(node.getExpression()));
    }

    @Override
    public void endVisit(InstanceofExpression node)
    {
        super.endVisit(node);
        ExpressionNode exp = new ExpressionNode(node);
        Node child = GetDataflowNode(node.getLeftOperand());
        current_method.graph.AddControlFlowEdge(child, exp);
        SetDataflowNode(node, exp);
    }

    @Override
    public void endVisit(ParenthesizedExpression node)
    {
        super.endVisit(node);
        SetDataflowNode(node, GetDataflowNode(node.getExpression()));
    }

    @Override
    public void endVisit(PostfixExpression node)
    {
        super.endVisit(node);
        ExpressionNode exp = new ExpressionNode(node);
        Node child = GetDataflowNode(node.getOperand());
        current_method.graph.AddControlFlowEdge(child, exp);
        SetDataflowNode(node, exp);        
    }

    @Override
    public void endVisit(PrefixExpression node)
    {
        super.endVisit(node);
        ExpressionNode exp = new ExpressionNode(node);
        Node child = GetDataflowNode(node.getOperand());
        current_method.graph.AddControlFlowEdge(child, exp);
        SetDataflowNode(node, exp);
    }

    @Override
    public void endVisit(InfixExpression node)
    {
        super.endVisit(node);
        ExpressionNode exp = new ExpressionNode(node);
        Node lhs = GetDataflowNode(node.getLeftOperand());
        Node rhs = GetDataflowNode(node.getRightOperand());
        current_method.graph.AddControlFlowEdge(lhs, exp);
        current_method.graph.AddControlFlowEdge(rhs, exp);
        for (Object operand : node.extendedOperands())
            current_method.graph.AddControlFlowEdge(GetDataflowNode((Expression)operand), exp);
        SetDataflowNode(node, exp);
    } 

    @Override
    public void endVisit(ReturnStatement node)
    {
        super.endVisit(node);
        Node returnValue = GetDataflowNode(node.getExpression());
         current_method.graph.AddDataFlowEdge(returnValue, current_method.context.return_value);
    } 

    @Override
    public void endVisit(SingleVariableDeclaration node)
    {
        super.endVisit(node);
        Node lhs = GetNode(node.resolveBinding());
        if (node.getInitializer() != null)
        {
            Node rhs = GetDataflowNode(node.getInitializer());
            current_method.graph.AddDataFlowEdge(rhs, lhs);
        }
    }

    // --------------------------------------------------------------------------------------------------------------
    // Member expressions
    // --------------------------------------------------------------------------------------------------------------    
    

    @Override
    public void endVisit(FieldAccess node)
    {
        super.endVisit(node);
        Node obj = GetDataflowNode(node.getExpression());
        FieldNode field = obj.getField(node.resolveFieldBinding());
        SetDataflowNode(node, field);
    } 
    
    @Override
    public void endVisit(SuperFieldAccess node)
    {   
        super.endVisit(node);
        FieldNode field = this_node.getField(node.resolveFieldBinding());
        SetDataflowNode(node, field);        
    }     
    
    @Override
    public void endVisit(ArrayCreation node)
    {
        super.endVisit(node);
        
        List<Node> args = new ArrayList<Node>();
        for (Object arg : node.dimensions())
            args.add(GetDataflowNode((Expression)arg));
           
        Node expr = new ExpressionNode( node);   
        current_method.graph.AddDataFlowEdge(GetNode(node.getType().resolveBinding()), expr);    
        SetDataflowNode(node, expr);       
    }    
    
    @Override
    public void endVisit(ClassInstanceCreation node)
    {
        super.endVisit(node);
        
        List<Node> args = new ArrayList<Node>();
        for (Object arg : node.arguments())
            args.add(GetDataflowNode((Expression)arg));
           
        Node expr = new ExpressionNode( node);   
        current_method.graph.AddDataFlowEdge(GetNode(node.resolveConstructorBinding().getDeclaringClass()), expr);    
        current_method.graph.AddMethodCallNode(node, node.resolveConstructorBinding(), null, args, expr, null);           
        SetDataflowNode(node, expr);    
    }
    
    @Override
    public void endVisit(MethodInvocation node)
    {
        super.endVisit(node);

        Node recv;
        if (node.getExpression() != null)
            recv = GetDataflowNode(node.getExpression());
        else
            recv = this_node;
        
        List<Node> args = new ArrayList<Node>();
        for (Object arg : node.arguments())
            args.add(GetDataflowNode((Expression)arg));
        
        Node expr = new ExpressionNode(node);
        IMethodBinding binding = node.resolveMethodBinding();
        current_method.graph.AddMethodCallNode(node,  binding, recv, args, expr, null); 
        SetDataflowNode(node, expr);         
    }  

    @Override
    public void endVisit(ConstructorInvocation node)
    {
        super.endVisit(node);
        List<Node> args = new ArrayList<Node>();        
        for (Object arg : node.arguments())
            args.add(GetDataflowNode((Expression)arg));
        
        Node expr = new ExpressionNode(node);
        current_method.graph.AddMethodCallNode(node,  node.resolveConstructorBinding(), this_node, args, expr, null); 
        SetDataflowNode(node, expr);   
    }    
    
    @Override
    public void endVisit(SuperConstructorInvocation node)
    {
        super.endVisit(node);
        List<Node> args = new ArrayList<Node>();        
        for (Object arg : node.arguments())
            args.add(GetDataflowNode((Expression)arg));
        
        Node expr = new ExpressionNode(node);
        current_method.graph.AddMethodCallNode(node,  node.resolveConstructorBinding(), this_node, args, expr, null); 
        SetDataflowNode(node, expr);           
    }
    
    @Override
    public void endVisit(SuperMethodInvocation node)
    {
        super.endVisit(node);
        List<Node> args = new ArrayList<Node>();
        for (Object arg : node.arguments())
            args.add(GetDataflowNode((Expression)arg));
        
        Node expr = new ExpressionNode(node);
        current_method.graph.AddMethodCallNode(node,  node.resolveMethodBinding(), this_node, args, expr, null); 
        SetDataflowNode(node, expr);          
    }

    @Override
    public void endVisit(ThisExpression node)
    {
        super.endVisit(node);
        SetDataflowNode(node, this_node);
    }    
    
    // --------------------------------------------------------------------------------------------------------------
    // Literals 
    // --------------------------------------------------------------------------------------------------------------
    
    @Override
    public void endVisit(TypeLiteral node)
    {
        super.endVisit(node);
        SetDataflowNode(node, new ExpressionNode(node.resolveTypeBinding()));          
    }    
    
    @Override
    public void endVisit(BooleanLiteral node)
    {
        super.endVisit(node);
        SetDataflowNode(node, new ExpressionNode(node.booleanValue()));  
    }

    @Override
    public void endVisit(CharacterLiteral node)
    {
        super.endVisit(node);
        SetDataflowNode(node, new ExpressionNode(node.charValue()));  
    }      
    
    @Override
    public void endVisit(NullLiteral node)
    {
        super.endVisit(node);
        SetDataflowNode(node, new ExpressionNode(node));         
    }

    @Override
    public void endVisit(NumberLiteral node)
    {
        super.endVisit(node);
        SetDataflowNode(node, new ExpressionNode(node.getToken()));        
    }   
    
    @Override
    public void endVisit(StringLiteral node)
    {
        super.endVisit(node);
        SetDataflowNode(node, new ExpressionNode(node.getLiteralValue()));       
    }    
    
    // ------------------------------------------------------------------------------------------------------------------------------
    // Declarations 
    // ------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void endVisit(VariableDeclarationStatement node)
    {
        super.endVisit(node);
        for (Object var : node.fragments())
        {
            VariableDeclarationFragment frag = (VariableDeclarationFragment) var;
            Node lhs = GetNode(frag.resolveBinding());
            if (frag.getInitializer() != null)
            {
                Node rhs = GetDataflowNode(frag.getInitializer());
                current_method.graph.AddDataFlowEdge(rhs, lhs);          
            }           
        }
    }
    
    @Override
    public boolean visit(MethodDeclaration node)
    {
        current_method = new MethodFoo();
        IMethodBinding binding = node.resolveBinding();
        methods.put(binding, current_method);

        for (Object  p : node.parameters())
        {
            SingleVariableDeclaration param = (SingleVariableDeclaration)p;
            current_method.context.args.add(GetNode(param.resolveBinding()));
        }
        
        current_method.context.recv = this_node = new LocalNode("this");
        current_method.context.return_value = new LocalNode("return");       
        
        return super.visit(node);
    }
    
    @Override
    public void endVisit(MethodDeclaration node)
    {
        super.endVisit(node);              
        current_method = null;
        this_node = null;
    }

    @Override
    public void postVisit(ASTNode node)
    {
        // TODO Auto-generated method stub
        super.postVisit(node);
        if (node instanceof Expression)
            GetDataflowNode(node); // test that we have created a node for every expression
    }
}