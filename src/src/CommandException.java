
public class CommandException extends Exception{
    public static final CommandException WRONG_SYNTAX = new CommandException("The syntax you've entered is incorrect.");
    
    public CommandException(String eString){
        super(eString);
    }

}
