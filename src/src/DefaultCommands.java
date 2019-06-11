
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Scanner;
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
            System.out.println(ex.getMessage());
            return;
        }
        System.out.println("Loaded:" + Arrays.toString(newRoot.childNames()));
    }
    
    public static void invokeAll(File initFile){
        Scanner sc = null;
        try {
            sc = new Scanner(initFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DefaultCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            try {
                CommandUtils.CURR_DIR = initFile.getParentFile().getAbsolutePath();
                CommandUtils.CURR_DIR += (CommandUtils.CURR_DIR.charAt(CommandUtils.CURR_DIR.length() - 1) == '\\'
                        || CommandUtils.CURR_DIR.charAt(CommandUtils.CURR_DIR.length() - 1) == '/' ? "" : "/");
                CommandUtils.invoke(line);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DefaultCommands.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CommandNotFoundException ex) {
                Logger.getLogger(DefaultCommands.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        CommandUtils.CURR_DIR = CommandUtils.COMMAND_LOC;
    }
    
    public static void loadf(String folderLocation){
        String fLoc = CommandUtils.COMMAND_LOC + folderLocation;
        File f = new File(fLoc);
        if(f.isDirectory() == false){
            throw new IllegalArgumentException("Location: \"" + fLoc + "\" is not a folder.");
        }
        // Get init file
        String initLoc = f.getAbsolutePath() + (
                f.getAbsolutePath().charAt(f.getAbsolutePath().length() - 1) == '\\' 
                || f.getAbsolutePath().charAt(f.getAbsolutePath().length() - 1) == '/'
                ? "" : "/") + CommandUtils.INIT_NAME;
        File init_file = new File(initLoc);
        if(init_file.exists() == false){
            throw new IllegalArgumentException("Initialization file doesn't exist in folder: \"" + fLoc + "\"");
        }
        invokeAll(init_file);
    }
    
    
    
    public static void ans(){
        System.out.println("Ans: null");
    }
    
    public static void cls(){
        for (int i = 0; i < 100; i++) {
            System.out.println("");
        }
    }
    
    
    public static void exit(){
        System.exit(0);
    }
    
}
