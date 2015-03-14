/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
//import java.util.ArrayList;

/**
 *
 * @author pumpkin
 */
public class Bing {
    
    /**
     * 
     * @param query - query to use for the search
     * @return - a json array of results. each contains a title, description, url, 
     * and some other metadata that can be easily extracted since its in json format
     */
    public static JSONArray search(String query){
        try {
            query = "'"+query+"'";
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            System.out.println(encodedQuery);
//            URL url = new URL("https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27web%27&Query=%27car%27");
//            URL url = new URL("http://csb.stanford.edu/class/public/pages/sykes_webdesign/05_simple.html");
            String webPage = "https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27web%27&Query="+encodedQuery+"&$format=JSON";
            String name = "6604d12c-3e89-4859-8013-3132f78c1595";
            String password = "cefgNRl3OL4PrJJvssxkqLw0VKfYNCgyTe8wNXotUmQ";

            String authString = name + ":" + password;
            System.out.println("auth string: " + authString);
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            System.out.println("Base64 encoded auth string: " + authStringEnc);

            URL url = new URL(webPage);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            
            in.close();
            JSONParser parser = new JSONParser();
            JSONArray arr = (JSONArray)((JSONObject)((JSONObject)parser.parse(response.toString())).get("d")).get("results");
            JSONObject obj = (JSONObject)arr.get(0);
            JSONArray out = (JSONArray)obj.get("Web");
            
            return out;
            
//            
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(Bing.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Bing.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Bing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * This main is for testing only.
     **/
    
    /*
    public static void main (String[] args){
        //read keywords
        String fullText;
        JSONArray results = Bing.search("evil genius");
        ArrayList<String> urls = new ArrayList<String>();
        //iterate through the results, printing out each entry.
        for(Object res : results){
            JSONObject obj = (JSONObject)res;
            //System.out.println(obj);
            //System.out.println("URL: ");
            //System.out.println((String)(obj.get("Url")));
            urls.add((String)(obj.get("Url")));
        }
        for(String url : urls){
            fullText = HtmlParser.getFullText(url);
            System.out.println(url);
            System.out.println(fullText);
            System.out.println("\n");
        }
    }
    */
    
}
