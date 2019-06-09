
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;


public class RegexUtils {

    public static final class REGEX {

        public static final String STRING = "(\"{1}[\\s\\S]+\"{1})|(\'{1}[\\s\\S]+\'{1})";
        public static final String INTEGER = "((\\+|-)?\\d+)";
        public static final String DOUBLE = "((\\+|-)?(0|([1-9][0-9]*))(\\.[0-9]+)?)";
        public static final String SPACE = "( +)";
        public static final String COMMA = "(( *),( *))";
        
        public static final Function<String, String> STRING_CAST = (passed) -> {
            String retString = "";
            char[] charRep = passed.toCharArray();
            for (int i = 1; i < charRep.length - 1; i++) {
                retString += charRep[i];
            }
            return retString;
        };
        
        public static final Function<String, Integer> INTEGER_CAST = (passed) -> {
            Integer retNum = null;
            retNum = Integer.parseInt(passed);
            return retNum;
        };
        
        public static final Function<String, Double> DOUBLE_CAST = (passed) -> {
            Double retNum = null;
            retNum = Double.parseDouble(passed);
            return retNum;
        };
        

        public static final Map<Class, String> defaultMap = new LinkedHashMap();
        public static final Map<Class, Function<String, ?>> castMap = new LinkedHashMap();
        static{
            defaultMap.put(String.class, STRING);
            castMap.put(String.class, STRING_CAST);
            
            defaultMap.put( Integer.class, INTEGER);
            castMap.put(Integer.class, INTEGER_CAST);
            defaultMap.put(int.class, INTEGER);
            castMap.put(int.class, INTEGER_CAST);
            
            defaultMap.put(Double.class, DOUBLE);
            castMap.put(Double.class, DOUBLE_CAST);
            defaultMap.put(double.class, DOUBLE);
            castMap.put(double.class, DOUBLE_CAST);
        }
        
        public static String ARRAY(String passedType) {
            return "(\\[{1}(" + passedType + COMMA + ")+(" + passedType + ")\\]{1})|(\\[{1}(" + passedType + ")\\]{1})";
        }
        
        public static <K> Function<String, K[]> ARRAY_CAST(String innerRegex, Class def, Function<String, K> inner){
            Function<String, K[]> retFunction = (passed) -> {
                char[] charRep = passed.toCharArray();
                List<String> retList = new ArrayList();
                String accumulated = "";
                String[] regArr = new String[]{innerRegex, COMMA};
                int regIndex = 0;
                for(int i = 1; i < charRep.length - 1; i++){
                    accumulated += charRep[i];
                    if(accumulated.matches(regArr[regIndex]) && i == charRep.length - 2){
                        if(regIndex == 0){
                            retList.add(accumulated);
                        }
                        accumulated = "";
                        break;
                    }
                    if((accumulated + charRep[i + 1]).matches(regArr[regIndex]) == false 
                            && accumulated.matches(regArr[regIndex])){
                        if(regIndex == 0){
                            retList.add(accumulated);
                        }
                        accumulated = "";
                        regIndex = (regIndex + 1) % 2;
                    }
                }
                String[] retArr = new String[retList.size()];
                List<K> objList = new ArrayList();
                for(String str : retList){
                    objList.add(inner.apply(str));
                }
                K[] temp = (K[]) Array.newInstance(def, objList.size());
                for (int i = 0; i < objList.size(); i++) {
                    temp[i] = objList.get(i);
                }
                return temp;
            };
            return retFunction;
        }
        
        public static final Map<Class, String> arrayMap = new LinkedHashMap();
        static{
            arrayMap.put(String[].class, ARRAY(STRING));
            castMap.put(String[].class, ARRAY_CAST(STRING, String[].class, STRING_CAST));
            
            arrayMap.put(Integer[].class, ARRAY(INTEGER));
            castMap.put(Integer[].class, ARRAY_CAST(INTEGER, Integer.class, INTEGER_CAST));
            arrayMap.put(int[].class, ARRAY(INTEGER));
            castMap.put(int[].class, ARRAY_CAST(INTEGER, Integer.class, INTEGER_CAST));
            
            arrayMap.put( Double[].class, ARRAY(DOUBLE));
            castMap.put(Double[].class, ARRAY_CAST(DOUBLE, Double[].class, DOUBLE_CAST));
            arrayMap.put( double[].class, ARRAY(DOUBLE));
            castMap.put(double[].class, ARRAY_CAST(DOUBLE, Integer[].class, DOUBLE_CAST));
        }
        
        private static String[] allDefaults(){
            List<String> retList = new ArrayList(defaultMap.keySet());
            return retList.toArray(new String[retList.size()]);
        }
        
        public static String[] allRegex(){
            String[] allDef = allDefaults();
            List<String> retList = new ArrayList(Arrays.asList(allDef));
            for(String s : allDef){
                retList.add(ARRAY(s));
            }
            return retList.toArray(new String[retList.size()]);
        }
        
        public static Class getRegexMapped(String regexFrom){
            for(Entry<Class, String> ent : defaultMap.entrySet()){
                if(ent.getValue().equals(regexFrom)){
                    return ent.getKey();
                }
            }
            for(Entry<Class, String> ent : arrayMap.entrySet()){
                if(ent.getValue().equals(regexFrom)){
                    return ent.getKey();
                }
            }
            return null;
        }
    }
    
    public static String[] getRegexFromMethod(Method m){
        Class[] allClasses = m.getParameterTypes();
        String[] regexRequired = new String[allClasses.length];
        for (int i = 0; i < allClasses.length; i++) {
            String mappedFrom = getMatchingRegex(allClasses[i]);
            if(mappedFrom == null){
                System.out.println("CANNOT FIND REGEX FOR:" + allClasses[i]);
                throw new RuntimeException();
            }
            regexRequired[i] = mappedFrom;
        }
        return regexRequired;
    }
    
    public static String[] getPaddedRegexFromMethod(Method m){
        String[] regArr = getRegexFromMethod(m);
        if(regArr.length == 0){
            return new String[]{};
        }
        String[] retArr = new String[regArr.length*2 - 1];
        for (int i = 0; i < regArr.length; i++) {
            int index = 2*i;
            retArr[index] = regArr[i];
        }
        for(int i = 1; i < regArr.length; i+=2){
            retArr[i] = REGEX.SPACE;
        }
        return retArr;
    }
    
    
    public static String getCombinedRegex(Method m){
        String[] regArr = getPaddedRegexFromMethod(m);
        String retString = "";
        for (int i = 0; i < regArr.length; i++) {
            retString += regArr[i];
        }
        return retString;
    }
    
    
    public static String getMatchingRegex(Class passed){
        for(Entry<Class, String> ent : REGEX.defaultMap.entrySet()){
            if(ent.getKey().equals(passed)){
                return ent.getValue();
            }
        }
        for(Entry<Class, String> ent : REGEX.arrayMap.entrySet()){
            if(ent.getKey().equals(passed)){
                return ent.getValue();
            }
        }
        return null;
    }
    
    public static Class getMatchingClass(String passed){
        for(Entry<Class, String> ent : REGEX.defaultMap.entrySet()){
            if(passed.matches(ent.getValue())){
                return ent.getKey();
            }
        }
        for(Entry<Class, String> ent : REGEX.arrayMap.entrySet()){
            if(passed.matches(ent.getValue())){
                return ent.getKey();
            }
        }
        return null;
    }
    
    
    

}
