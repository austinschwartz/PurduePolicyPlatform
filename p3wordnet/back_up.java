import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class nlp {
	public static void main(String[] args) throws IOException {
		//JavaRDD<String> file = spark.textFile("hdfs://...");

		
		//String webContent = GetWebContent.getWeb("http://www.nature.com/nrd/journal/v13/n6/full/nrd4309.html");
		//Document doc;

		//doc = Jsoup.parse(webContent);

		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, regexner");
	    /*
			RegexNER as needed in the future, ref: http://nlp.stanford.edu/software/regexner/
			props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
			props.put("regexner.mapping", "XXX.txt");
	    */
		props.put("regexner.mapping", "./jg-regexner.txt");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	    //String text = "Maintaining research and development (R&D) productivity at a sustainable level is one of the main challenges currently facing the pharmaceutical industry. In this article, we discuss the results of a comprehensive longitudinal review of AstraZeneca's small-molecule drug projects from 2005 to 2010. The analysis allowed us to establish a framework based on the five most important technical determinants of project success and pipeline quality, which we describe as the five 'R's: the right target, the right patient, the right tissue, the right safety and the right commercial potential.";
	    String text = "What are the challenges in applying more systematic approaches to characterizing and communicating uncertainty in the assessment of a drug’s benefits and risks. What are the potential systematic approaches to address uncertainty faced by regulators in the assessment of benefits and risks in pharmaceuticals, drawing from various scientific and regulatory disciplines and domains.  What are the possible principles, best practices, and resources that can facilitate the development, evaluation, and incorporation of such approaches in regulatory decision-making.  Explore principles and approaches to facilitate the communication about uncertainty in the assessment of benefits and risks with FDA stakeholders. Provide an overview of regulatory strategies for communicating benefits and risks of pharmaceutical products and clarify the drug regulator’s role in communicating uncertainty FDA’s patient-focused drug development initiative and the different ways in which FDA receives information from different stakeholders and incorporates this information into addressing the relevant uncertainties in the assessment of benefits and risks. How can the patient voice inform how much uncertainty can be tolerated? How do we communicate information about what is known and unknown about benefits and risks as that information changes?";

	    //doc.body().text();

	    // create an empty Annotation just with the given text
    	Annotation document = new Annotation(text);

    	// run all Annotators on this text
    	pipeline.annotate(document);

    	// these are all the sentences in this document
    	// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
    	List<CoreMap> sentences = document.get(SentencesAnnotation.class);

    	//HashMap that help store the Set
    	HashMap<String, Set<String>> duplicate_helper = new HashMap<String, Set<String>>();

    	for(CoreMap sentence: sentences) {
    		List<Integer> Verb_List = new ArrayList<Integer>();
    		List<Integer> KeyWord_List = new ArrayList<Integer>();
    		boolean keyword_found = false;
    		// traversing the words in the current sentence
      		// a CoreLabel is a CoreMap with additional token-specific methods

    		for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
    			System.out.print(token.get(TextAnnotation.class) + " ");
    		}
    		System.out.println();
    		System.out.println();
    		System.out.println();

	      	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        	// this is the text of the token
	        	String word = token.get(TextAnnotation.class);
	        	// this is the POS tag of the token
	        	String pos = token.get(PartOfSpeechAnnotation.class);
	        	// this is the NER label of the token
	        	String ne = token.get(NamedEntityTagAnnotation.class);

	        	//System.out.println(word + "    " + ne);
	        	if(isVerb(pos)){
	        		//System.out.print(word+" ");
	        		Verb_List.add(token.index());
	        	}	

	        	if(isAdj(pos)){
	        		//System.out.print(word+" ");
	        	}

	        	if(iskeyword(ne)){
	        		KeyWord_List.add(token.index());
	        		keyword_found = true;
	        	}

	        	if(isNoun(pos)){
	        		//System.out.println("Target   "+ token.get(LemmaAnnotation.class) +":");
	        		System.out.println(token.get(LemmaAnnotation.class) + "-------------------");

        			Set<String> sS = wordNet.getSynonyms(token.get(LemmaAnnotation.class));
        			sS.addAll(wordNet.getHypernyms(token.get(LemmaAnnotation.class)));
    				
    				for(String str : sS){
    					if(!duplicate_helper.containsKey(str))
    						duplicate_helper.put(str, sS);
    					else
    						duplicate_helper.get(str).addAll(sS);
    				}
	        		//System.out.println();
	        		//System.out.println();
	        	}
	      	}
	      	System.out.println();
	      	// this is the parse tree of the current sentence
	      	//Tree tree = sentence.get(TreeAnnotation.class);
			//tree.indentedListPrint();
	      	

	      	// this is the Stanford dependency graph of the current sentence
	      	if(true){
	      		SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);

	      		//findSVO(dependencies, KeyWord_List, Verb_List);

	      		//dependencies.prettyPrint();

	      		System.out.println();
		      	//List<IndexedWord> Sl = extractSubject(dependencies);
		      	//List<IndexedWord> Ol = extractObject(dependencies);
		      	//findSVOII(dependencies, KeyWord_List, Sl, Ol);

		      	/*for(List<IndexedWord> path : SVO_list){
		      		for(IndexedWord word : path){
		      			System.out.print(word.toString("WORD_FORMAT") + " ");
		      		}
		      		System.out.println();
		      	}*/

	      	}

	      	System.out.println();
	      	System.out.println();
	      	System.out.println("-----------------------------------------------");
	      	System.out.println();
	      	System.out.println();
    	}

    	//For printing out the Sets
    	Set<Set<String>> temp = new HashSet<Set<String>>();
    	for(Set<String> s : duplicate_helper.values()){
    		temp.add(s);
    	}
    	for(Set<String> s : temp){
    		System.out.println(s);
    	}

    	// This is the coreference link graph
    	// Each chain stores a set of mentions that link to each other,
    	// along with a method for getting the most representative mention
    	// Both sentence and token offsets start at 1!
    	//Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
	}


	private static List<List<IndexedWord>> SVO_list = new ArrayList<List<IndexedWord>>();

	/*public static void Find_Sentence(Tree cur) {
		if (cur.value().equals("S")){
			ArrayList<Tree> subject = null, verb = null, object = null;

			for (Tree child : cur.getChildrenAsList()){
				if(child.value().equals("NP")){
					subject = extract_subject(child);
				}
				else if(cur.getChildren()[i].getType().equals("VP")){
					//extract verb
					verb = extract_verb(child);
					object = extract_object(verb);
				}
			}
			String SVO = "";
			if(subject != null){
				for(Tree obj : subject)
					SVO += obj.value() + " ";
			}
			else
				SVO += "Empty";

			if(verb != null){
				for(Tree obj : verb)
					SVO += obj.value() + " ";
			}
			else
				SVO += "Empty";

			if(object != null){
				for(Tree obj : object)
					SVO += obj.value() + " ";
			}
			else
				SVO += "Empty";

			SVO_list.add(SVO);
			System.out.println(subject.value() + " | " + verb.value() + " | " + object.value());
			return;
		}

		for (Tree child : cur.getChildrenAsList()){
			Find_Sentence(child);
		}
		return;
	}*/


	private static void findSVO(SemanticGraph dependencies, List<Integer> KeyWord_List, List<Integer> Verb_List){
		for(int verbIndex : Verb_List){
			int min_distance = Integer.MAX_VALUE;
			IndexedWord verb = dependencies.getNodeByIndex(verbIndex);
			List<IndexedWord> targetPath = null;
			for(int i = 0; i < KeyWord_List.size(); i++){
				IndexedWord keywordI = dependencies.getNodeByIndex(KeyWord_List.get(i));
				for(int j = i+1; j < KeyWord_List.size(); j++){
					IndexedWord keywordII = dependencies.getNodeByIndex(KeyWord_List.get(j));

					//verbIndex has to be in the middle between two key works, if not, abandon this combination.
					if(verbIndex < Math.min(KeyWord_List.get(i), KeyWord_List.get(j)) || verbIndex > Math.max(KeyWord_List.get(i), KeyWord_List.get(j)))
						continue;

					List<IndexedWord> cur_path = new ArrayList<IndexedWord>();

					List<IndexedWord> temp;

					temp = dependencies.getShortestDirectedPathNodes(verb, keywordI);

					if(temp == null) continue;

					for (IndexedWord word : temp){
						if(!cur_path.contains(word))
							cur_path.add(word);
					}

					temp = dependencies.getShortestDirectedPathNodes(verb, keywordII);

					if(temp == null) continue;

					for (IndexedWord word : temp){
						if(!cur_path.contains(word))
							cur_path.add(word);
					}

					if(cur_path.size() < min_distance){
						min_distance = cur_path.size();
						targetPath = cur_path;
						Collections.sort(cur_path, new CustomComparator());
					}
				}
			}
			if(targetPath != null){
				SVO_list.add(targetPath);
			}
		}
	}

	private static void findSVOII(SemanticGraph dependencies, List<Integer> KeyWord_List, List<IndexedWord> Sl, List<IndexedWord> Ol){
		for(int KeyWord_index : KeyWord_List){
			IndexedWord keyword = dependencies.getNodeByIndex(KeyWord_index);

			int Smin = Integer.MAX_VALUE, Omin = Integer.MAX_VALUE;
			IndexedWord sbjTarget = null, objTarget = null;

			List<IndexedWord> Path;

			for(IndexedWord subject : Sl){
				if(subject.equals(keyword)) continue;
				int distance = Math.abs(keyword.index() - subject.index());
				if(distance < Smin && distance > 0 && dependencies.commonAncestor(subject, keyword) > 0){
					sbjTarget = subject;
				}
			}
			
			if(sbjTarget != null){
				Path = dependencies.getShortestDirectedPathNodes(dependencies.getCommonAncestor(sbjTarget, keyword), keyword);
				Path.addAll(dependencies.getShortestDirectedPathNodes(dependencies.getCommonAncestor(sbjTarget, keyword), sbjTarget));
				Collections.sort(Path, new CustomComparator());
				for (IndexedWord word : Path){
					System.out.print(word.toString("WORD_FORMAT") + " ");
				}
				System.out.println();
			}

			for(IndexedWord object : Ol){
				if(object.equals(keyword)) continue;
				int distance = Math.abs(keyword.index() - object.index());
				if(distance < Omin && distance > 0 && dependencies.commonAncestor(object, keyword) > 0){
					objTarget = object;
				}
			}

			if(objTarget != null){
				Path = dependencies.getShortestDirectedPathNodes(dependencies.getCommonAncestor(objTarget, keyword), keyword);
				Path.addAll(dependencies.getShortestDirectedPathNodes(dependencies.getCommonAncestor(objTarget, keyword), objTarget));
				Collections.sort(Path, new CustomComparator());
				for (IndexedWord word : Path){
					System.out.print(word.toString("WORD_FORMAT") + " ");
				}
				System.out.println();
			}

			return;
		}
	}

	private static List<IndexedWord> extractSubject(SemanticGraph dependencies){
		ArrayList<IndexedWord> subject = new ArrayList<IndexedWord>();

		for(SemanticGraphEdge sge: dependencies.findAllRelns(EnglishGrammaticalRelations.CLAUSAL_PASSIVE_SUBJECT)){
			if(!subject.contains(sge.getTarget()))subject.add(sge.getTarget());
		}

		for(SemanticGraphEdge sge: dependencies.findAllRelns(EnglishGrammaticalRelations.CLAUSAL_SUBJECT))
			if(!subject.contains(sge.getTarget())) subject.add(sge.getTarget());		

		for(SemanticGraphEdge sge: dependencies.findAllRelns(EnglishGrammaticalRelations.CONTROLLING_SUBJECT))
			if(!subject.contains(sge.getTarget())) subject.add(sge.getTarget());

		for(SemanticGraphEdge sge: dependencies.findAllRelns(EnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT))
			if(!subject.contains(sge.getTarget())) subject.add(sge.getTarget());

		for(SemanticGraphEdge sge: dependencies.findAllRelns(EnglishGrammaticalRelations.NOMINAL_SUBJECT))
			if(!subject.contains(sge.getTarget())) subject.add(sge.getTarget());

		for(SemanticGraphEdge sge: dependencies.findAllRelns(EnglishGrammaticalRelations.SUBJECT))
			if(!subject.contains(sge.getTarget())) subject.add(sge.getTarget());

		for(IndexedWord word : subject){
			System.out.println(word.toString("WORD_FORMAT") + " ");
		}
		System.out.println();
		
		return subject;
	}

	private static List<IndexedWord> extractObject(SemanticGraph dependencies){
		ArrayList<IndexedWord> object = new ArrayList<IndexedWord>();

		for(SemanticGraphEdge sge: dependencies.findAllRelns(EnglishGrammaticalRelations.ADJECTIVAL_COMPLEMENT)){
			if(!object.contains(sge.getTarget())) object.add(sge.getTarget());
		}

		for(SemanticGraphEdge sge: dependencies.findAllRelns(EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT))
			if(!object.contains(sge.getTarget())) object.add(sge.getTarget());		

		for(SemanticGraphEdge sge: dependencies.findAllRelns(EnglishGrammaticalRelations.DIRECT_OBJECT))
			if(!object.contains(sge.getTarget())) object.add(sge.getTarget());

		for(SemanticGraphEdge sge: dependencies.findAllRelns(EnglishGrammaticalRelations.INDIRECT_OBJECT))
			if(!object.contains(sge.getTarget())) object.add(sge.getTarget());

		for(SemanticGraphEdge sge: dependencies.findAllRelns(EnglishGrammaticalRelations.PREPOSITIONAL_OBJECT))
			if(!object.contains(sge.getTarget())) object.add(sge.getTarget());

		for(SemanticGraphEdge sge: dependencies.findAllRelns(EnglishGrammaticalRelations.XCLAUSAL_COMPLEMENT))
			if(!object.contains(sge.getTarget())) object.add(sge.getTarget());		

		for(IndexedWord word : object){
			System.out.println(word.toString("WORD_FORMAT") + " ");
		}
		System.out.println();

		return object;
	}

	private static boolean iskeyword(String ne){
		if(ne.equals("ORGANIZATION") || ne.equals("DRUG") || ne.equals("KEYWORD")) {
			return true;
		}
		else
			return false;
	}

	private static boolean isVerb(String word){
		if( word.equals("VB")  || word.equals("VBD") || word.equals("VBG") || word.equals("VBN") || word.equals("VBP") || word.equals("VBZ"))
			return true;
		else
			return false;
	}

	private static boolean isNoun(String word){
		if( word.equals("NN")  || word.equals("NNS") || word.equals("NNP") || word.equals("NNPS") )
			return true;
		else
			return false;
	}

	private static boolean isAdj(String word){
		if( word.equals("JJ") || word.equals("JJR") || word.equals("JJS") )
			return true;
		else
			return false;
	}	

	private static class CustomComparator implements Comparator<IndexedWord> {
    	public int compare(IndexedWord o1, IndexedWord o2) {
        	return o1.compareTo(o2);
    	}
	}

}