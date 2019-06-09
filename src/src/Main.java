
import java.util.Scanner;


public class Main {

    public static void main(String[] args) throws Exception {
        liveTest();
    }

    public static void testCommands() {
        try {
            CommandUtils.loadCommands("c1.xml");
            CommandUtils.invoke("say \"Hello World!\"");
            CommandUtils.invoke("bridge build 20");
            CommandUtils.invoke("simp \"yo\" 20");
            CommandUtils.invoke("arr [20,10,20,30,40]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void liveTest() {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.print("> ");
            while(sc.hasNextLine()){
                String line = sc.nextLine();
                CommandUtils.invoke(line);
                System.out.print("> ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
