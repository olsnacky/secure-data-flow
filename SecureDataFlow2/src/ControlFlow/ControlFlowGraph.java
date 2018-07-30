package ControlFlow;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.*;

import FlowGraph.Graph;
import QUT.DataflowVisitor;

public class ControlFlowGraph
{
    public CompilationUnit cu;
    public Statement end_of_method, start_of_method;
    private Map<Statement, List<Statement>> predecessors = new HashMap<Statement, List<Statement>>();
    private Map<Statement, List<Statement>> successors = new HashMap<Statement, List<Statement>>();
    private IMethodBinding method;
    private Graph graph;
    
    public ControlFlowGraph(IMethodBinding method, Graph graph)
    {
        this.method = method;
        this.graph = graph;
    }
    
    private boolean isSpecial(Statement stmt)
    {
        return false; //stmt == start_of_method || stmt == end_of_method;
    }
   
    private String Dump(ASTNode stmt)
    {
        if (stmt == start_of_method)
            return "start of method";
        if (stmt == end_of_method)
            return "end of method";
        
        int pos = stmt.getStartPosition();
        return stmt.getClass().getSimpleName()+ "(" + cu.getLineNumber(pos) + "," + cu.getColumnNumber(pos) + ")";
    }
    
    public void Dump()
    {
        for (Map.Entry<Statement, List<Statement>> entry : successors.entrySet())
            for (Statement dest : entry.getValue())
                System.out.println(Dump(entry.getKey()) + " => " + Dump(dest));
        
        for (Map.Entry<Statement, List<Statement>> entry : predecessors.entrySet())
            for (Statement src : entry.getValue())
                System.out.println(Dump(entry.getKey()) + " <= " + Dump(src));        
        
        System.out.println("------------------------------------------------------------------------------");
    }

    public void AddControlFlowEdge(Statement src, Statement dest)
     {
        //System.out.println("AddControlFlowEdge("+Dump(src)+", " + Dump(dest)+")");
        if (!predecessors.containsKey(src))
            predecessors.put(src, new ArrayList<Statement>());

        if (!successors.containsKey(src))
            successors.put(src, new ArrayList<Statement>());

        if (!predecessors.containsKey(dest))
            predecessors.put(dest, new ArrayList<Statement>());

        if (!successors.containsKey(dest))
            successors.put(dest, new ArrayList<Statement>());

        if (!predecessors.get(dest).contains(src))
            predecessors.get(dest).add(src);

        if (!successors.get(src).contains(dest))
            successors.get(src).add(dest);
        
        //Dump();
    }

    public void AddControlFlowEdges(Collection<Statement> src, Statement dest)
    {
        for (Statement s : src)
            AddControlFlowEdge(s, dest);
    }
   

    public void AnalyseControlFlow(CompilationUnit cu) 
    {
        this.cu = cu;
        //Dump();
        PostOrderStatements();
        //Dump();
        UnreachableCodeElimination();
        //Dump();
        CalculateDominators();
        CalculateControlDependencies();
    }

    // --------------------------- Post Order --------------------------------

    private List<Statement> nodes = new ArrayList<Statement>();
    private List<Statement> reverse_postorder = new ArrayList<Statement>();
    private Map<Statement, Integer> postorder_number = new Hashtable<Statement, Integer>();

    private void PostOrderStatements()
    {
        Stack<Statement> child = new Stack<Statement>();
        Stack<Statement> parent = new Stack<Statement>();

        //Dump();
        
        child.push(end_of_method);

        while (child.size() > 0)
        {
            Statement node = child.pop();

            if (!nodes.contains(node))
            {
                if (!isSpecial(node))
                {
                    nodes.add(node);
                    parent.push(node);
                }

                if (predecessors.containsKey(node))
                    for (Statement pred : predecessors.get(node))
                        child.push(pred);
            }
        }

        int post_order_number = 0;
        while (parent.size() > 0)
        {
            Statement node = parent.pop();
            reverse_postorder.add(0, node);
            postorder_number.put(node, post_order_number++);
        }
     
        //System.out.println("postorder_numbers:");
        //for (Map.Entry<Statement, Integer> entry: postorder_number.entrySet())
        //    System.out.println("  " + entry.getValue() + ": " + Dump(entry.getKey()));
    }

