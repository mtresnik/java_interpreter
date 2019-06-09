
public class CommandNotFoundException extends Exception {

    public CommandNotFoundException(String passText) {
        super("Command: \"" + passText + "\" cannot be found.");
    }

}
