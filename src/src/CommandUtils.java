
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public final class CommandUtils {

    public static boolean INITIALIZED = false;
    public static CommandNode ROOT = null, HELP = null;
    public static String ROOT_LOCATION = "root.xml";
    public static String COMMAND_LOC = "commands/";
    public static String CURR_DIR = COMMAND_LOC;
    public static final String INIT_NAME = "init.txt";
    
    static{
        if(INITIALIZED == false){
            try {
                System.out.println("Loading from \""
                        + CURR_DIR + "init.txt"
                        + "\" . . .");
                ROOT = CommandUtils.loadCommands(ROOT_LOCATION);
                DefaultCommands.invokeAll(new File(CURR_DIR + "init.txt"));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CommandUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
            INITIALIZED = true;
            if(HELP != null){
                HELP = getHelpNode();
            }
        }
    }
    
    
    
    public static final class TAGS{
        private TAGS(){}
        public static final String 
                COMMAND = "command", NAME = "name", 
                DESCRIPTION ="description", LOCATION = "location";
    }
    
    public static Method getMethod(String locationName) throws ClassNotFoundException, CommandNotFoundException{
        String className = locationName.substring(0, locationName.lastIndexOf("."));
        String methodName = locationName.substring(locationName.lastIndexOf(".") + 1);
        return getMethod(className, methodName);
    }
    
    public static Method getMethod(String className, String methodName) throws ClassNotFoundException, CommandNotFoundException{
        Class clazz = Class.forName(className);
        Method[] methods = clazz.getMethods();
        for(Method m : methods){
            if(m.getName().equals(methodName)){
                return m;
            }
        }
        throw new CommandNotFoundException(methodName);
    }
    
    
    public static CommandNode[] loadAllCommands(String ... fileLocations) throws FileNotFoundException{
        CommandNode[] retArr = new CommandNode[fileLocations.length];
        if(fileLocations.length == 0){
            return retArr;
        }
        for (int i = 0; i < fileLocations.length; i++) {
            String currLoc = fileLocations[i];
            retArr[i] = loadCommands(currLoc);
        }
        return retArr;
    }
    
    public static CommandNode loadCommands(String fileLocation) throws FileNotFoundException{
        File fXmlFile = new File(CURR_DIR + fileLocation);
        String retMessage = "File:" + fXmlFile.getAbsolutePath() + " cannot be found.";
        if(fXmlFile.exists() == false){
            throw new FileNotFoundException(retMessage);
        }
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        Document doc;
        try{
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);
        }catch(Exception e){
            throw new FileNotFoundException(retMessage);
        }
        Element rootElement = doc.getDocumentElement();
        CommandNode rootRep = new CommandNode(rootElement);
        makeTree(rootElement, rootRep);
        if(ROOT != null){
            ROOT = ROOT.join(rootRep);
        }
        return rootRep;
    }
    
    public static CommandNode getHelpNode(){
        for(CommandNode child : ROOT.children){
            if("help".equals(child.name)){
                return child;
            }
        }
        return null;
    }
    
    public static List<CommandNode> makeTree(Node tempRoot, CommandNode rootRep){
        List<CommandNode> retList = new ArrayList();
        List<Node> children = getChildren(tempRoot);
        if(children.isEmpty()){
            return retList;
        }
        List<Node> commands = filterNodesTag(children, TAGS.COMMAND);
        CommandNode[] commandChildren = new CommandNode[commands.size()];
        for(int i = 0; i < commandChildren.length; i++){
            CommandNode currRep = new CommandNode(commands.get(i));
            List<CommandNode> grandChildren = makeTree(commands.get(i), currRep);
            CommandNode[] grandArray = new CommandNode[grandChildren.size()];
            grandArray = grandChildren.toArray(grandArray);
            currRep.children = grandArray;
            currRep.init();
            retList.add(currRep);
            commandChildren[i] = currRep;
        }
        rootRep.children = commandChildren;
        return retList;
    }
    
    public static List<Node> filterNodesTag(List<Node> nodes, final String TAG){
        List<Node> retList = new ArrayList();
        for(Node n : nodes){
            if(n.getNodeName().equals(TAG)){
                retList.add(n);
            }
        }
        return retList;
    }
    
    public static List<Node> getChildren(Node parentNode){
        return getChildren((Element) parentNode);
    }
    
    public static List<Node> getChildren(Element parentElement){
        if(parentElement == null){
            return new ArrayList();
        }
        return nodeListToArr(parentElement.getChildNodes());
    }
    
    public static List<Node> nodeListToArr(NodeList nList){
        List<Node> retList = new ArrayList();
        for (int i = 0; i < nList.getLength(); i++) {
            Node currNode = nList.item(i);
            if(currNode.getNodeType() == Node.ELEMENT_NODE){
                retList.add(currNode);
            }
        }
        return retList;
    }
    
    
    public static Object invoke(String passText) throws ClassNotFoundException, CommandNotFoundException{
        try{
            return ROOT.invoke(passText);
        }catch(CommandNotFoundException iae){
            System.out.println(iae.getMessage());
            System.out.println("Type 'help' for a list of valid commands.");
        }catch(IllegalArgumentException iae){
            System.out.println(iae.getMessage());
            CommandNode actual = CommandNode.getPath(passText);
            Method m = getMethod(actual.location);
            System.out.println("Expected Parameters:" + Arrays.toString(m.getParameterTypes()));
        }
        return null;
    }
    
    public static void liveInput(){
        try {
            Scanner sc = new Scanner(System.in);
            System.out.print("> ");
            while(sc.hasNextLine()){
                String line = sc.nextLine();
                if(line.length() > 0){
                    CommandUtils.invoke(line);
                }
                System.out.print("> ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