    private void UnreachableCodeElimination()
    {
        //System.out.println("UnreachableCodeElimination()");
        
        //System.out.print("nodes:");
        //for (Statement node : nodes)
        //    System.out.print(Dump(node) + "; ");
        //System.out.println();
        
        List<Statement> dead = new ArrayList<Statement>();
        for (Map.Entry<Statement, List<Statement>> dest : successors.entrySet())
        {
            //System.out.println(Dump(dest.getKey()) + " => " + dest.getValue().size());
            
            //System.out.print("nodes:");
            //for (Statement node : nodes)
            //    System.out.print(Dump(node) + "; ");
            //System.out.println();            
            
            if (!nodes.contains(dest.getKey()))
            {
                dead.add(dest.getKey());
                //System.out.println("add to dead list " + Dump(dest.getKey()));
            }
            
            for (int i = 0; i < dest.getValue().size(); i++)
            {
                Statement src = dest.getValue().get(i);
                if (!nodes.contains(src))
                {
                    dest.getValue().remove(src);
                    i--;
                }
            }
        }

        for (Statement p : dead)
        {
            //System.out.println("remove dead code " + Dump(p));
            successors.remove(p);
        }
    }

    // ---------------------------Dominators -----------------------------

    Map<Statement, Statement> dom = new HashMap<Statement, Statement>();

    private void CalculateDominators()
    {
        //System.out.println("CalculateDominators:");
        
        for (Statement node : nodes)
        {
            dom.put(node, null);
            //System.out.println("  " + Dump(node) + " dominates nothing");
        }
        
        Comparator<Statement> cmp = new StatementSorter(postorder_number);

        dom.put(end_of_method, end_of_method);
        for (Statement b : reverse_postorder)
        {
            //System.out.println("In reverse post order consider node " + Dump(b));
            SortedSet<Statement> queue = new TreeSet<Statement>(cmp);
            SortedSet<Statement> seen = new TreeSet<Statement>(cmp);
            for (Statement p : successors.get(b))
            {
                //System.out.println("  " + Dump(p) + " is a successor of " + Dump(b) + ", add it to the queue");
                queue.add(p);
                seen.add(p);
            }
            //System.out.println(queue.size() + " items in queue");
            while (queue.size() > 1)
            {
                Statement top = queue.first();
                queue.remove(top);
                //System.out.println("remove top item from queue " + Dump(top));
                for (Statement p : successors.get(top))
                {
                    //System.out.println("  " + Dump(p) + " is a successor of " + Dump(top));
                    if (!seen.contains(p))
                    {
                        //System.out.println("add it to the queue and seen list");
                        queue.add(p);
                        seen.add(p);
                    }
                }
            }
            //System.out.println("exit from queue loop with " + queue.size() + " items remaining");
            if (!queue.isEmpty() && !isSpecial(queue.first()))
            {      
                dom.put(b, queue.first());
                //System.out.println(Dump(b) + " is dominated by " + Dump(queue.first()));
            }
        }
    }

    public class StatementSorter implements Comparator<Statement>
    {
        private Map<Statement, Integer> postorder_number; 
        public StatementSorter(Map<Statement, Integer> postorder_number)
        {
            this.postorder_number = postorder_number;
        }
        public int compare(Statement o1, Statement o2)
        {
            return postorder_number.get(o1) - postorder_number.get(o2);
        }
    }

    // ------------------------ Control Dependencies --------------------------

