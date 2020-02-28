package ControlFlow;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.*;

import FlowGraph.Graph;
import FlowGraph.MethodFoo;


public class ControlFlowVisitor extends ASTVisitor
{   
    private Map<ASTNode, ControlFlowBlock> blocks = new Hashtable<ASTNode, ControlFlowBlock>();
    private ControlFlowGraph CFG = null;
    private CompilationUnit cu;
    
    public ControlFlowVisitor(CompilationUnit cu)
    {
        this.cu = cu;    
    }
    
    // ------------------- Code Containers -------------------------
    @Override
    public boolean visit(MethodDeclaration node)
    {
        StartNewMethod(node.resolveBinding());
        return super.visit(node);
    }
    
    @Override
    public void endVisit(MethodDeclaration node)
    {
        super.endVisit(node);
        if (node.getBody() != null)
            AnalyseMethod(node, node.getBody());      
    }

    // ------------------- Control Statements -------------------------

    @Override
    public void endVisit(Block node)
    {
        // { Statements }
        super.endVisit(node);

        if (node.statements().size() > 0)
        {
            ControlFlowBlock parent = CreateBlock(node, blocks.get(node.statements().get(0)).head);

            for (int i = 0; i < node.statements().size(); i++)
            {
                ControlFlowBlock stmt = blocks.get(node.statements().get(i));
                
                if (i + 1 < node.statements().size()) {
                	Statement head = blocks.get(node.statements().get(i + 1)).head;
                    CFG.AddControlFlowEdges(stmt.last, head);
                }

                Propagate(stmt, parent, i == node.statements().size() - 1);
            }
        }
        else
            CreateBlock(node, null);
    }

    @Override
    public void endVisit(IfStatement node)
    {
        // if ( Condition ) Statement else Else
        super.endVisit(node);

        ControlFlowBlock parent = CreateBlock(node, node);

        ControlFlowBlock Then = blocks.get(node.getThenStatement());
        Propagate(Then, parent, true);
        CFG.AddControlFlowEdge(node, Then.head);

        if (node.getElseStatement() != null)
        {
            ControlFlowBlock Else = blocks.get(node.getElseStatement());
            Propagate(Else, parent, true);
            CFG.AddControlFlowEdge(node, Else.head);
        }
        else
            parent.last.add(node);
    }

    @Override
    public void endVisit(WhileStatement node)
    {
        // while ( Condition ) Statement ;
        super.endVisit(node);
        PreTestedLoop(node, node.getBody());
    }

    @Override
    public void endVisit(DoStatement node)
    {
        // do Statement while ( Condition ) ;
        super.endVisit(node);
        PostTestedLoop(node, node.getBody());
    }

    @Override
    public void endVisit(ForStatement node)
    {
        // for ( Declaration Initializers; Condition; Incrementors ) Statement
        super.endVisit(node);
        PreTestedLoop(node, node.getBody());
    }
    
//    @Override
//    public void endVisit(EnhancedForStatement node)
//    {
//        // for ( Declaration Initializers; Condition; Incrementors ) Statement
//        super.endVisit(node);
//        PreTestedLoop(node, node.getBody());
//    }

    @Override
    public void endVisit(SwitchStatement node)
    {
        // switch ( Expression ) { Sections }
        super.endVisit(node);

        ControlFlowBlock parent = CreateBlock(node, node);

        for (int i = 0; i < node.statements().size(); i++)
        {
            Statement s = (Statement)node.statements().get(i);

            if  (s instanceof SwitchCase)
                CFG.AddControlFlowEdge(node, s);            

            ControlFlowBlock stmt = blocks.get(s);            
            
            if (i + 1 < node.statements().size())
                CFG.AddControlFlowEdges(stmt.last, blocks.get(node.statements().get(i + 1)).head);

            parent.last.addAll(stmt.breaks);
            stmt.breaks.clear(); // don't propagate breaks further to outer structures   
            
            Propagate(stmt, parent, i == node.statements().size() - 1);
        }        
    }

