
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Node;

public class CommandNode {

    public Node XMLRep;
    public CommandNode[] children;
    public String name, description, location;

    public CommandNode(){}
    
    public CommandNode(Node XMLRep, CommandNode... passChild) {
        this.XMLRep = XMLRep;
        this.children = passChild;
    }

    public void init() {
        List<Node> nodeChildren = CommandUtils.getChildren(XMLRep);
        List<Node> names = CommandUtils.filterNodesTag(nodeChildren, CommandUtils.TAGS.NAME);
        this.name = (names.isEmpty() ? null : names.get(0).getTextContent());
        List<Node> descriptions = CommandUtils.filterNodesTag(nodeChildren, CommandUtils.TAGS.DESCRIPTION);
        this.description = (descriptions.isEmpty() ? null : descriptions.get(0).getTextContent());
        List<Node> locations = CommandUtils.filterNodesTag(nodeChildren, CommandUtils.TAGS.LOCATION);
        this.location = (locations.isEmpty() ? null : locations.get(0).getTextContent());
    }

    @Override
    public String toString() {
        String retString = "";
        if (this.name != null) {
            retString += "NAME:" + this.name + "\n";
        }
        if (this.description != null) {
            retString += "DESCRIPTION:" + this.description + "\n";
        }
        if (this.location != null) {
            retString += "LOCATION:" + this.location + "\n";
        }
        if (this.name == null && this.description == null && this.location == null) {
            retString += "root";
        }
        if (children.length > 0) {
            retString += "CHILDREN:\n" + Arrays.toString(children);
        }
        return retString;
    }

    public static CommandNode getPath(String pathText){
        String[] splitString = pathText.split(" ");
        CommandNode currNode = CommandUtils.ROOT;
        for(int i = 0; i < splitString.length; i++){
            CommandNode nextNode = null;
            for(CommandNode child : currNode.children){
                if(splitString[i].equals(child.name)){
                    nextNode = child;
                    break;
                }
            }
            if(nextNode == null){
                break;
            }
            currNode = nextNode;
        }
        return currNode;
    }
    
    public Object invokeHelp(String passText) throws ClassNotFoundException, CommandNotFoundException{
        String endString = passText.substring("help ".length());
        CommandNode c = getPath(endString);
        if(c.equals(CommandUtils.ROOT)){
            throw new CommandNotFoundException(endString);
        }
        DefaultCommands.helpCommand(c);
        return null;
    }
    
