package FlowGraph;

import org.eclipse.jdt.core.dom.IPackageBinding;

public class PackageNode extends Node
{
    public PackageNode(IPackageBinding name)
    {
        super(name);
    }

    public String NodeChar()
    {
        return "Package";
    }
}
