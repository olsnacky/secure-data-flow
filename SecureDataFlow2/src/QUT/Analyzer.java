package QUT;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Analyzer
{
    public void Analyze(ASTParser parser)
    {
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        
        System.out.println("Data flow analysis...");
        DataflowVisitor visitor = new DataflowVisitor();
        cu.accept(visitor); 
        //visitor.Dump();
        System.out.println("Control flow analysis ...");
        ControlFlow.ControlFlowVisitor visitor2 = new ControlFlow.ControlFlowVisitor(cu);
        cu.accept(visitor2);
    }
}
