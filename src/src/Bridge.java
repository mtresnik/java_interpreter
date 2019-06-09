
public class Bridge {

    public static String build(int num){
        for(int i = 0; i < num; i++){
            System.out.println("Building a bridge:" + i);
        }
        return "Built!";
    }
    
    public static void destroy(){
        System.out.println("Destroying a bridge");
    }
    
}
