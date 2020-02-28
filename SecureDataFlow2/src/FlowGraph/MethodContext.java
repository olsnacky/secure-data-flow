package FlowGraph;

import java.util.List;
import java.util.ArrayList;

public class MethodContext
{
    public Node recv;
    public List<Node> args = new ArrayList<Node>();
    public Node return_value;
    public Node entry;
}
