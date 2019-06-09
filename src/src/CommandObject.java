
public class CommandObject {

    public String name;
    public Object value;
    public Class definingClass;

    public CommandObject(String name, Object value, Class definingClass) {
        this.name = name;
        this.value = value;
        this.definingClass = definingClass;
    }
    
    
    
}
