
import java.util.Arrays;


public class MathUtils {

    public static Double sum(double[] inputArr){
        double retValue = 0.0;
        String retString = "";
        for(int i = 0; i < inputArr.length; i++){
            retValue += inputArr[i];
            retString += inputArr[i];
            if(i < inputArr.length - 1){
                retString += " + ";
            }
        }
        retString += " = " + retValue;
        System.out.println(retString);
        return retValue;
    }
    
    public static Double mult(double[] inputArr){
        double retValue = 1.0;
        String retString = "";
        for(int i = 0; i < inputArr.length; i++){
            retValue *= inputArr[i];
            retString += inputArr[i];
            if(i < inputArr.length - 1){
                retString += " * ";
            }
        }
        retString += " = " + retValue;
        System.out.println(retString);
        return retValue;
    }
    
    public static Double dot(double[] vec1, double[] vec2){
        if(vec1.length != vec2.length){
            throw new IllegalArgumentException("Vectors must be of same length.");
        }
        double retVal = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            retVal += vec1[i] * vec2[i];
        }
        String retString = Arrays.toString(vec1) + " * " + Arrays.toString(vec2) + " = " + retVal;
        System.out.println(retString);
        return retVal;
    }
    
    public static Double fact(double d){
        int parsed = (int) d;
        double ret = 1.0;
        String retString = "";
        for (int i = 1; i <= parsed; i++) {
            ret *= i;
        }
        retString = "" + parsed + "! = " + ret;
        System.out.println(retString);
        return ret;
    }
    
    
    
    
}
