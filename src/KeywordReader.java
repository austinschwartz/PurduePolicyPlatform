
import java.io.BufferedReader;  
import java.io.FileNotFoundException;  
import java.io.FileReader;  
import java.io.IOException; 
import java.util.ArrayList;

import java.io.File;
import java.io.FileInputStream;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * Java program that can read .csv file and .pdf file to obtain keywords
 *  
 * @author huanyi_guo
 * 
 */
public class KeywordReader{
	/**
	 * Pre-defined constant
	 */
	private static final String CSV_DELIMITER = ","; //the delimiter used by csv file
	private static final int PDF_STARTINGPAGE = 1;

	/**
	 * readCsv - Read given csv file and return keywords from this file
	 * 
	 * @param csvFileToRead - name of file to read
	 * @return keywords - array list of strings that includes keywords from the file. Return null if file reading is unsuccessful.
	 */
	public static ArrayList<String> readCsv(File csvFileToRead){
		
		ArrayList<String> keywords = null;
		String line;	//each line in the csv file
		BufferedReader br = null;
		
		try{
			br = new BufferedReader(new FileReader(csvFileToRead));
			keywords = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				for(String s : line.split(CSV_DELIMITER)){
					keywords.add(s);
				}
			}
		}
		catch(FileNotFoundException e){
			System.out.println("readCsv: FileNotFound");
			e.printStackTrace();
		}
		catch(IOException e){
			System.out.println("readCsv: IOException - read");
			e.printStackTrace();
		}
		finally{
			if(br != null){
				try{
					br.close();
				}
				catch(IOException e){
					System.out.println("readCsv: IOException - close");
					e.printStackTrace();
				}
			}
		}
		
		return keywords;
	}
	
	//NOTE: unnecessary space

	/**
	 * readPdf - Read given PDF file. Ignore image and extract whole text. Takenize the whole text and treat each word as one keyword
	 *
	 * @param pdfFileToRead - name of pdf file to read
	 * @return keywords - array list of strings that includes keywords from the file. Return null if file reading is unsuccessful.
	 */
	public static ArrayList<String> readPdf(File pdfFileToRead){
		ArrayList<String> keywords = null;
		PDFParser parser;
		COSDocument cosDoc = null;
		PDDocument pdDoc = null;
		PDFTextStripper textStripper;
		String wholeText, pdfDelimiter;

		try{
			parser = new PDFParser(new FileInputStream(pdfFileToRead));
			parser.parse();
			cosDoc = parser.getDocument();
			textStripper = new PDFTextStripper();
			pdDoc = new PDDocument(cosDoc);
			textStripper.setStartPage(PDF_STARTINGPAGE);
			textStripper.setEndPage(textStripper.getEndPage());
			wholeText = textStripper.getText(pdDoc);	
			pdfDelimiter = textStripper.getWordSeparator();
			keywords = new ArrayList<String>();
			for(String s : wholeText.split(pdfDelimiter)){
				keywords.add(s);
			}
		}
		catch(IOException e){
			System.out.println("readPdf: IOException - read");
			e.printStackTrace();
		}
		finally{
			try{
				if(cosDoc != null){
					cosDoc.close();
				}
				if(pdDoc != null){
					pdDoc.close();
				}
			}
			catch(IOException e){
				System.out.println("readPdf: IOException - close");
				e.printStackTrace();
			}
		}  

		return keywords;
	}
	
	/**
	 * main - Main method is for testing only
	 */
	public static void main(String[] args){
		ArrayList<String> keywords;// = KeywordReader.readCsv("file1.csv");


		System.out.println("Reading PDF...");
		keywords = KeywordReader.readPdf(new File("file2.pdf"));
		//System.out.println(keywords.size());
		for(String s : keywords){
			System.out.println(s);
		}
	}
	

}
