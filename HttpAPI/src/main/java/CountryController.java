import java.util.concurrent.ConcurrentHashMap;
import io.javalin.http.Context;

public class CountryController {
    private ConcurrentHashMap<String, Country> countries = new ConcurrentHashMap<>();

    public CountryController(){
        countries.put("Switzerland", new Country("Switzerland", "Bern", 8796669));
        countries.put("France", new Country("France", "Paris", 64756584));
        countries.put("India", new Country("India", "New Delhi", 1428627663));
    }

    public void get(Context ctx){
        String countryName = ctx.pathParam("name");
        ctx.json(countries.get(countryName));
    }

    public void getAll(Context ctx){
        ctx.json(countries);
    }

    public void create(Context ctx){
        Country country = ctx.bodyAsClass(Country.class);
        countries.put(country.name, country);
        ctx.status(201);
    }

    public void delete(Context ctx){
        String cntryName = ctx.pathParam("name");
        countries.remove(cntryName);
    }

    public void update(Context ctx){
        String cntryName = ctx.pathParam("name");
        Country country = ctx.bodyAsClass(Country.class);
        countries.put(cntryName, country);
    }

}
