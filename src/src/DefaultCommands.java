
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
            if(child.location == null){
                System.out.println("\t" + Arrays.toString(child.childNames()));
            }
        }
    }
    
    public static void helpCommand(String pathText, CommandNode com) throws ClassNotFoundException, CommandNotFoundException{
        if(com.location != null){
            Method m = CommandUtils.getMethod(com.location);
            Class[] clazzes = m.getParameterTypes();
            System.out.println(pathText + " : " + Arrays.toString(clazzes) + " - " + com.description);
        }else{
            System.out.println(com.description);
            for(CommandNode child : com.children){
                System.out.println("\t["+ child.name +":" + child.description +"]");
            }
        }
    }
    
    public static void loadc(String fileLocation){
        CommandNode newRoot = null;
        try {
            newRoot = CommandUtils.loadCommands(fileLocation);
        } catch (FileNotFoundException ex) {
            System.out.println("File at location: \"" + fileLocation + "\" cannot be found.");
            return;
        }
        System.out.println("Loaded:" + Arrays.toString(newRoot.childNames()));
    }
    
    public static void ans(){
        System.out.println("Ans: null");
    }
    
    
    public static void exit(){
        System.exit(0);
    }
    
}
