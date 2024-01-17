
import io.javalin.*;

public class API{
   
    private static int PORT = 3141;
    public static void main (String[] args){
        Javalin app = Javalin.create().start(PORT);
        CountryController controller = new CountryController();
        app.get("/api/country", controller::getAll);
        app.get("/api/country/{name}", controller::get);
        app.post("/api/country", controller::create);
        app.put("/api/country/{name}", controller::update);
        app.delete("/api/country/{name}", controller::delete);
    }
}