    @Override
    public void endVisit(TryStatement node)
    {
        // try { Block } Catches Finally
        super.endVisit(node);

        ControlFlowBlock parent = Inherit(node.getBody());

        ControlFlowBlock Final = null;
        if (node.getFinally() != null)
        {
            Final = blocks.get(node.getFinally());
            Propagate(Final, parent, true);
            // branch from end of try block to start of finally block
            CFG.AddControlFlowEdges(blocks.get(node.getBody()).last, Final.head);
        }

        for (Object c : node.catchClauses())
        {
            CatchClause clause = (CatchClause)c;
            ControlFlowBlock catchBlock = blocks.get(clause.getBody());

            List<ThrowStatement> handled = new ArrayList<ThrowStatement>();
            if (clause.getException() != null)
            {
                ITypeBinding catchType = clause.getException().getType().resolveBinding();

                // branch from throw statements to catch block
                for (ThrowStatement t : blocks.get(node.getBody()).Throws)
                {
                    ITypeBinding throwType = t.getExpression().resolveTypeBinding();
                    if (catchType != null && throwType.isSubTypeCompatible(catchType)) // throw matches type of catch???
                    {
                        CFG.AddControlFlowEdge(t, catchBlock.head);
                        handled.add(t);
                    }
                }
                // branch from catch block to finally block
                if (node.getFinally() != null)
                    CFG.AddControlFlowEdges(catchBlock.last, Final.head);
            }
            else
                for (ThrowStatement t : blocks.get(node.getBody()).Throws)
                {
                    CFG.AddControlFlowEdge(t, catchBlock.head);
                    handled.add(t);
                }

            for (Object h : handled)
                blocks.get(node.getBody()).Throws.remove(h);

            Propagate(catchBlock, parent, node.getFinally() == null);
        }
    }

    @Override
    public void endVisit(LabeledStatement node)
    {
        // Identifier : Statement
        super.endVisit(node);
        Inherit(node);
    }

    // ------------------- Branch Statements -------------------------   
    
    @Override
    public void endVisit(ReturnStatement node)
    {
        // return Expression ;
        super.endVisit(node);
        BranchNode(node).returns.add(node);
    }

    @Override
    public void endVisit(BreakStatement node)
    {
        // break;
        super.endVisit(node);
        BranchNode(node).breaks.add(node);
    }

    @Override
    public void endVisit(ContinueStatement node)
    {
        // continue;
        super.endVisit(node);
        BranchNode(node).continues.add(node);
    }

    @Override
    public void endVisit(ThrowStatement node)
    {
        // throw Expression ;
        super.endVisit(node);
        BranchNode(node).Throws.add(node);
    }

    // ------------------- Leaf Statements -------------------------

    @Override
    public void endVisit(SwitchCase node)
    {
        super.endVisit(node);
        SimpleNode(node);
    } 
    
    @Override
    public void endVisit(ExpressionStatement node)
    {
        // Expression ;
        super.endVisit(node);
        SimpleNode(node);
    }
    
    @Override
    public void endVisit(SuperConstructorInvocation node)
    {
        super.endVisit(node);
        SimpleNode(node);
    }    

    @Override
    public void endVisit(ConstructorInvocation node)
    {
        super.endVisit(node);
        SimpleNode(node);
    }     
    
    @Override
    public void endVisit(VariableDeclarationStatement node)
    {
        // Modifiers [const] [fixed] Declaration ;
        super.endVisit(node);
        SimpleNode(node);
    }

    @Override
    public void endVisit(EmptyStatement node)
    {
        // ;
        super.endVisit(node);
        SimpleNode(node);
    }

    // ------------------- Compound Statements -------------------------

    @Override
    public void endVisit(SynchronizedStatement node)
    {
        // lock ( Expression ) Statement
        super.endVisit(node);
        Inherit(node.getBody());
    }  
    
// -----------------------------------------------------------------------------------------
    