    public Object invoke(String passText) throws ClassNotFoundException, CommandNotFoundException{
        if(passText.length() > 4){
            String helpName = "help ";
            boolean isHelp = true;
            for(int i = 0; i < helpName.length(); i ++){
                if(helpName.charAt(i) != passText.charAt(i)){
                    isHelp = false;
                    break;
                }
            }
            if(isHelp){
                return invokeHelp(passText);
            }
        }
        Entry<Method, String> entry = getMethodFullArgs(this, passText);
        String endString = passText.substring(entry.getValue().length() + 1);
        String[] regArr = RegexUtils.getPaddedRegexFromMethod(entry.getKey());
        if(regArr.length == 0){
            try {
                return entry.getKey().invoke(null, new Object[]{});
            } catch (IllegalAccessException ex) {
                Logger.getLogger(CommandNode.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(CommandNode.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(CommandNode.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String[] toMatch = new String[regArr.length];
        char[] charRep = endString.toCharArray();
        String accumulated = "";
        int regexIndex = 0;
//        System.out.println("regArr:" + Arrays.toString(regArr));
        for (int i = 0; i < charRep.length &&  regexIndex < regArr.length; i++) {
            accumulated+= charRep[i];
            if(accumulated.matches(regArr[regexIndex]) && i == charRep.length - 1){
                toMatch[regexIndex] = accumulated;
                regexIndex++;
                accumulated = "";
                break;
            }
            if(accumulated.matches(regArr[regexIndex]) == false && i == charRep.length - 1){
                break;
            }
            if((accumulated + charRep[i+1]).matches(regArr[regexIndex]) == false
                    && accumulated.matches(regArr[regexIndex])){
                toMatch[regexIndex] = accumulated;
                regexIndex++;
                accumulated = "";
            }
        }
        for(String s : toMatch){
            if(s == null){
                throw new IllegalArgumentException("Invalid argument types.");
            }
        }
        String[] parameters = new String[entry.getKey().getParameterCount()];
        for (int i = 0; i < toMatch.length; i++) {
            if(i % 2 == 0){
                parameters[i/2] = toMatch[i];
            }
        }
        Class[] clazzes = entry.getKey().getParameterTypes();
        Object[] objects = new Object[clazzes.length];
        for (int i = 0; i < parameters.length; i++) {
            String currParCast = parameters[i];
            Class currClass = clazzes[i];
            Function<String, ?> castFunc = RegexUtils.REGEX.castMap.get(currClass);
            Object currObj = castFunc.apply(currParCast);
            if(currClass.equals(int[].class) && currObj instanceof Integer[]){
                objects[i] = ReflectionUtils.convertInteger((Integer[])currObj);
            }else if(currClass.equals(double[].class) && currObj instanceof Double[]){
                objects[i] = ReflectionUtils.convertDouble((Double[])currObj);
            }else{
                objects[i] = currObj;
            }
        }
        try {
            Method m = entry.getKey();
            Object ret = m.invoke(null, objects);
            return ret;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CommandNode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CommandNode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(CommandNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public CommandNode join(CommandNode c2){
        return CommandNode.join(this, c2);
    }
    
    public static CommandNode join(CommandNode c1, CommandNode c2){
        CommandNode retNode = new CommandNode();
        List<CommandNode> children = new ArrayList();
        for(CommandNode child : c1.children){
            children.add(child);
        }
        for(CommandNode child2 : c2.children){
            boolean contains = false;
            for(CommandNode child1 : c1.children){
                if(child2.name != null){
                    if(child2.name.equals(child1.name)){
                        contains = true;
                        break;
                    }
                }
            }
            if(contains == false){
                children.add(child2);
            }
        }
        CommandNode[] childArr = children.toArray(new CommandNode[children.size()]);
        retNode.children = childArr;
        retNode.XMLRep = c1.XMLRep;
        retNode.name = c1.name;
        retNode.description = c1.description;
        retNode.location = c1.location;
        return retNode;
    }

    public static Entry<Method, String[]> getMethodFull(CommandNode root, String passedArg) throws ClassNotFoundException, CommandNotFoundException {
        Method m = getMethod(root, passedArg);
        if (m != null) {
            return new EntryObject(m, new String[]{});
        }
        String[] splitArg = passedArg.split(" ");
        for (int i = splitArg.length - 1; i > 0; i--) {
            String[] subSplit = Arrays.copyOfRange(splitArg, 0, i);
            String toTest = "";
            for (int j = 0; j < subSplit.length; j++) {
                toTest += subSplit[j];
                if (j != subSplit.length - 1) {
                    toTest += " ";
                }
            }
            m = getMethod(root, toTest);
            if (m != null) {
                String[] args = Arrays.copyOfRange(splitArg, i, splitArg.length);
                List<String> argList = new ArrayList();
                boolean QUOTE = false;
                int QUOTE_FOUND = 0;
                String aggregate = "";
                for (int j = 0; j < args.length; j++) {
                    String toCheck = args[j];
                    if (args[j].startsWith("\"") || args[j].endsWith("\"")) {
                        QUOTE_FOUND++;
                        QUOTE = !QUOTE;
                        toCheck = args[j].replace('\"', (char) 0);
                        if (!QUOTE) {
                            aggregate += toCheck;
                            argList.add(aggregate);
                            aggregate = "";
                            continue;
                        }
                    }
                    if (QUOTE) {
                        aggregate += toCheck + " ";
                        continue;
                    } else {
                        argList.add(toCheck);
                    }
                }
                return new EntryObject(m, argList.toArray(new String[argList.size()]));
            }
        }
        return null;
    }
    
    public static Entry<Method, String> getMethodFullArgs(CommandNode root, String passedArg) throws ClassNotFoundException, CommandNotFoundException {
        Entry<Method, String> ent = getMethodArgs(root, passedArg);
        if(ent == null){
            throw new CommandNotFoundException( passedArg);
        }
        Method m = ent.getKey();
        if (m != null) {
            return new EntryObject(m, new String());
        }
        String[] splitArg = passedArg.split(" ");
        for (int i = splitArg.length - 1; i > 0; i--) {
            String[] subSplit = Arrays.copyOfRange(splitArg, 0, i);
            String toTest = "";
            for (int j = 0; j < subSplit.length; j++) {
                toTest += subSplit[j];
                if (j != subSplit.length - 1) {
                    toTest += " ";
                }
            }
            ent = getMethodArgs(root, toTest);
            m = ent.getKey();
            if (m != null) {
                return new EntryObject(m, toTest);
            }
        }
        throw new CommandNotFoundException( passedArg);
    }

    public static Method getMethod(CommandNode root, String treePath) throws ClassNotFoundException, CommandNotFoundException {
        if (treePath.contains(" ") == false) {
            for (CommandNode child : root.children) {
                if (treePath.equals(child.name)) {
                    return CommandUtils.getMethod(child.location);
                }
            }
            return null;
        }
        String firstCom = treePath.substring(0, treePath.indexOf(" "));
        for (CommandNode child : root.children) {
            if (treePath.equals(child.name)) {
                return CommandUtils.getMethod(child.location);
            }
            if (firstCom.equals(child.name)) {
                return getMethod(child, treePath.substring(treePath.indexOf(" ") + 1));
            }
        }
        return null;
    }

    public static Entry<Method, String> getMethodArgs(CommandNode root, String treePath) throws ClassNotFoundException, CommandNotFoundException {
        if (treePath.contains(" ") == false) {
            for (CommandNode child : root.children) {
                if (treePath.equals(child.name)) {
//                    System.out.println(child);
                    if(child.location == null){
                        throw new CommandNotFoundException(treePath);
                    }
                    Method m = CommandUtils.getMethod(child.location);
                    return new EntryObject(m, child.name);
                }
            }
            return null;
        }
        String firstCom = treePath.substring(0, treePath.indexOf(" "));
        for (CommandNode child : root.children) {
            if (treePath.equals(child.name)) {
//                    System.out.println(child);
                Method m = CommandUtils.getMethod(child.location);
                return new EntryObject(m, child.name);
            }
            if (firstCom.equals(child.name)) {
//                    System.out.println(child);
                Method m = getMethod(child, treePath.substring(treePath.indexOf(" ") + 1));
                return new EntryObject(m, child.name);
            }
        }
        return null;
    }

    public String[] childNames(){
        String[] retArray = new String[this.children.length];
        for (int i = 0; i < this.children.length; i++) {
            retArray[i] = this.children[i].name;
        }
        return retArray;
    }
    
}
