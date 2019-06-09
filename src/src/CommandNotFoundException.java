
public class CommandNotFoundException extends Exception {

    public CommandNotFoundException(String passText) {
        super("Command: \"" + passText + "\" is not a valid command.");
    }

}
