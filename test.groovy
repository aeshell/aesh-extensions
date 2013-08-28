import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.Command;
import org.jboss.aesh.console.operator.ControlOperator;
import org.jboss.aesh.console.CommandResult;


@CommandDefinition(name = "test", description = "foo")
class test implements Command {

  def CommandResult execute(AeshConsole console, ControlOperator operator) {
    console.out().println("YAY");

    return CommandResult.SUCCESS;
  }

}
