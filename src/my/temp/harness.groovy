package my.temp

import groovyx.sql.dsl.*

import java.security.CodeSource
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit

class TestHarnessClassLoader extends GroovyClassLoader {

    protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource codeSource) {

        CompilationUnit cu = super.createCompilationUnit(config, codeSource)
        cu.addPhaseOperation(new TestHarnessOperation(), Phases.CANONICALIZATION)
        return cu
    }
}

private class TestHarnessOperation extends PrimaryClassNodeOperation {

	public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        new SqlQueryASTTransformation().visit(null, source)
	}
}
//C:\VnsApplications\GroovyDSL\DatastoreQueryASTTesEx
def file = new File('C:\\VnsApplications\\GroovyDSL\\GroovySqlDsl\\src\\my\\temp\\doScript.groovy')
//                     C:\\VnsApplications\\GroovyDSL\\GroovySqlDsl
TestHarnessClassLoader loader = new TestHarnessClassLoader()
Class clazz = loader.parseClass(file)
Script script = (Script)clazz.newInstance();
script.run()