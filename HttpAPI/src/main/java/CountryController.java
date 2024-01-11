import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import io.javalin.http.Context;
import java.sql.*;

public class CountryController {
    private ConcurrentHashMap<String, Country> countries = new ConcurrentHashMap<>();

    private Connection connect;
    final static String dbUrl = "jdbc:postgresql://localhost:5432/countryDB";
    final static String login = "apiUser";
    final static String psw   = "1_L0VE_D@1";

    public CountryController(){
        /*
        countries.put("Switzerland", new Country("Switzerland", "Bern", 8796669));
        countries.put("France", new Country("France", "Paris", 64756584));
        countries.put("India", new Country("India", "New Delhi", 1428627663));
        */
        String urlConnect = dbUrl + "?user=" + login + "&password=" + psw + "&currentSchema=countries";
        try{
            connect = DriverManager.getConnection(urlConnect);
            //System.out.println("Connected baby!!!!");
        } catch (Exception e) {
            System.out.println("Problem during connection to DB : " + e);
        }
    }

    public void get(Context ctx){
        try{
            var getCountry = connect.prepareStatement("SELECT name, capital, population FROM country WHERE name =?");
            getCountry.setString(1, ctx.pathParam("name"));
            var result = getCountry.executeQuery();
            result.next();
            ctx.json(new Country(result));
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            ctx.status(500);
        }
        
    }

    public void getAll(Context ctx){
        HashMap<Integer, Country> values = new HashMap<>();
        try{
            int id = 0;
            var countries = connect.createStatement().executeQuery("SELECT name, capital, population FROM country");
            while (countries.next()) {
                values.put(id++, new Country(countries));
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        ctx.json(values);
    }

    public void create(Context ctx){
        try{
            Country country = ctx.bodyAsClass(Country.class);
            var statement = connect.prepareStatement("INSERT INTO country(name,capital,population) VALUES(?,?,?)");
            statement.setString(1, country.name);
            statement.setString(2, country.capital);
            statement.setInt(3, country.population);
            statement.executeQuery();
            ctx.status(201);
        } catch (Exception e){
            System.out.println(e.getMessage());
            ctx.status(500);
        }
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
