
/**
 * Parser will use NLP toolkit to parse a natural language question
 * and extract key words
 *
 * @author huanyi_guo
 *
 */
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Parser {
	InputStream modelIn;
	ParserModel model;
	opennlp.tools.parser.Parser parser;
	
	public Parser(String modelPath) {
		try {
			if (modelPath == "")
				modelIn = new FileInputStream("/Users/nawns/Dropbox/School/Spring2015/490DDP/PurduePolicyPlatform/nlp_model/en-parser-chunking.bin");
			else
				modelIn = new FileInputStream(modelPath);

			model = new ParserModel(modelIn);
			parser = ParserFactory.create(model);
			


		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		finally {
			if (modelIn != null) {
				try {
			      modelIn.close();
			    }
			    catch (IOException e) {
			    }
			}
		}
	}
	public Parser() {
		this("");
	}
	
	public String[] getConcepts(String line) {
		ArrayList<String> strs = new ArrayList<String>();
		
		System.out.println("Loading model ...");
		
    	//sbuilder.append(line);//
//    	System.out.print("\nRead Sentence: ");
//    	System.out.println(line);
    	System.out.println("\nParsing sentence ...");
    	int numberOfWords = line.split(" ").length;

    	Parse topParses[] = ParserTool.parseLine(line, parser, 1);
    	StringBuffer sb = new StringBuffer();
    	topParses[0].show(sb);
    	//parse the string;
		System.out.println("\nExtracting keywords ...\n");
		StringBuilder[] nns = new StringBuilder[numberOfWords];
		int nCount = 0;
		char c;
		for(int i = 0; i+2<sb.toString().length(); ){
			if(sb.charAt(i) == 'N' && sb.charAt(i+1)=='N' && sb.charAt(i+2) == ' '){//match NN
				i +=3; //first char of the word
				nns[nCount] = new StringBuilder();
				while( (c = sb.charAt(i++)) != ')'){
					nns[nCount].append(c);

				}
				nCount++;
			}
			else if(sb.charAt(i) == 'N' && sb.charAt(i+1)=='N' && sb.charAt(i+2) == 'S'){//match NNS
				i += 4;
				nns[nCount] = new StringBuilder();
				while( (c = sb.charAt(i++)) != ')'){
					nns[nCount].append(c);
				}
				nCount++;
			}
			else{
				i++;
			}
		}
		for(int i = 0; i<nCount; i++){
			strs.add(nns[i].toString());
		}
		
		String[] strArray = strs.toArray(new String[strs.size()]);
		
		return strArray;
	}
	
	
	public static void main(String args[]){
		Parser parse = new Parser();
		try {
			//get the sentence and count number of words
			BufferedReader br = new BufferedReader(new FileReader("../input/questions.txt"));
		    //StringBuilder sbuilder = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		    	System.out.println(parse.getConcepts(line));
		    	
				line = br.readLine();
		    }//end of reading file
		    //String everything = sbuilder.toString();
		    br.close();

			parse.modelIn.close();
			System.out.println("\nDone");
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return;
	}

}
