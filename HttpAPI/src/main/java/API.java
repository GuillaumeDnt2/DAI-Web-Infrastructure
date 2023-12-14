
import io.javalin.*;

public class API{
   
    private static int PORT = 3141;
    public static void main (String[] args){
        Javalin app = Javalin.create().start(PORT);
        CountryController controller = new CountryController();
        app.get("/api/countries", controller::getAll);
        System.out.println("Server started");
    }
}