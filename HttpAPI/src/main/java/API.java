
import io.javalin.*;

public class API{
   
    private static int PORT = 3149;
    public static void main (String[] args){
        Javalin app = Javalin.create().start(PORT);
        CountryController controller = new CountryController();
        app.get("/country", controller::getAll);
        app.get("/country/{name}", controller::get);
        app.post("/country", controller::create);
        app.put("/country/{name}", controller::update);
        app.delete("/country/{name}", controller::delete);
    }
}