package securedataflow2.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jface.dialogs.MessageDialog;

import QUT.*;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public SampleHandler() {
	}

    private void AnalyseCompilationUnit(ICompilationUnit unit) throws JavaModelException
    {
        ASTParser parser = ASTParser.newParser(AST.JLS10);
        parser.setResolveBindings(true);
        parser.setSource(unit);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        
        QUT.Analyzer analyzer = new QUT.Analyzer();
        analyzer.Analyze(parser);
    }
    
  public Object execute(ExecutionEvent event) throws ExecutionException
  {
      try
      {
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())   
        {
            for (IPackageFragment mypackage : JavaCore.create(project).getPackageFragments())
            {     
                for (ICompilationUnit unit : mypackage.getCompilationUnits())
                {
                    System.out.println("project " + project.getName() + ", package " + mypackage.getElementName() + ", unit " + unit.getElementName());
                    AnalyseCompilationUnit(unit);                   
                }
            }
        }
        QUT.DataflowVisitor.Closure();
        System.out.println("Completely done !!!!");
      }
      catch (Exception e)
      {
          System.err.println("Exception " + e);
      }
    return null;
  }
}
