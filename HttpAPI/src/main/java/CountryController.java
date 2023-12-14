import java.util.concurrent.ConcurrentHashMap;
import io.javalin.http.Context;

public class CountryController {
    private ConcurrentHashMap<String, Country> countries = new ConcurrentHashMap<>();

    public CountryController(){
        countries.put("Switzerland", new Country("Switzerland", "Bern", 9000000));
    }

    public void get(Context ctx){
        String countryName = ctx.pathParam("country");
        ctx.json(countries.get(countryName));
    }

    public void getAll(Context ctx){
        ctx.json(countries);
    }


}
