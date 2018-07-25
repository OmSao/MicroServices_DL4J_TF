package oracle.gat.runtime.microservices;

import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class VectorMeanComparison {
    //private static Logger log = LoggerFactory.getLogger(TEST_VectorMeanForSentences.class);
	private static VectorMeanComparison VMC_Obj;
	public static String exceptionMessage = "Similarity score calculated";
	public static List<String> helpingVerbs =  Arrays.asList("am", "are", "is", "was", "were", "be", "being", "been", "have", "has", "had", "having", "can", "could", "may", "might", "shall", "will", "would", "should", "must", //supporting verbs
			"do", "does", "did", "to", "of", //
			"for", "and", "nor", "no", "but", "or", "yet", "so" 
			);
	
	private static Word2Vec vecGoogle;
	private VectorMeanComparison() {
		System.out.println("Inside VectorMeanComparison Private Constructor");
	}
	
	public static VectorMeanComparison getInstance(){
        if(VMC_Obj == null){
        	VMC_Obj = new VectorMeanComparison();
        	System.out.println("VMC_Obj is null, Creating Neural Network Model in Singleton way!");
        	System.out.println("<-----------Vectorizatioin Started------------>");
        	long googleVectoringStartTime = System.currentTimeMillis();
            File gModel = new File("/scratch/thlai/OSAO/GoogleNews-vectors-negative300.bin.gz");
            //File gModel = new File("D://iRobot//GoogleNews-vectors-negative300.bin.gz");
            VMC_Obj.vecGoogle = WordVectorSerializer.readWord2VecModel(gModel);
            	long googleVectoringEndTime = System.currentTimeMillis();
            	System.out.println("Vectorization finished, took:==>"+(googleVectoringEndTime - googleVectoringStartTime) + " ms");
            	System.out.println("<-----------Vectorizatioin Finished------------>");
        	
        	return VMC_Obj;
        }
        else
        	return VMC_Obj;
    }
	
    //public static void main(String s1, String s2) throws Exception {
	public double Main(String s1, String s2) throws Exception {
		
		System.out.println("-----------MAIN FUNCTION------------");
        //System.out.println("sentence1==>"+s1);
        //System.out.println("sentence2==>"+s2);

        return cosineSimForSentence(vecGoogle, s1, s2);
    }
    
	public static Collection<String> nearestWords(String word, int numberOfWords){
		
		return vecGoogle.wordsNearest(word, numberOfWords);
		
	}
    
    public static double cosineSimForSentence(Word2Vec vector, String sentence1, String sentence2){
    	//Collection<String> label1 = Splitter.on(' ').splitToList(sentence1.toLowerCase().replaceAll("[^A-Za-z0-9 ]", ""));
    	List<String> label1 = new LinkedList<String>(Splitter.on(' ').splitToList(sentence1.toLowerCase().replaceAll("[^A-Za-z ]", "")));
        List<String> label2 = new LinkedList<String>(Splitter.on(' ').splitToList(sentence2.toLowerCase().replaceAll("[^A-Za-z ]", "")));
        
        if(label1.size() <= 2 || label2.size() <= 2 || (similarity(sentence1, sentence2) >= 0.7) ){
        	return similarity(sentence1, sentence2);
        }
        else{
	        label1 = removeHelpingVerbs(label1);
	        label1 = removeHelpingVerbs(label1);
	        System.out.println("After processing sentences:==>"+ label1 + "--vs--" + label2);
	        //System.out.println("LayerSize:==>"+vector.getLayerSize());
	        //System.out.println("LayerSize:==>"+vector.getMinWordFrequency());
	        //System.out.println(vector.wordsNearest("foods", 10));
	        //return Transforms.cosineSim(vector.getWordVectorsMean(label1), vector.getWordVectorsMean(label2));
	        try{
	        	return Transforms.cosineSim(vector.getWordVectorsMean(label1), vector.getWordVectorsMean(label2));
	        }catch(Exception e){
	        	exceptionMessage = e.getMessage();
	        }
	        return Transforms.cosineSim(vector.getWordVectorsMean(label1), vector.getWordVectorsMean(label2));
    	}
    }
    
    public static List<String> removeHelpingVerbs(List<String> label){
		for(int i=0; i<helpingVerbs.size(); i++){
			//for(String s : helpingVerbs){
				if(label.contains(helpingVerbs.get(i))){
					label.remove(label.indexOf(helpingVerbs.get(i)));
					System.out.println("Words Eliminated:===>"+helpingVerbs.get(i));
				}
			}
		return label;
		
	}
    
    	/**
	   * Calculates the similarity (a number within 0 and 1) between two strings.
	   */
	  public static double similarity(String s1, String s2) {
	    String longer = s1, shorter = s2;
	    if (s1.length() < s2.length()) { // longer should always have greater length
	      longer = s2; shorter = s1;
	    }
	    int longerLength = longer.length();
	    if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
	    /* // If you have StringUtils, you can use it to calculate the edit distance:
	    return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
	                               (double) longerLength; */
	    return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

	  }

	  // Example implementation of the Levenshtein Edit Distance
	  // See http://rosettacode.org/wiki/Levenshtein_distance#Java
	  public static int editDistance(String s1, String s2) {
	    s1 = s1.toLowerCase();
	    s2 = s2.toLowerCase();

	    int[] costs = new int[s2.length() + 1];
	    for (int i = 0; i <= s1.length(); i++) {
	      int lastValue = i;
	      for (int j = 0; j <= s2.length(); j++) {
	        if (i == 0)
	          costs[j] = j;
	        else {
	          if (j > 0) {
	            int newValue = costs[j - 1];
	            if (s1.charAt(i - 1) != s2.charAt(j - 1))
	              newValue = Math.min(Math.min(newValue, lastValue),
	                  costs[j]) + 1;
	            costs[j - 1] = lastValue;
	            lastValue = newValue;
	          }
	        }
	      }
	      if (i > 0)
	        costs[s2.length()] = lastValue;
	    }
	    return costs[s2.length()];
	  }

	  public static void printSimilarity(String s, String t) {
	    System.out.println(String.format(
	      "%.16f is the similarity between \"%s\" and \"%s\"", similarity(s, t), s, t));
	  }
}
