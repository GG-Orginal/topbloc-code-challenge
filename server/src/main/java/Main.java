import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

class CandyList{
    List<Candy> list;
    //getter and setter
    public List<Candy> getList(){
        return list;
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

    // Read from Distributors file
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

            for (int d = 0; d < 3; d++) {

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

    // Read from Inventory file
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

    private static double lowestCostAmongDistributors(Candy c){

        List<Distributor> distr = distrInv();

        double minCost = 10000.0;

        for (Distributor d: distr) {

            //Equals is overriden to match against SKU/id and name
            Item comparisonItem = new Item(c.name,c.id,0.0);

            if(d.inv.contains(comparisonItem)){
                //get cost of item at index where the comparisonItem matches
                double cost = d.inv.get(d.inv.indexOf(comparisonItem)).cost;
                System.out.println("###################");
                System.out.println("cost: " + cost);
                System.out.println("name: " + comparisonItem.name);

                if(cost < minCost){
                    minCost = cost;

                }
            }
        }

        return minCost;

    }

    private static double lowTotalRestockCost(List<Candy> candies){

        double totalCost = 0;

        for (Candy c: candies) {
            totalCost += ((double) lowestCostAmongDistributors(c) * (double) c.quantity);
        }

        return totalCost;
    }

    public static void main(String[] args) {

        List<Candy> candies = candyStock();
//        List<Distributor> distr = distrInv();
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

        //TODO: Return JSON containing the candies for which the stock is less than 25% of it's capacity
        get("/low-stock", (request, response) -> {

            response.type("application/json");


            List<Candy> candies1 = candyStock();
            List<Candy> lowStock = new ArrayList<>();

            for (Candy c: candies1){
                if((((double) c.stock)/ ((double) c.capacity)) < 0.25){
                    System.out.println("Low on: + " + c.name);
                    System.out.println("stock: " + (c.stock));
                    System.out.println("cap: " + (c.capacity));
                    System.out.println((c.stock/c.capacity));
                    lowStock.add(c);
                }
            }

            String json = new Gson().toJson(lowStock);

            //response.body(json);
            //System.out.println(response.body());
//            JsonObject jo = new JsonObject();
//            jo.put();
            return json;
        });

        //TODO: Return JSON containing the total cost of restocking candy
        post("/restock-cost", (request, response) -> {

            System.out.println(request.body());

            JsonParser parser = new JsonParser();
            JsonObject json1 = (JsonObject) parser.parse(request.body());

            String clist = json1.get("items").toString();

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Candy>>() {}.getType();
            ArrayList<Candy> c5 = new Gson().fromJson(clist, listType);

            System.out.println("bruh");
            System.out.println(c5.getClass());
            System.out.println(c5);

            double cost = lowTotalRestockCost(c5);
            cost = Math.round(cost * 100.0) / 100.0;
            System.out.println(cost);

            String json = new Gson().toJson(cost);
            return json;
        });

    }
}