    private void CalculateControlDependencies()
    {
        for (Map.Entry<Statement, List<Statement>> edge : successors.entrySet())
        {
            Comparator<Statement> cmp = new StatementSorter(postorder_number);

            dom.put(end_of_method, end_of_method);
            for (Statement x : reverse_postorder)
            {
                if (dom.get(x) != null)
                {
                    SortedSet<Statement> queue = new TreeSet<Statement>(cmp);
                    SortedSet<Statement> seen = new TreeSet<Statement>(cmp);
                    
                    for (Statement p : successors.get(x))
                    {
                        if (p != dom.get(x))
                        {
                            queue.add(p);
                            seen.add(p);
                        }
                    }
                    
                    while (queue.size() > 0)
                    {
                        Statement y = queue.first();
                        queue.remove(y);
                        AddControlDependence(x, y);
                        for (Statement p : successors.get(y))
                        {
                            if (p != dom.get(x) && !seen.contains(p))
                            {
                                queue.add(p);
                                seen.add(p);
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<Expression, List<ASTNode>> controlDeps = new HashMap<Expression, List<ASTNode>>();
    
    private void AddControlDependence(Expression src, ASTNode dst)
    {
        if (!controlDeps.containsKey(src))
            controlDeps.put(src,  new ArrayList<ASTNode>());
        
        if (!controlDeps.get(src).contains(dst))
            controlDeps.get(src).add(dst);
    }
    
    private void PropagateDown(Expression src, ASTNode dstRoot)
    {
        //System.out.println("PropagateDown(" + Dump(src) + "," + Dump(dstRoot) + ")");
        ExpressionVisitor visitor = new ExpressionVisitor(src, graph);
        dstRoot.accept(visitor);
    }
    
    private void AddControlDependence(Statement src, Statement dst)
    {
        //System.out.println(Dump(src) + " ~~> " + Dump(dst));        
        
        Expression srcExpr;
        switch (src.getClass().getSimpleName())
        {
        case "IfStatement":
            srcExpr = ((IfStatement)src).getExpression();
            break;
        case "ForStatement":
            srcExpr = ((ForStatement)src).getExpression();
            break;
//        case "EnhancedForStatement":
//        	srcExpr = ((EnhancedForStatement)src).getExpression();
//            break;
        case "WhileStatement":
            srcExpr = ((WhileStatement)src).getExpression();
            break;
        case "SwitchStatement":
            srcExpr = ((SwitchStatement)src).getExpression();
            break;
        case "EmptyStatement": // Fixme: special entry statement???
            return;
        default:
            throw new RuntimeException("Unexpected source type " + src.getClass().getSimpleName());
        }
        
        switch (dst.getClass().getSimpleName())
        {
        case "ReturnStatement":
            AddControlDependence(srcExpr, ((ReturnStatement)dst).getExpression());
            break;
        case "ExpressionStatement":
            AddControlDependence(srcExpr, ((ExpressionStatement)dst).getExpression());
            break;
        case "WhileStatement":
            AddControlDependence(srcExpr, ((WhileStatement)dst).getExpression());
            break;
        case "VariableDeclarationStatement":
            for (Object frag : ((VariableDeclarationStatement)dst).fragments())
                AddControlDependence(srcExpr, (VariableDeclarationFragment)frag);   
            break;
        case "ForStatement":
            AddControlDependence(srcExpr, ((ForStatement)dst).getExpression());
            for (Object init : ((ForStatement)dst).initializers())
                AddControlDependence(srcExpr, (Expression)init);
            // Fixme: increment expressions
            break;
//        case "EnhancedForStatement":
//            AddControlDependence(srcExpr, ((EnhancedForStatement)dst).getExpression());
//            // Fixme: increment expressions
//            break; 
        case "SwitchCase":
        case "BreakStatement":
            break;
        default:
            throw new RuntimeException("Unexpected destination type " + dst.getClass().getSimpleName());            
        }
        
        for (Map.Entry<Expression, List<ASTNode>> entry : controlDeps.entrySet())
            for (ASTNode d : entry.getValue())
                PropagateDown(entry.getKey(), d);        
    }
}
