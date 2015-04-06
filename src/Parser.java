
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

public class Parser {
	public static void main(String args[]){
		InputStream modelIn = null;
		ParserModel model = null;
		System.out.println("Loading model ...");
		try {
			modelIn = new FileInputStream("../nlp_model/en-parser-chunking.bin");
			model = new ParserModel(modelIn);
			opennlp.tools.parser.Parser parser = ParserFactory.create(model);

			//get the sentence and count number of words
			BufferedReader br = new BufferedReader(new FileReader("../input/questions.txt"));
		    //StringBuilder sbuilder = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		    	//sbuilder.append(line);//
		    	System.out.print("\nRead Sentence: ");
		    	System.out.println(line);
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
					System.out.println(nns[i].toString());
				}

				//read another line
				line = br.readLine();
		    }//end of reading file
		    //String everything = sbuilder.toString();
		    br.close();

			//String testcase = "What are the challenges in applying more systematic approaches to characterizing and communicating uncertainty in the assessment of a drug's benefits and risks.";
			//
			//parse the string




			//System.out.println(sb.toString());



			modelIn.close();
			System.out.println("\nDone");
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

		return;
	}

}
