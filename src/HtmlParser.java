
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException; 

/**
 * This class is for web scraping
 * 
 * @author huanyi_guo
 */
public class HtmlParser{
	/**
	 * getFullText - grab full text from the given url
	 * @throws IOException - if the url given is not reachable
	 * @param url - the given url
	 * @return full text in string, or warning message if cannot connect to the given url
	 */
	public static String getFullText(String url) throws IOException{
		Document doc = Jsoup.connect(url).get();
		return doc.text();
	}

	/**
	 * main - main method is for testing only
	 */
	public static void main(String[] args){
		
		try{
			String text = HtmlParser.getFullText("http://en.wikipedia.org/wiki/Wiki");
			System.out.println(text);
		}
		catch(IOException e){
			
		}
	}
}
