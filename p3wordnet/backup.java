/*private static ArrayList<String> SVO_list = new ArrayList<String>();

	//Traverse the whole parse tree to find all legal Sentence.
	public static void Find_Sentence(Tree cur) {
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


	/*
		To find the subject of the sentence, do search in the NP subtree. The subject
		will be found by performing BFS and selecting the first descendent of NP that is 
		a noun.
	
	private static ArrayList<Tree> extract_subject(Tree NP) {
		ArrayList<Tree> subjectList = new ArrayList<Tree>();
		LinkedList<Tree> queue = new LinkedList<Tree>();
		queue.add(NP);
		int cur_size = 1;
		Tree temp;
		while(!queue.isEmpty()){
			for(int i = 0; i < cur_size; i++){
				temp = queue.poll();
				if( isNoun(temp.value()) ){
					//Searching for possible nonc phrase
					Tree parent = temp.parent();
					for (int j = parent.objectIndexOf(temp); j < parent.numChildren(); j++){
						if()
					}
					return subjectList;
				}
				for(Tree child : temp.getChildrenAsList()){
					queue.add(child);
				}
			}
			cur_size = queue.size();
		}

		return null;
	}*/

	//Note: For the convenience of extract_object, return type is set to Parse
	/*
		For determing the predicate of sentence, search will be performed in the VP subtree.
		The deepest verb descendent of the verb phrase will give the verb as we want.
	
	private static ArrayList<Tree> extract_verb(Tree VP) {
		LinkedList<Tree> queue = new LinkedList<Tree>();
		queue.add(VP);
		int cur_size = 1;
		Tree temp, targetVerb = null;
		while(!queue.isEmpty()){
			for(int i = 0; i < cur_size; i++){
				temp = queue.poll();
				if(temp.value().equals("SBAR")){
					Find_Sentence(temp);
					continue;
				}
				if(temp.value().equals("PP")){
					continue;
				}
				if( isVerb(temp.value()) ){
						targetVerb = temp.children()[0];
						continue;
				}
				
				for(Tree child : temp.getChildrenAsList()){
					queue.add(child);
				}
			}
			cur_size = queue.size();
		}

		return targetVerb;
	}*/

	/*
		These can be found in three different subtrees, all siblings of the VP subtree containing the predicate.
		The subtrees are PP(prepostional phrase), NP and ADJP(Adjective phrase). In NP and PP, we search for the
		first noun, while in ADJP we find the first adjective.
	
	private static Span extract_object(Tree verb) {
		//According the rule of parser, verb's parent has to be VP
		Tree VP = verb.parent();
		VP.objectIndexOf(verb);
		return extract_subject(VP);
	}

	private static boolean isNoun(String word){
		if( word.equals("NN") || word.equals("NNP") || word.equals("NNPS") || word.equals("NNS"))
			return true;
		else
			return false;
	}

	private static boolean isVerb(String word){
		if( word.equals("VB")  || word.equals("VBD") || word.equals("VBG") || word.equals("VBN") || word.equals("VBP") || word.equals("VBZ"))
			return true;
		else
			return false;
	}*/