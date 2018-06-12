package ControlFlow;

import org.eclipse.jdt.core.dom.*;
import java.util.*;

public class ControlFlowBlock
{
    public Statement head = null;
    public List<Statement> last = new ArrayList<Statement>();
    public List<Statement> returns = new ArrayList<Statement>();
    public List<Statement> breaks = new ArrayList<Statement>();
    public List<Statement> continues = new ArrayList<Statement>();
    public List<ThrowStatement> Throws = new ArrayList<ThrowStatement>();

    public ControlFlowBlock(Statement head)
    {
        this.head = head;
    }
}
