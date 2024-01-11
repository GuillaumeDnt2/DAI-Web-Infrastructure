import java.sql.ResultSet;

public class Country {
    public String name;
    public String capital;
    public int    population;
    public String flagPath; //Si on a du temps

    public Country(){}

    public Country(String name, String capital, int population){
        this.name = name;
        this.capital = capital;
        this.population = population;
    }

    public Country(ResultSet result){
        try{
            name = result.getString("name");
            capital = result.getString("capital");
            population = result.getInt("population");
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}