    private void StartNewMethod(IMethodBinding methodBinding)
    {
        System.out.println("StartNewMethod " + methodBinding);
        MethodFoo method = QUT.DataflowVisitor.methods.get(methodBinding);
        CFG = new ControlFlowGraph(methodBinding, method);
        CFG.cu = cu;
        blocks.clear();        
    }
    
    private void SimpleNode(Statement node)
    {
        CreateBlock(node, node).last.add(node);
    }   
    
    private ControlFlowBlock Inherit(Statement node)
    {
        ControlFlowBlock child = blocks.get(node);
        ControlFlowBlock parent = CreateBlock((Statement)node.getParent(), child.head);
        Propagate(child, parent, true);
        return parent;
    }
    
    private void PreTestedLoop(Statement loop, Statement body)
    {
        ControlFlowBlock parent = CreateBlock(loop, loop);
        ControlFlowBlock child = blocks.get(body);

        // breaks within the loop are exists from that loop.
        parent.last.addAll(child.breaks);
        child.breaks.clear(); // don't propagate breaks further to outer loops

        // add branches from nested continue statements to the start of the loop
        CFG.AddControlFlowEdges(child.continues, loop);
        child.continues.clear(); // don't propagate continues further to outer loops

        Propagate(child, parent, false);

        // branch from condition to loop body
        CFG.AddControlFlowEdge(loop, child.head);

        // can skip the body
        parent.last.add(loop);

        // loop back to the top
        CFG.AddControlFlowEdges(child.last, loop);        
    }

    private void PostTestedLoop(Statement loop, Statement body)
    {
        ControlFlowBlock child = blocks.get(body);
        ControlFlowBlock parent = CreateBlock(loop, child.head);

        // breaks within the loop are exists from that loop.
        parent.last.addAll(child.breaks);
        child.breaks.clear(); // don't propagate breaks further to outer loops

        // add branches from nested continue statements to the start of the loop
        CFG.AddControlFlowEdges(child.continues, loop);
        child.continues.clear(); // don't propagate continues further to outer loops

        Propagate(child, parent, false);
        
        // branch from end of loop to condition
        CFG.AddControlFlowEdges(child.last, loop);

        parent.last.add(loop);

        // loop back to the top
        CFG.AddControlFlowEdge(loop, child.head);        
    }
    
    private void AnalyseMethod(MethodDeclaration method, Block methodBody)
    {
        ControlFlowBlock body = blocks.get(methodBody);

        assert(body.continues.size() == 0 && body.breaks.size() == 0);

        CFG.end_of_method = methodBody.getAST().newEmptyStatement();
        CFG.start_of_method = methodBody.getAST().newEmptyStatement();
        
        if (body.head != null)
            CFG.AddControlFlowEdge(CFG.start_of_method, body.head);
        CFG.AddControlFlowEdge(CFG.start_of_method, CFG.end_of_method);
        CFG.AddControlFlowEdges(body.returns, CFG.end_of_method);
        CFG.AddControlFlowEdges(body.last, CFG.end_of_method);

        CFG.AnalyseControlFlow(cu);        
        
        Graph method_graph = QUT.DataflowVisitor.methods.get(method.resolveBinding()).graph;
        //method_graph.Closure();
        //method_graph.Print();
    }
    
    private ControlFlowBlock CreateBlock(Statement node, Statement head)
    {
        ControlFlowBlock block =  new ControlFlowBlock(head);
        blocks.put(node, block);
        return block;
    }   
    
    private void Propagate(ControlFlowBlock src, ControlFlowBlock dst, boolean last)
    {
        dst.Throws.addAll(src.Throws);
        dst.returns.addAll(src.returns);
        dst.breaks.addAll(src.breaks);
        dst.continues.addAll(src.continues);
        if (last)
            dst.last.addAll(src.last);
    }    
    
    private ControlFlowBlock BranchNode(Statement node)
    {
        return CreateBlock(node, node);
    }    
}
