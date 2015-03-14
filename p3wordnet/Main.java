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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

public class Main {

	private static List<List<IndexedWord>> SVO_list = new ArrayList<List<IndexedWord>>();

	private static StanfordCoreNLP pipeline;

	private static HttpSolrServer solrServer;

	private static void initializeCoreNLP(){
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, regexner");
	    /*
			RegexNER as needed in the future, ref: http://nlp.stanford.edu/software/regexner/
			props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
			props.put("regexner.mapping", "XXX.txt");
	    */
		props.put("regexner.mapping", "./jg-regexner.txt");
	    
	    pipeline = new StanfordCoreNLP(props);

	}

	private static void initializeSolrServer() throws IOException, SolrServerException{
		solrServer = new HttpSolrServer("http://localhost:8983/solr");
	}

	private static Set<Set<String>> conceptClassification(String text){

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
    		// traversing the words in the current sentence
      		// a CoreLabel is a CoreMap with additional token-specific methods

	      	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        	// this is the POS tag of the token
	        	String pos = token.get(PartOfSpeechAnnotation.class);

	        	if(isNoun(pos)){
        			Set<String> sS = wordNet.getSynonyms(token.get(LemmaAnnotation.class));
        			sS.addAll(wordNet.getHypernyms(token.get(LemmaAnnotation.class)));
    				
    				for(String str : sS){
    					if(!duplicate_helper.containsKey(str))
    						duplicate_helper.put(str, sS);
    					else
    						duplicate_helper.get(str).addAll(sS);
    				}
	        	}
	      	}
    	}

    	//*********************** For testing ***********************
    	Set<Set<String>> kySet = new HashSet<Set<String>>();
    	//This is not a redundant step, some of the keywords may point to the same value.
    	for(Set<String> s : duplicate_helper.values()){
    		kySet.add(s);
    	}

    	return kySet;
	}

	//sentenceFilter is used to parse each sentence and filter out all the noun
	private static ArrayList<String> sentenceFilter(String text){
		ArrayList<String> words = new ArrayList<String>();

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
    		// traversing the words in the current sentence
      		// a CoreLabel is a CoreMap with additional token-specific methods

	      	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        	// this is the POS tag of the token
	        	String pos = token.get(PartOfSpeechAnnotation.class);

	        	if(isNoun(pos) || isAdj(pos)){
	        		words.add(token.get(LemmaAnnotation.class));
	        	}
	        }
	    }

	    return words;
	}

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

	private static String bindString(ArrayList<Set<String>> result){
		String str = "";
		for(Set<String> s : result){
			for(String t : s){
				str += t + " ";
			}
		}
		return str;
	}

	private static HashMap<String, ArrayList<Set<String>>> topicAnalyzer(String topic){

		Set<Set<String>> concepts = conceptClassification(topic);

		for(Set<String> concept: concepts)
			System.out.println(concept);
		System.out.println();
		System.out.println();

		String questions[] = topic.split("\n");

		HashMap<String, ArrayList<Set<String>>> classification = new HashMap<String, ArrayList<Set<String>>>();
		HashMap<String, Set<String>> helper = new HashMap<String, Set<String>>();

		for(String question: questions){
			classification.put(question, new ArrayList<Set<String>>());
		}

		for(Set<String> concept : concepts){
			for(String word : concept){
				helper.put(word, concept);
			}
		}

		for(String question: questions){
			ArrayList<String> words = sentenceFilter(question);
			for(String word : words){
				if(helper.containsKey(word) && !classification.get(question).contains(helper.get(word))){
					classification.get(question).add(helper.get(word));
				}
			}
		}

		return classification;
	}

	public static void main(String[] args) throws IOException, SolrServerException {
		
		initializeCoreNLP();
		initializeSolrServer();

		System.out.println("\n\n\n");
		System.out.println("----------------------------------Initialization Success---------------------------------------");

		ArrayList<String> topics = new ArrayList<String>();
		String topic1 = "What are the challenges in applying more systematic approaches to characterizing and communicating uncertainty in the assessment of a drug’s benefits and risks.\n What are the potential systematic approaches to address uncertainty faced by regulators in the assessment of benefits and risks in pharmaceuticals, drawing from various scientific and regulatory disciplines and domains.\nWhat are the possible principles, best practices, and resources that can facilitate the development, evaluation, and incorporation of such approaches in regulatory decision-making.\nExplore principles and approaches to facilitate the communication about uncertainty in the assessment of benefits and risks with FDA stakeholders.\nProvide an overview of regulatory strategies for communicating benefits and risks of pharmaceutical products and clarify the drug regulator’s role in communicating uncertainty.\nFDA’s patient-focused drug development initiative and the different ways in which FDA receives information from different stakeholders and incorporates this information into addressing the relevant uncertainties in the assessment of benefits and risks.\nHow can the patient voice inform how much uncertainty can be tolerated?\nHow do we communicate information about what is known and unknown about benefits and risks as that information changes?";
		String topic2 = "What are the different perspectives on Innovation and Value in Drug Discovery?\nwhat are the different paradigms of university Intellectual Property rights assignments in Drug Discovery?\nIs crowd sourcing prevalent in drug research and development?\nIs there an  open database to share failures in Drug Discovery?\nHow are people monetizing the long tail in Drug Discovery?";
		topics.add(topic1);
		topics.add(topic2);

		for(int i = 0; i < topics.size(); i++){
			System.out.println("Topic" + (i+1) + ":");

			HashMap<String, ArrayList<Set<String>>> classificationTab = topicAnalyzer(topics.get(i));

			//For testing
			for(String question : classificationTab.keySet()){

				System.out.println("\t" + question);
				for(Set<String> concept : classificationTab.get(question))
					System.out.println("\t\t" + concept);

				String query = "";
				ArrayList<String> temp = sentenceFilter(question);
				for(String s: temp){
					query += s + " ";
				}
				System.out.println("\t\tQuery String: " + query);
				for(Object job : Bing.search(query)){
					System.out.println("\t\t\t" + ((JSONObject)job).get("Url"));	
				}
			}
			System.out.println("\n\n\n");
		}
		/*
		for(String qes : question){
			Set<Set<String>> kwcResult = kwClassification(qes);
			String query = bindString(kwcResult);
			System.out.println("\n\n");
			System.out.println(query+"\n\n");

			String information = "";
			for(Object job : Bing.search(query)){
				//String webContent = GetWebContent.getWeb((JSONObject)job).get("Url"));
				//Document doc;
				//doc = Jsoup.parse(webContent);
				information += ((JSONObject)job).get("Url") + "\n";

				doc.addField("cat", "book");
		      	doc.addField("id", "book-" + i);
		      	doc.addField("name", "The Legend of the Hobbit part " + i);
		      	server.add(doc);
		      	if(i%100==0) server.commit();  // periodically flush
			}

			SolrInputDocument doc = new SolrInputDocument();

		}
		*/

	}

}