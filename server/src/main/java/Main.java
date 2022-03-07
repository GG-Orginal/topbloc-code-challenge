import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.*;

import static spark.Spark.*;

class Candy {

    String name;
    int stock;
    int capacity;
    long id;
    int quantity = 0;

    public Candy (String name,int stock,int capacity,long id,int quantity){
        this.name = name;
        this.stock = stock;
        this.capacity = capacity;
        this.id = id;
        this.quantity = quantity;
    }
}

class Item {
    String name;
    long id;
    double cost;

    public Item (String name,long id, double cost) {
        this.name = name;
        this.id = id;
        this.cost = cost;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final Item other = (Item) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }

        if (this.id != other.id) {
            return false;
        }

        return true;
    }
}

class Distributor {
    String name;
    List<Item> inv;

    public Distributor (String name, List<Item> inv) {
        this.name = name;
        this.inv = inv;
    }

}

public class Main {

    /**
     * Read from Distributors file, works for unlimited number of distributors
     * @return List of distributor objects
     */
    private static List<Distributor> distrInv(){
        FileInputStream file =
                null;
        try {
            file = new FileInputStream(
                    new File(
                            "resources/Distributors.xlsx"));

            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //list of distribtors who each have their own list of items
            List<Distributor> distr = new ArrayList<Distributor>();

            for (int d = 0; d < workbook.getNumberOfSheets(); d++) {

                XSSFSheet sheet = workbook.getSheetAt(d);
                Iterator<Row> rowIterator = sheet.iterator();

                Row head = rowIterator.next(); // if this doesn't work your file is messed up

                List<Item> items = new ArrayList<Item>();

                // Loop through inventory spreadsheet and create a list of candy objects
                while (rowIterator.hasNext()) {
                    System.out.println(rowIterator.hasNext());
                    System.out.println("DOODLEBOB");

                    Row row = rowIterator.next();

                    if ((Cell) row.getCell(0) == null){
                        break;
                    }
                    // Get the cell values in the current row
                    String nameCell = ((Cell) row.getCell(0)).getStringCellValue();
                    System.out.println(nameCell);
                    long idCell = (long) ((Cell) row.getCell(1)).getNumericCellValue();
                    System.out.println(idCell);
                    double costCell = (double) ((Cell) row.getCell(2)).getNumericCellValue();
                    System.out.println(costCell);

                    Item item = new Item(nameCell, idCell, costCell);
                    items.add(item);

                }

                distr.add(new Distributor(workbook.getSheetName(d),items));

            }
            return distr;

        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Read from Inventory file
     * @return Candy List
     */
    private static List<Candy> candyStock(){
        FileInputStream file =
                null;
        try {
            file = new FileInputStream(
                    new File(
                            "resources/Inventory.xlsx"));

            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();


        System.out.println("hi mom");
        Row head = rowIterator.next(); // if this doesn't work your inventory file is messed up

        List<Candy> candies = new ArrayList<Candy>();

        // Loop through inventory spreadsheet and create a list of candy objects
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            // Get the cell values in the current row
            String nameCell = ((Cell) row.getCell(0)).getStringCellValue();
            int stockCell = (int) ((Cell) row.getCell(1)).getNumericCellValue();
            int capacityCell = (int) ((Cell) row.getCell(2)).getNumericCellValue();
            long idCell = (long) ((Cell) row.getCell(3)).getNumericCellValue();

            Candy candy = new Candy(nameCell,stockCell,capacityCell,idCell,0);

            //System.out.println(candy);

            candies.add(candy);

        }

        return candies;

        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Return the lowest price of Candy c among all the distributors, result is not rounded
     * @param c Candy whose price you want to check
     * @return
     */
    private static double lowestCostAmongDistributors(Candy c){

        // Get all distrbutors from xlsx file as java list
        List<Distributor> distr = distrInv();

        double minCost = 10000.0;

        // Loop through all distributors
        for (Distributor d: distr) {

            //Equals is overriden to match against SKU/id and name
            // comparison item is an object of type Item created from the given Candy c
            // cost doesn't matter since we are only using it to match the name and id below
            Item comparisonItem = new Item(c.name,c.id,0.0);

            if(d.inv.contains(comparisonItem)){
                //get cost of item at index where the comparisonItem matches
                double cost = d.inv.get(d.inv.indexOf(comparisonItem)).cost;

                if(cost < minCost){
                    minCost = cost;
                }
            }
        }

        return minCost;
    }

    /**
     * Find the lowest total restock price of a list of candies with given quantities
     * @param candies Candy List
     * @return tptal cost - not rounded
     */
    private static double lowTotalRestockCost(List<Candy> candies){

        double totalCost = 0;

        for (Candy c: candies) {
            totalCost += ((double) lowestCostAmongDistributors(c) * (double) c.quantity);
        }

        return totalCost;
    }

    public static void main(String[] args) {

        //This is required to allow GET and POST requests with the header 'content-type'
        options("/*",
                (request, response) -> {
                        response.header("Access-Control-Allow-Headers",
                                "content-type");

                        response.header("Access-Control-Allow-Methods",
                                "GET, POST");


                    return "OK";
                });

        //This is required to allow the React app to communicate with this API
        before((request, response) -> response.header("Access-Control-Allow-Origin", "http://localhost:3000"));

        //Returns JSON containing the candies for which the stock is less than 25% of it's capacity
        get("/low-stock", (request, response) -> {

            response.type("application/json");

            List<Candy> candies1 = candyStock();
            List<Candy> lowStock = new ArrayList<>();

            // Loop through entire stock of candies
            for (Candy c: candies1){
                // if the stock is less than 25% capacity add it to low stock list
                if((((double) c.stock)/ ((double) c.capacity)) < 0.25){
                    lowStock.add(c);
                }
            }

            // convert lowstock list to jason
            String json = new Gson().toJson(lowStock);

            return json;
        });

        //Returns JSON containing the total cost of restocking candy
        post("/restock-cost", (request, response) -> {

            // Parse request body
            JsonParser parser = new JsonParser();
            JsonObject json1 = (JsonObject) parser.parse(request.body());
            String clist = json1.get("items").toString();

            // convert json list to java
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Candy>>() {}.getType();
            ArrayList<Candy> candies = new Gson().fromJson(clist, listType);


            double cost = lowTotalRestockCost(candies);
            cost = Math.round(cost * 100.0) / 100.0;
            System.out.println(cost);

            String json = new Gson().toJson(cost);
            return json;
        });

    }
}
