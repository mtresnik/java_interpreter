
import java.math.BigDecimal;
import java.math.BigInteger;

public class ReflectionUtils {

    
    public static int[] convertInteger(Integer[] passed){
        int[] retArray = new int[passed.length];
        for (int i = 0; i < passed.length; i++) {
            retArray[i] = passed[i].intValue();
        }
        return retArray;
    }
    
    public static double[] convertDouble(Double[] passed){
        double[] retArray = new double[passed.length];
        for (int i = 0; i < passed.length; i++) {
            retArray[i] = passed[i].doubleValue();
        }
        return retArray;
    }
    
    public static Object[] bulkCast(String[] from, Class[] defining) throws NumberFormatException {
        Object[] retArray = new Object[defining.length];
        for (int i = 0; i < retArray.length; i++) {
            String currFrom = from[i];
            Class currDef = defining[i];
            if (currDef.equals(String.class)) {
                retArray[i] = currFrom;
                continue;
            }
            try {
                Number n = castNum(currFrom, currDef);
                retArray[i] = n;
                continue;
            } catch (NumberFormatException nfe) {
                throw nfe;
            }
        }
        return retArray;
    }

    public static Number castNum(String obj1, Class clazz) throws NumberFormatException {
        if (obj1 == null || clazz == null) {
            return null;
        }
        if (clazz.equals(Byte.class) || clazz.getName().equals("byte")) {
            return new Byte(obj1);
        } else if (clazz.equals(Short.class)|| clazz.getName().equals("short")) {
            return new Short(obj1);
        } else if (clazz.equals(Long.class) || clazz.getName().equals("long")) {
            return new Long(obj1);
        } else if (clazz.equals(Integer.class) || clazz.getName().equals("int")) {
            return new Integer(obj1);
        } else if (clazz.equals(Float.class) || clazz.getName().equals("float")) {
            return new Float(obj1);
        } else if (clazz.equals(Double.class) || clazz.getName().equals("double")) {
            return new Double(obj1);
        } else if (clazz.equals(BigInteger.class)) {
            return new BigInteger(obj1);
        } else if (clazz.equals(BigDecimal.class)) {
            return new BigDecimal(obj1);
        } else {
            return null;
        }
    }
    
    public static int countContains(String source, String value){
        int retVal = 0;
        for (int i = 0; i < source.length() - value.length() + 1; i++) {
            String sub = source.substring(i, i + value.length());
            if(sub.equals(value)){
                retVal++;
            }
        }
        return retVal;
    }

}
