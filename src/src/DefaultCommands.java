
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DefaultCommands {

    public static void help(){
        CommandNode[] children = CommandUtils.ROOT.children;
        for(CommandNode child : children){
            System.out.println(child.name + " - " + child.description);
        }
    }
    
    public static void helpCommand(CommandNode com) throws ClassNotFoundException, CommandNotFoundException{
        if(com.location != null){
            Method m = CommandUtils.getMethod(com.location);
            Class[] clazzes = m.getParameterTypes();
            System.out.println(com.description+ " : " + Arrays.toString(clazzes));
        }else{
            System.out.println(com.description);
        }
    }
    
    public static void loadc(String fileLocation){
        System.out.println("Loading:" + fileLocation);
        CommandNode newRoot = null;
        try {
            newRoot = CommandUtils.loadCommands(fileLocation);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DefaultCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Loaded!");
    }
    
    public static void ans(){
        System.out.println("Ans: null");
    }
    
    
    public static void exit(){
        System.exit(0);
    }
    
}
