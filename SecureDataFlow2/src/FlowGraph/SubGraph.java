package FlowGraph;

public class SubGraph
{
    public MethodContext context = new MethodContext();
    public SubGraph caller;
    public MethodFoo method;
    public Object callpoint;

    public SubGraph(MethodFoo method, Object callpoint, SubGraph caller)
    {
        this.caller = caller;
        this.method = method;
        this.callpoint = callpoint;
    }

    public SubGraph GetContour(MethodFoo method, MethodCall method_invocation)
    {
        if (method == this.method && callpoint == method_invocation.callpoint)
            return this;
        else if (caller != null)
            return caller.GetContour(method, method_invocation);
        else
            return null;
    }
}
