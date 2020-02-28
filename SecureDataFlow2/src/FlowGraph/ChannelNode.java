package FlowGraph;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;

public class ChannelNode extends ValueNode 
{
	private SecurityLevel securityLevel;
	
    public ChannelNode(Object name, IMethodBinding methodBinding, SecurityLevel securityLevel)
    {
    	this(name.toString(), methodBinding, securityLevel);
    }
    
    public ChannelNode(String name, IMethodBinding methodBinding, SecurityLevel securityLevel) {
        super(name, methodBinding);
        this.securityLevel = securityLevel;
    }

    public String NodeChar()
    {
        return "Channel";
    }
    
    public SecurityLevel getSecurityLevel() {
    	return this.securityLevel;
    }
}