import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import java.net.UnknownHostException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 * The entry point of the entire application (Testing only)
 *
 * @author huanyi_guo
 */


@SuppressWarnings("serial")
public class GUI  extends JPanel implements ActionListener{

	private static final String TITLE = "Demo"; //title bar text
	private static final String NEWLINE = "\n";	//new line char in unix-like os

	//constant: for testing only
	private static final String DB_HOST = "localhost";			//only run mongoDB on local host
	private static final int DB_PORT = 27017;					//use default port
	private static final String DB_NAME = "test";				//use default database

	private static final String FULLTEXT_INDEX = "fullText";	//index in mongoDB
	private static final String URL_INDEX = "URL";				//index in mongoDB
	private static final String OUTPUT_DIR = "output";

	//tagging
	private static final String SAMPLE_TAG_INDEX = "TOPIC";		//sample tag index
	private static final String SAMPLE_TAG = "pharmaceutical";	//sample tag

	//private static final String SEPARATOR = "|";				//character used in the CSV file

	//menu
	JMenuBar menuBar;
	JMenu menu;
	JMenuItem menuItem;
	//file chooser
	JFileChooser fileChooser;
	//to show status
	JTextArea console;

	/*
	 * Constructor
	 */
	public GUI(){
		//create text area
		console = new JTextArea(40, 40);
		console.setEditable(false);
		JScrollPane consoleScrollPane = new JScrollPane(console);
		//create file chooser
		fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new FileTypeFilter(".pdf", "PDF Documents"));	//pdf support
		fileChooser.addChoosableFileFilter(new FileTypeFilter(".csv", "Comma-Separated Values Documents")); //csv support
		fileChooser.addChoosableFileFilter(new FileTypeFilter(".txt", "Text files")); //txt support
        //create menu
		menuBar = new JMenuBar();
		menu = new JMenu("Menu");
		menuBar.add(menu);
		menuItem = new JMenuItem("Select Input File");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		add(consoleScrollPane, BorderLayout.CENTER);
	}

	private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//Add panel and menu bar to frame
 		GUI m = new GUI();
 		frame.add(m);
 		frame.setJMenuBar(m.menuBar);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

	public void actionPerformed(ActionEvent e) {
		int ret;
		File file;
		String filename;
		ArrayList<String> keywords;

		if(e.getSource() == menuItem){
			ret = fileChooser.showOpenDialog(GUI.this);
			if(ret == JFileChooser.APPROVE_OPTION){
				file = fileChooser.getSelectedFile();
				filename = file.getName();

				if(file.getName().endsWith(".csv")) {
					keywords = KeywordReader.readCsv(file);
				}
                else if (file.getName().endsWith(".txt")) {
                    keywords = KeywordReader.readTxt(file);
                }
				else {
					keywords = KeywordReader.readPdf(file);
				}

				console.append("Input File Selected: " +  filename + NEWLINE);

				//NOTE: keyword csv format?
				//List> wordList = new ArrayList<ArrayList>();
				for (String s : keywords) {
					try {
						invokeProcedures(s);
					} catch (Exception t) {}
				}

			}
			else {
				console.append("Input File Selection cancelled" + NEWLINE);
			}
		}
	}

	/**
	 * main method creates GUI
	 **/
	public static void main(String[] args) throws IOException {
        if (args.length == 0) {
        //creating GUI
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    } else {
        GUI gui = new GUI();
        gui.invokeProcedures(args[0]);
    }
}

	/**
	 * The entire data processing procedure for a single keyword
	 *
	 * @param keyword - keyword is a list of keywords, with AND or OR operator already inserted
	 */
	public void invokeProcedures(String keyword) throws IOException{
		//Parser parser = new Parser();
		
		//STEP 0: check mongoDB connection
		DB db = null;
		try {
			System.out.print("Connecting to the database ...\n");
			MongoClient mongoClient = new MongoClient( DB_HOST , DB_PORT );	//connect to given DB using given port //throws UnknownHostException
			db = mongoClient.getDB( DB_NAME );//use given database
		}
		catch(UnknownHostException e){
			System.out.print("Cannot connect to MongoDB\n");
			return;
		}
		
		
		
		//Step 0a: turn into concepts
		
		//System.out.println(keyword);
		//String[] concepts = parser.getConcepts(keyword);
		//System.out.println("concepts: " + Arrays.toString(concepts));
		
		
		//STEP 1: call Bing search
		System.out.print("Searching the Internet ...\n");
		JSONArray results = Bing.search(keyword);
		ArrayList<String> urls = new ArrayList<String>();
		for(Object res : results){
			JSONObject obj = (JSONObject)res;
			urls.add((String)(obj.get("Url"))); //get URL (string) from JSON object
		}
		System.out.print(results.size());
		System.out.print(" URLs returned\n");

		//System.out.print("Connecting to the database ...\n");
		//MongoClient mongoClient = new MongoClient( DB_HOST , DB_PORT );	//connect to given DB using given port //throws UnknownHostException
//		DB db = mongoClient.getDB( DB_NAME );							//use given database
		String entry = keyword.replaceAll("\\s+","_");
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<entry.length(); i++ ){
			char c = entry.charAt(i);
			if(c=='&' || c=='|'){
				sb.append('_');
			}
			else{
				sb.append(c);
			}
		}

        String query = sb.toString();
		System.out.println(query);
        if (query.length() > 123)
            query = query.substring(0, 123);
		DBCollection collection = db.getCollection(query); //use keyword (without space) as collection name; create such collection if not exist



		try {
			File dir = new File(OUTPUT_DIR);
			if (!dir.exists()) {
				try{
					dir.mkdir();
				        //result = true;
				} catch(SecurityException se){
				        //handle it
				}
			}

			PrintWriter out = new PrintWriter(OUTPUT_DIR + "/" + query + ".txt");

			for(String url : urls){
				//STEP 3: extract data
				System.out.print("Extracting full text from ");
				System.out.print(url);
				System.out.print(" ...\n");

				String fulltext = null;
				try{//attempt to get full text
					fulltext = HtmlParser.getFullText(url);
				}
				catch(IOException e){//cannot get full text
					System.out.print("Cannot get full text from ");
					System.out.print(url);
					System.out.println("...Skip");
					continue;
				}

				//STEP 4: NLP

				// Insert into Lucene
//				System.out.print("Inserting into Lucene ... ");
//				Document doc = new Document();
//				doc.add(new TextField(url, fulltext, Field.Store.YES));
//				iwriter.addDocument(doc);

				//STEP 5: insert data to mongoDB
				System.out.print("Inserting data to database ... ");
				//tagging
				BasicDBObject dbObj = new BasicDBObject(FULLTEXT_INDEX, fulltext).append(URL_INDEX, url);
				if(fulltext.toLowerCase().contains(SAMPLE_TAG.toLowerCase())){
					dbObj.append(SAMPLE_TAG_INDEX, SAMPLE_TAG);
				}
				collection.insert(dbObj);
				System.out.print("done\n");

				//Extra: Output the url in a file
				out.println(url);
			}
//			try {
////			    DirectoryReader ireader = DirectoryReader.open(directory);
////			    IndexSearcher isearcher = new IndexSearcher(ireader);
////			    // Parse a simple query that searches for "text":
////			    QueryParser queryParser = new QueryParser("fieldname", analyser);
////			    Query qQuery = queryParser.parse("text");
////			    ScoreDoc[] hits = isearcher.search(qQuery, null, 1000).scoreDocs;
////	
////			    // Iterate through the results:
////			    for (int i = 0; i < hits.length; i++) {
////			      Document hitDoc = isearcher.doc(hits[i].doc);
////			      System.out.println(hitDoc.get("fieldname"));
////			    }
////				iwriter.close();
////			    ireader.close();
////			    directory.close();
//			} catch (Exception y) {}
			
			
			out.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
}
