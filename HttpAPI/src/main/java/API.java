
import io.javalin.*;

public class API{
   
    private static int PORT = 3141;
    public static void main (String[] args){
        Javalin app = Javalin.create().start(PORT);
    }
}