package FlowGraph;

import java.util.List;

public abstract class Edge implements java.util.Comparator<Edge>
{
    public Node src, dest;
    public List why;
    public int id;
    public static int nextId = 0;

    public Edge(Node src, Node dest, List why)
    {
        this.src = src;
        this.dest = dest;
        this.why = why;
        this.id = nextId++;
    }
    
    public String dotty()
    {
        return "\t" + src.dotty() + " -> " + dest.dotty();
    } 

    protected abstract String arrow();
    
    public boolean isExternal(MethodContext context) {
    	return !src.equals(dest) && src.isExternalNode(context) && dest.isExternalNode(context);
    }
    
//    public boolean shouldBeExternal(MethodContext context) {
//    	return src.isExternalNode(context) && !dest.isExternalNode(context);
//    }

    public String toString()
    {
//        return "\t" + src + " " + arrow() + " " + dest;
    	return src + " " + arrow() + " " + dest;
    }

    public void Explain(int indent)
    {
        for (int i = 0; i < indent; i++)
            System.out.print("    ");

        System.out.println(this);

        if (why == null)
            return;
        else if (why instanceof Edge)
        {
            Edge item = (Edge)why;
            item.Explain(indent + 1);
        }
        else
        {
            throw new UnsupportedOperationException();
            /*
            for (int i = 1; i < 10; i++)
            {
                Object prop = why.getClass().GetProperty("Item" + i);
                if (prop != null)
                {
                    Edge item = (Edge)prop.GetValue(why, null);
                    item.Explain(indent + 1);
                }  
            }
            */
        }
    }
    
    @Override
    public int compare(Edge e1, Edge e2)
    {
        int res = e1.src.CompareTo(e2.src);
        if (res == 0)
            return e1.dest.CompareTo(e2.dest);
        return res;
    }
    
    public boolean correspondsTo(Edge contractEdge, MethodContext impContext, MethodContext conContext) {
    	return this.src.mapsTo(contractEdge.src, impContext, conContext) && this.dest.mapsTo(contractEdge.dest, impContext, conContext) &&
    			this.getClass() == contractEdge.getClass();
    }
}

