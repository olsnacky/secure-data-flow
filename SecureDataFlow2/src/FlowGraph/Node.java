package FlowGraph;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public abstract class Node implements java.util.Comparator<Node>
{
    public static int NEXTID = 0;
    public String name;
    public int id;

    public Map<IVariableBinding, FieldNode> fields = new Hashtable<IVariableBinding, FieldNode>();

    public Map<Node, DataFlowEdge> dataFlowEdges = new Hashtable<Node, DataFlowEdge>();
    public Map<Node, ControlFlowEdge> controlFlowEdges = new Hashtable<Node, ControlFlowEdge>();
    public Map<Node, PointsToSameEdge> pointsToEdges = new Hashtable<Node, PointsToSameEdge>();
    public Map<Node, AliasEdge> aliasEdges = new Hashtable<Node, AliasEdge>();

    public Node(IBinding binding)
    {
        this(binding.getName());
    }
    
    public Node(String name)
    {
        this.id = NEXTID++;
        this.name = name;
        /*this.subgraph = null;*/
    }

    public Node Clone(NodeMap map)
    {
        return this;
    }

    public FieldNode getField(IVariableBinding name)
    {
        if (!fields.containsKey(name))
        {
            FieldNode node = new FieldNode(this, name);
            fields.put(name, node);
            return node;
        }
        else
            return fields.get(name);
    }

    public void AddDataFlowSrc(DataFlowEdge edge)
    {
        dataFlowEdges.put(edge.dest, edge);
    }

    public void AddControlFlowSrc(ControlFlowEdge edge)
    {
        controlFlowEdges.put(edge.dest, edge);
    }

    public void AddPointsToSameSrc(PointsToSameEdge edge)
    {
        pointsToEdges.put(edge.dest, edge);
    }

    public void AddPointsToSameDest(PointsToSameEdge edge)
    {
        pointsToEdges.put(edge.src, edge);
    }

    public void AddAliasSrc(AliasEdge edge)
    {
        aliasEdges.put(edge.dest, edge);
    }

    public void AddAliasDest(AliasEdge edge)
    {
        aliasEdges.put(edge.src, edge);
    }

    public boolean IsDataFlowTo(Node dest)
    {
        return dataFlowEdges.containsKey(dest);
    }

    public boolean IsAlias(Node dest)
    {
        return aliasEdges.containsKey(dest);
    }

    public boolean IsControlFlowTo(Node dest)
    {
        return controlFlowEdges.containsKey(dest);
    }
    
    public boolean isHigh() {
    	return this.name.equals("HIGH");
    }
    
    public boolean isExternalNode(MethodContext context) {
    	// TODO: formal params
    	// TODO: return node
    	// TODO: object properties
    	return isHigh() || isLow() || context.args.contains(this);
    }
    
    public boolean isLow() {
    	return this.name.equals("LOW");
    }

    boolean PointsToSame(Node other)
    {
        return pointsToEdges.containsKey(other);
    }

    abstract public String NodeChar();

    public String dotty()
    {
        return String.format("n%04x", id);
    }
    
    public String toString()
    {
        return NodeChar() + ":" + name;
    }

    public int CompareTo(Node o)
    {
        return this.id - o.id;
    }

    @Override
    public int compare(Node o1, Node o2)
    {
        return o1.id - o2.id;
    }
    
    public boolean mapsTo(Node contractNode) {
    	if (this.isHigh()) {
    		return contractNode.isHigh();
    	} else if (this.isLow()) {
    		return contractNode.isLow();
    	}
    	
    	return false;
    }
}


