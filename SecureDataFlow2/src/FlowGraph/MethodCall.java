package FlowGraph;

import java.util.*;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

public class MethodCall
{
    public SubGraph subgraph;

    public Node recv;
    public IMethodBinding method;
    public List<Node> args;
    public Node return_value;
    public ASTNode callpoint;
    public List<ITypeBinding> already_expanded = new ArrayList<ITypeBinding>();

    public MethodCall(ASTNode callpoint, Node recv, IMethodBinding method, List<Node> args, Node return_value)
    {
        this.method = method;
        this.recv = recv;
        this.args = args;
        this.callpoint = callpoint;
        this.return_value = return_value;
    }
    
    private String methodName()
    {
        if (method.isConstructor())
            return "<init>";
        else
            return method.getName();
    }

    public String dotty()
    {
            return "\t" + recv + "." + methodName() + "(" +MethodCall.toString(args) + ") posn:" + callpoint.getStartPosition();
    }    
    
    public String toString()
    {
            return "\t" + recv + "." + methodName() + "(" +MethodCall.toString(args) + ") posn:" + callpoint.getStartPosition();
    }

    private static String toString(List<Node> args)
    {
        StringBuilder builder = new StringBuilder();
        for (Node a : args)
        {
            if (builder.length() > 0)
                builder.append(",");
            builder.append(a);
        }
        return builder.toString();
    }
}

