
import java.util.Arrays;


public class Test {

    public static void say(String toSay){
        System.out.println(toSay);
    }
    
    public static void parameterTest(String s1, int i1, double d1){
        System.out.println("CALLED");
        System.out.println("str:" + s1);
        System.out.println("int:" + i1);
        System.out.println("doubl:" + d1);
    }
    
    public static void simpleTest(String s1, int i1){
        for(int i = 0; i < i1; i++){
            System.out.println(s1);
        }
    }
    
    public static void arrayTest(int[] a){
        System.out.println("ARRAY TEST");
        System.out.println(Arrays.toString(a));
    }
    
    
    
}
