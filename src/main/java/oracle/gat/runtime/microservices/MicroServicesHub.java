package oracle.gat.runtime.microservices;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import org.json.simple.DeserializationException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import org.json.simple.JSONObject;
import org.json.simple.JsonObject;
import org.json.simple.Jsoner;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime; 


@SuppressWarnings("deprecation")
@Path("")		//For parent contextual path. No need now.
public class MicroServicesHub {
	public static boolean ASC = true;
    public static boolean DESC = false;
	
	DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd/MMM/yyyy HH:mm:ss");
	
	/*
	 *POST
	 *URL: http://blr00avc.in.oracle.com:8080/MicroServices_DL4J_TF/get/svs
	 *Request JSON: {"sentence1": "Enter your name", "sentence2": "Enter your first name"}
	 *Response JSON: {"sentence1":"Enter your name","sentence2":"Enter your first name","message":"Similarity score calculated","similarityScore":"0.7142857142857143"}
	*/
	
	@SuppressWarnings("unchecked")
	@POST
    //@Path("/similarity/find")
	@Path("/svs")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findSentenceVsSentence(String requestJSONString) throws DeserializationException, ParseException {  //return type was Response
		System.out.println("------------------------SVS: Query Started[TimeStamp: "+dateTimeFormat.format(LocalDateTime.now())+"]----------------------");
		System.out.println("Request JSON String:==>"+requestJSONString);
		
		String response = null;
		JSONObject requestJSONObject = new JSONObject();  //null
		JSONObject responseJSONObject = new JSONObject();
		
		JSONParser parser = new JSONParser();
		requestJSONObject = (JSONObject) parser.parse(requestJSONString);
		
			//Object requestObject = Jsoner.deserialize(requestJSONString);
			//System.out.println("requestObject.toString():==>"+requestObject.toString());
			//if(requestJSONObject == null) System.out.println("requestJSONObject is NULL");
			
			
			/*if (requestJSONObject instanceof JsonObject) {
				requestJSONObject = (JsonObject)requestObject;
			}
			else {
				System.out.println("Request JSON:==>"+requestJSONString + " is NOT a valid JSON");
			}*/
		
			String sentence1 = null;
			if (requestJSONObject.get("sentence1") != null){ 
				sentence1 = requestJSONObject.get("sentence1").toString();
				responseJSONObject.put("sentence1", sentence1);
				System.out.println("sentence1:==>"+sentence1);
			}
			else {
				System.out.println("sentence1 is NULL. Aborting the call.");
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			
			String sentence2 = null;
			if (requestJSONObject.get("sentence2") != null) {
				sentence2 = requestJSONObject.get("sentence2").toString();
				responseJSONObject.put("sentence2", sentence2);
				System.out.println("sentence2:==>"+sentence2);
			}
			else {
				System.out.println("sentence2 is NULL. Aborting the call.");
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			
			VectorMeanComparison VMCObj = VectorMeanComparison.getInstance();
			
			try {
				responseJSONObject.put("similarityScore", Double.toString(VMCObj.Main(sentence1, sentence2)));
				responseJSONObject.put("message", "Similarity score calculated");
		      } catch (Exception e) {
				// TODO Auto-generated catch block
		    	  responseJSONObject.put("message", VMCObj.exceptionMessage);
				if(VMCObj.exceptionMessage.toString().equals("Indexes shouldn't be empty")){
					responseJSONObject.put("similarityScore", new Double("-1"));
					}
				e.printStackTrace();
		      	}
			System.out.println(":===> STRING FORMAT of responseJSONObject: "+responseJSONObject.toJSONString());
			System.out.println("------------------------SVS: Query Finished[TimeStamp: "+dateTimeFormat.format(LocalDateTime.now())+"]----------------------");
			
		    return Response.ok(responseJSONObject.toJSONString()).build();

			//return Response.status(Response.Status.BAD_REQUEST).build();	//OSAO: Changed Response.Status
	}
	
	
	/*
	 *POST
	 *URL: http://blr00avc.in.oracle.com:8080/MicroServices_DL4J_TF/get/svl
	 *Request JSON: {"sentence": "Enter your name", "list": ["Enter first name", "Enter application name", "Contact number entered"]}
	 *Response JSON: {"sentence":"Enter your name","maxSimilarityScore":0.7771732211112976,"mostSimilarSentence":"Enter application name","list":["Enter first name","Enter application name","Contact number entered"],"averageScore":0.6700412631034851,"deltasWithSentence":{"Contact number entered":{"message":"Similarity score calculated","similarityScore":0.48368316888809204},"Enter application name":{"message":"Similarity score calculated","similarityScore":0.7771732211112976},"Enter first name":{"message":"Similarity score calculated","similarityScore":0.7492673993110657}}}
	*/
	
	@SuppressWarnings("unchecked")
	@POST    
	@Path("/svl")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findSentenceVsList(String requestJSONString) throws DeserializationException, ParseException {  //return type was Response
		System.out.println("------------------------SVL: Query Started[TimeStamp: "+dateTimeFormat.format(LocalDateTime.now())+"]----------------------");
		System.out.println("Request JSON String:==>"+requestJSONString);

		JSONObject responseJSONObj = new JSONObject(); //JSON for response body
		JSONObject deltaJObj = new JSONObject(); //stores value of Key: deltalsWithSentence
		
		String sentence;	  
	    double holdCosSim = 0;
	    //Map <String,String>params = MicroServicesHub.queryToMap(httpExchange.getRequestURI().getQuery());
	    VectorMeanComparison VMCObj = VectorMeanComparison.getInstance();
	     
	    String jsonStringFromReqBody = requestJSONString; //IOUtils.toString(httpExchange.getRequestBody());
	    System.out.println("------SVL: Request body json:"+ jsonStringFromReqBody);
	     
	     JSONParser parser = new JSONParser();
	     JSONObject JSONObj = null;
	     try {
			Object jsonObjFromReqBody = parser.parse(jsonStringFromReqBody);
			JSONObj = (JSONObject)jsonObjFromReqBody;
			
	      } catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
	      }

	    sentence = (String) JSONObj.get("sentence");
	    System.out.println("---sentence:==>"+sentence);
	    	
	    	
	    JSONArray arrayOfSentencesFromJSON = (JSONArray) JSONObj.get("list");
	    System.out.println("---list:==>"+arrayOfSentencesFromJSON);
	    String[] DOMTextArray = new String[arrayOfSentencesFromJSON.size()];
	    //JSONArray msg = (JSONArray) jsonObject.get("messages");
        
	    Iterator<String> iterator = arrayOfSentencesFromJSON.iterator();
	       int i=0;
           while (iterator.hasNext()) {
	            	DOMTextArray[i]=iterator.next();
	                System.out.println(DOMTextArray[i]);
	                i++;
	            }
           System.out.println("---DOMTextArray:==>"+Arrays.asList(DOMTextArray));
	      	//}
	     
	     
	     
	     //response.append("sentence:==> "+params.get("sentence"));
	     responseJSONObj.put("sentence", sentence);
	     //response.append("\nlist:==> "+params.get("list"));
	     //jObj.put("list", params.get("list").replaceAll("%20", " "));  // space character in URL is '%20'
	     responseJSONObj.put("list", Arrays.asList(DOMTextArray));
	     
	     //response.append("\nstringArray:==> "+ Arrays.asList(DOMTextArray));
	     //jObj.put("stringArray", Arrays.asList(DOMTextArray));
	     
	     double similarityScoresArray[] = new double[DOMTextArray.length];
	     
	     
	     for(i=0;i<DOMTextArray.length; i++){
	    	 JSONObject holdJObj = new JSONObject();  //stores value of each child of deltasWithSentence Object  
	    	 										//Always create a new object reference with every iteration, otherwise you are updating same instance over and over again and adding reference to same object many times to array.
	        	try {
					holdCosSim = VMCObj.Main(sentence, DOMTextArray[i]);
					System.out.println("\n"+sentence +" -vs- "+ DOMTextArray[i] +"="+ holdCosSim);
					holdJObj.put("similarityScore", holdCosSim);
					holdJObj.put("message", "Similarity score calculated");
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					holdJObj.put("message", VMCObj.exceptionMessage);
					System.out.println(sentence +" -vs- "+ DOMTextArray[i]); 
					if(VMCObj.exceptionMessage.toString().equals("Indexes shouldn't be empty")){
						holdJObj.put("similarityScore", new Double("-1"));
						}
					System.out.println("----Exception occured:"+e.getMessage());  //e.getMessage();
					//e.printStackTrace();
				}
	        	
	        	//response.append("\n"+params.get("sentence") +" -vs- "+ DOMTextArray[i] +"="+ holdCosSim);
	        	deltaJObj.put(DOMTextArray[i], holdJObj);
	        	
	        	similarityScoresArray[i] = holdCosSim;
	        }
	     
	     //response.append("\n\nMost Similar:==>"+getMax(similarityScores));
	     responseJSONObj.put("maxSimilarityScore", new Double(getMax(similarityScoresArray)));
	     responseJSONObj.put("mostSimilarSentence", DOMTextArray[getMaxIndex(similarityScoresArray)]);
	     responseJSONObj.put("averageScore", new Double(getAverageScore(similarityScoresArray)));
	     responseJSONObj.put("deltasWithSentence", deltaJObj);

		System.out.println(":===> STRING FORMAT of responseJSONObject: "+responseJSONObj.toJSONString());
		System.out.println("------------------------SVL: Query Finished[TimeStamp: "+dateTimeFormat.format(LocalDateTime.now())+"]----------------------");
			
		return Response.ok(responseJSONObj.toJSONString()).build();

		//return Response.status(Response.Status.BAD_REQUEST).build();	//OSAO: Changed Response.Status
	}
	
	/*
	 *POST
	 *URL: http://blr00avc.in.oracle.com:8080/MicroServices_DL4J_TF/get/lvl
	 *Request JSON: {"list1": ["Enter first name", "Enter application name", "Contact number entered"], "list2": ["Enter first name", "Enter application name", "Contact number entered"]}
	 *Response JSON: {"listMatch":[{"sentence":"Enter your name","maxSimilarityScore":0.7492673993110657,"mostSimilarSentence":"Enter first name","deltasWithSentence":[{"Enter first name":{"message":"Similarity score calculated","similarityScore":0.7492673993110657}},{"Register Username":{"message":"Similarity score calculated","similarityScore":0.5294117647058824}},{"Go":{"message":"Similarity score calculated","similarityScore":0.06666666666666667}}],"averageScore":0.4484486102278716},{"sentence":"Submit","maxSimilarityScore":0.125,"mostSimilarSentence":"Enter first name","deltasWithSentence":[{"Enter first name":{"message":"Similarity score calculated","similarityScore":0.125}},{"Register Username":{"message":"Similarity score calculated","similarityScore":0.11764705882352941}},{"Go":{"message":"Similarity score calculated","similarityScore":0.0}}],"averageScore":0.08088235294117647},{"sentence":"Create Account","maxSimilarityScore":0.23529411764705882,"mostSimilarSentence":"Register Username","deltasWithSentence":[{"Register Username":{"message":"Similarity score calculated","similarityScore":0.23529411764705882}},{"Enter first name":{"message":"Similarity score calculated","similarityScore":0.1875}},{"Go":{"message":"Similarity score calculated","similarityScore":0.07142857142857142}}],"averageScore":0.16474089635854341}]}
	*/
	
	@SuppressWarnings("unchecked")
	@POST    
	@Path("/lvl")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findListVsList(String requestJSONString) throws DeserializationException, ParseException {  //return type was Response
		System.out.println("------------------------LVL: Query Started[TimeStamp: "+dateTimeFormat.format(LocalDateTime.now())+"]----------------------");
		System.out.println("Request JSON String:==>"+requestJSONString);

		 JSONObject responseJSONObj = new JSONObject(); //JSON for response body
		 
	     double holdCosSim = 0;
	     //Map <String,String>params = CustomHTTPServerJSON.queryToMap(httpExchange.getRequestURI().getQuery());
	     VectorMeanComparison VMCObj = VectorMeanComparison.getInstance();
	     
	     String jsonStringFromReqBody = requestJSONString;  //IOUtils.toString(httpExchange.getRequestBody());
	     System.out.println("------LVL: Request body json:"+ jsonStringFromReqBody);
	     
	     JSONParser parser = new JSONParser();
	     JSONObject JSONObj = null;
	     try {
			Object jsonObjFromReqBody = parser.parse(jsonStringFromReqBody);
			JSONObj = (JSONObject)jsonObjFromReqBody;
			
	      } catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
	      }
	     
	     JSONArray arrayOfSentencesFromJSON_1 = (JSONArray) JSONObj.get("list1");
	     System.out.println("---list1:==>"+arrayOfSentencesFromJSON_1);
	     String[] DOMTextArray_1 = new String[arrayOfSentencesFromJSON_1.size()];
	     Iterator<String> iterator_1 = arrayOfSentencesFromJSON_1.iterator();
	     int i=0;
	     while (iterator_1.hasNext()) {
	            	DOMTextArray_1[i]=iterator_1.next();
	                System.out.println(DOMTextArray_1[i]);
	                i++;
	            }
	     System.out.println("---DOMTextArray_1:==>"+Arrays.asList(DOMTextArray_1)+"\n");
	    	
	    JSONArray arrayOfSentencesFromJSON_2 = (JSONArray) JSONObj.get("list2");
	    
	    System.out.println("---list2:==>"+arrayOfSentencesFromJSON_2);
	    String[] DOMTextArray_2 = new String[arrayOfSentencesFromJSON_2.size()];
	    Iterator<String> iterator_2 = arrayOfSentencesFromJSON_2.iterator();
	    i=0;
	    while (iterator_2.hasNext()) {
	            DOMTextArray_2[i]=iterator_2.next();
	            System.out.println(DOMTextArray_2[i]);
	            i++;
	            }
    	System.out.println("---DOMTextArray_2:==>"+Arrays.asList(DOMTextArray_2));
	     
    	JSONArray holdListMatchJSONArray = new JSONArray();
     
    	for(i=0; i<DOMTextArray_1.length; i++){
	    	 	JSONObject holdList1JSONObj = new JSONObject();  //stores value of each child of deltasWithSentence Object  
	    	 													//Always create a new object reference with every iteration, otherwise you are updating same instance over and over again and adding reference to same object many times to array.
	    	 	JSONObject holdDeltasJSONObj = new JSONObject();
	    	 	JSONArray holdDeltasJSONArray = new JSONArray();
	    	 	JSONObject holdList2SentenceSimilarityScoreJSONObj = new JSONObject();
	    	 	
	    	 	Map<String, Double> unsortedMap = new HashMap<String, Double>();
	    	 	
	    	 	holdList1JSONObj.put("sentence", DOMTextArray_1[i]);
	    	 	double totalList2Score = 0;
	    	 	String mostSimilarSentence = null;
	    	 	double maxSimilarityScore = 0;
	    	 	
	    	 	for(int j=0; j<DOMTextArray_2.length; j++){
	    	 		//JSONObject holdList2JSONObj = new JSONObject();
	    	 		
	   	        	try {
	   					holdCosSim = VMCObj.Main(DOMTextArray_1[i], DOMTextArray_2[j]);
	   					System.out.println("\n"+DOMTextArray_1[i] +" -vs- "+ DOMTextArray_2[j] +"="+ holdCosSim);
	   					
	   					unsortedMap.put(DOMTextArray_2[j], holdCosSim);
	   					
	   					//holdList2JSONObj.put("similarityScore", holdCosSim);
	   					//holdList2JSONObj.put("message", "Similarity score calculated");
	   					totalList2Score += holdCosSim;
	   					if(holdCosSim > maxSimilarityScore){
	   						maxSimilarityScore = holdCosSim;
	   						mostSimilarSentence = DOMTextArray_2[j];
	   					}
	   					
	   				} catch (Exception e) {
	   					// TODO Auto-generated catch block
	   					//holdList2JSONObj.put("message", VMCObj.exceptionMessage);
	   					
	   					unsortedMap.put(DOMTextArray_2[j], -1.0);
	   					System.out.println(DOMTextArray_1[i] +" -vs- "+ DOMTextArray_2[j]); 
	   					if(VMCObj.exceptionMessage.toString().equals("Indexes shouldn't be empty")){
	   						//holdList2JSONObj.put("similarityScore", new Double("-1"));
	   						}
	   					System.out.println("----Exception occured:"+e.getMessage());
	   				}
	   	        	//holdDeltasJSONObj.put(DOMTextArray_2[j], holdList2JSONObj);
	   	        	//holdDeltasJSONArray.add(holdDeltasJSONObj);
	   	        	
	   	        	
     	}  // j loop closing

	        System.out.println("************");
	    	Map<String, Double> sortedMapDsc = sortByComparator(unsortedMap, DESC);
	        
	    	printMap(sortedMapDsc);
	        
	        
	        for (Entry<String, Double> entry : sortedMapDsc.entrySet())
	        {	
	        	JSONObject msgAndSimilarityScoreObject = new JSONObject();  // {   "Enter first name": { "message": "Similarity score calculated", "similarityScore": 0.5107064247131348 }}
	        	JSONObject enclosedBySentenceObject = new JSONObject();
	        	if(entry.getValue() < 0){
	        		msgAndSimilarityScoreObject.put("message", "Indexes shouldn't be empty");
	        	}
	        	else 
	        		msgAndSimilarityScoreObject.put("message", "Similarity score calculated");
	        	
	        	msgAndSimilarityScoreObject.put("similarityScore", entry.getValue());
	        	
	        	enclosedBySentenceObject.put(entry.getKey(), msgAndSimilarityScoreObject);
	        	holdDeltasJSONArray.add(enclosedBySentenceObject); 
	            //System.out.println("-----Key : " + entry.getKey() + " Value : "+ entry.getValue());
	        }
	        
	    	holdList1JSONObj.put("maxSimilarityScore", maxSimilarityScore);
	    	holdList1JSONObj.put("mostSimilarSentence", mostSimilarSentence);
	    	holdList1JSONObj.put("deltasWithSentence", holdDeltasJSONArray);
	    	holdList1JSONObj.put("sentence", DOMTextArray_1[i]);
	    	holdList1JSONObj.put("averageScore", totalList2Score/DOMTextArray_2.length);
	    	
	    	holdListMatchJSONArray.add(holdList1JSONObj);
	        }	// i loop closing
     
    		responseJSONObj.put("listMatch",holdListMatchJSONArray);

	     //CustomHTTPServerJSON.writeResponse(httpExchange, resposeJSONObj.toString());
	     //System.out.println("------------------------Query Finished----------------------");
	   

		System.out.println(":===> STRING FORMAT of responseJSONObject: "+responseJSONObj.toJSONString());
		System.out.println("------------------------LVL: Query Finished[TimeStamp: "+dateTimeFormat.format(LocalDateTime.now())+"]----------------------");
			
		return Response.ok(responseJSONObj.toJSONString()).build();

		//return Response.status(Response.Status.BAD_REQUEST).build();	//OSAO: Changed Response.Status
	}

	/*
	 *POST
	 *URL: http://blr00avc.in.oracle.com:8080/MicroServices_DL4J_TF/get/nearestWords
	 *Request JSON: {"word": "invalid", "numberOfWords": "100"}
	 *Response JSON: {"numberOfWords": 100, "nearestWords": ["abc", "xyz", ...]}
	*/
	
	@SuppressWarnings("unchecked")
	@POST    
	@Path("/nearestWords")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findNearestWords(String requestJSONString) throws DeserializationException, ParseException {  //return type was Response
		System.out.println("------------------------nearestWords: Query Started[TimeStamp: "+dateTimeFormat.format(LocalDateTime.now())+"]----------------------");
		System.out.println("Request JSON String:==>"+requestJSONString);
	
		JSONObject responseJSONObj = new JSONObject();
	    String word;
	    int numberOfWords;
	      
	      //Map <String,String>params = CustomHTTPServerJSON.queryToMap(httpExchange.getRequestURI().getQuery());
	      String jsonStringFromReqBody = requestJSONString; //IOUtils.toString(httpExchange.getRequestBody());
	      System.out.println("------nearestWord: Request body json:"+ jsonStringFromReqBody);
	      JSONParser parser = new JSONParser();
	      JSONObject reqJSONObj = null;
	      try {
			Object jsonObjFromReqBody = parser.parse(jsonStringFromReqBody);
			reqJSONObj = (JSONObject)jsonObjFromReqBody;
			
	      } catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
	      }
	      
	    	word = (String) reqJSONObj.get("word");
	    	numberOfWords = Integer.parseInt((String) reqJSONObj.get("numberOfWords"));

	      System.out.println("word:==>"+word+", numberOfWords:==>"+numberOfWords);
	      
	      VectorMeanComparison VMCObj = VectorMeanComparison.getInstance();
	      
	      //response.append("sentence1:==> "+params.get("sentence1"));
	      responseJSONObj.put("word", word);
	      //response.append("\nsentence2:==> "+params.get("sentence2"));
	      responseJSONObj.put("numberOfWords", numberOfWords);
	      try {
			//response.append("\nSimilarity:==> "+ Double.toString(obj.Main(params.get("sentence1"), params.get("sentence2"))));
			responseJSONObj.put("nearestWords", VMCObj.nearestWords(word, numberOfWords));
			responseJSONObj.put("message", "Nearest words fetched");
	      } catch (Exception e) {
			// TODO Auto-generated catch block
			responseJSONObj.put("message", VMCObj.exceptionMessage);
			if(VMCObj.exceptionMessage.toString().equals("Indexes shouldn't be empty")){
				responseJSONObj.put("nearestWords", new Double("-1"));
				}
			e.printStackTrace();
	      	}
		
		
		System.out.println(":===> STRING FORMAT of responseJSONObject: "+responseJSONObj.toJSONString());
		System.out.println("------------------------nearestWords: Query Finished[TimeStamp: "+dateTimeFormat.format(LocalDateTime.now())+"]----------------------");
			
		return Response.ok(responseJSONObj.toJSONString()).build();

	//return Response.status(Response.Status.BAD_REQUEST).build();	//OSAO: Changed Response.Status
	}
	
	
	/*
	 *POST
	 *URL: http://blr00avc.in.oracle.com:8080/MicroServices_DL4J_TF/get/iconSimilarityTF
	 *Request JSON: {"iconInStringFormat": "asdfasdfa;lskjfasdfjklasdfjadsf"}
	 *Response JSON: {"mostSimilarIcon": "add", "top5SimilarityString": "", "averageScore": 0.2, "mostSimilarIcon_1": { "name": "add", "similarityScore": 0.9976853 },
	*/
	
	@SuppressWarnings("unchecked")
	@POST    
	@Path("/iconSimilarityTF")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findIconTFHandler(String requestJSONString) throws DeserializationException, ParseException, IOException {  //return type was Response
		System.out.println("------------------------iconSimilarityTF: Query Started[TimeStamp: "+dateTimeFormat.format(LocalDateTime.now())+"]----------------------");
		System.out.println("Request JSON String:==>"+requestJSONString);
		
		JSONObject responseJSONObj = new JSONObject();
	    String iconInStringFormat;
	      
	      //Map <String,String>params = CustomHTTPServerJSON.queryToMap(httpExchange.getRequestURI().getQuery());
	      String jsonStringFromReqBody = requestJSONString; //IOUtils.toString(httpExchange.getRequestBody());
	      //System.out.println("------iconTFSimilarity: Request body json:"+ jsonStringFromReqBody);
	      
	      JSONParser parser = new JSONParser();
	      JSONObject reqJSONObj = null;
	      
	      try {
			Object jsonObjFromReqBody = parser.parse(jsonStringFromReqBody);
			reqJSONObj = (JSONObject)jsonObjFromReqBody;
			
	      } catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
	      }
	      
	      iconInStringFormat = (String) reqJSONObj.get("iconInStringFormat"); // reqJSONObj format is: {"iconInStringFormat": "<iconInStringFormat.......................>"}
	      
	      byte[] base64Decoded = DatatypeConverter.parseBase64Binary(iconInStringFormat);
		  System.out.println("base64Decoded:==>"+base64Decoded.toString());
		    
		  BufferedImage recreatedImage = byteArrayToImage(base64Decoded); 
		  File file = new File("./tempHold.png");  //EclipseWorkspace_DL4J/dl4j-examples-master/dl4j-examples/tempHold.png
		  ImageIO.write(recreatedImage, "png", file);
	      
	      responseJSONObj.put("iconInStringFormat", iconInStringFormat);  // put "iconInStringFormat" in responseJSONObj
	      
	      StringBuffer sb = new StringBuffer();
	      //sb = null;
	      String s = null;
	      //Process p = Runtime.getRuntime().exec("python /scratch/thlai/OSAO/ImageClassifier/tensorflow-master/tensorflow/examples/label_image/label_image.py --image=./tempHold.png");
	      Process p = Runtime.getRuntime().exec("python /scratch/thlai/OSAO/ImageClassifier/hub-master/examples/label_image/label_image.py --image=./tempHold.png");
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			//read the output from the command
			System.out.println("Here is the standard output of command:");
			while((s = stdInput.readLine()) != null){
				//System.out.println(s);
				sb.append(s+',');
			}
		  System.out.println(sb);
		  //Value of sb is: "home 0.99702555,profile 0.0006951755,cancel 0.00046152622,calendar 0.00044027422,add 0.00039851325,"
	      String top5Similarity = sb.substring(0,sb.length()-1);
		  responseJSONObj.put("top5SimilarityString", top5Similarity);   // last character is ','
	      String iconNameAndScoreArray[] = top5Similarity.split(",");
	      double totalSimilarityScore = 0;
	      //Already the scores are sorted.
	      for(int i=0;i<5;i++){
	    	  JSONObject holdJSONObj = new JSONObject(); 
	    	  holdJSONObj.put("name", iconNameAndScoreArray[i].substring(0, iconNameAndScoreArray[i].lastIndexOf(" ")));  // iconNameAndScoreArray[i]: home 0.99702555
	    	  holdJSONObj.put("similarityScore", Double.parseDouble(iconNameAndScoreArray[i].substring(iconNameAndScoreArray[i].lastIndexOf(" ")+1)));
	    	  
	    	  totalSimilarityScore += Double.parseDouble(iconNameAndScoreArray[i].substring(iconNameAndScoreArray[i].lastIndexOf(" ")));
	    	  
	    	  responseJSONObj.put("mostSimilarIcon_"+(i+1), holdJSONObj);
	      }
	      responseJSONObj.put("maxScore", Double.parseDouble(iconNameAndScoreArray[0].substring(iconNameAndScoreArray[0].lastIndexOf(" "))));
	      responseJSONObj.put("mostSimilarIcon", iconNameAndScoreArray[0].substring(0, iconNameAndScoreArray[0].lastIndexOf(" ")));
	      responseJSONObj.put("averageScore", totalSimilarityScore/5);
		
		System.out.println(":===> STRING FORMAT of responseJSONObject: "+responseJSONObj.toJSONString());
		System.out.println("------------------------iconSimilarityTF: Query Finished[TimeStamp: "+dateTimeFormat.format(LocalDateTime.now())+"]----------------------");
			
		return Response.ok(responseJSONObj.toJSONString()).build();

	//return Response.status(Response.Status.BAD_REQUEST).build();	//OSAO: Changed Response.Status
	}
	
	
	/*
	 *POST
	 *URL: http://blr00avc.in.oracle.com:8080/MicroServices_DL4J_TF/get/iconSimilarityVGG16
	 *Request JSON: {"iconInStringFormat": "asdfasdfa;lskjfasdfjklasdfjadsf"}
	 *Response JSON: {"mostSimilarIcon": "add", "top5SimilarityString": "", "averageScore": 0.2, "mostSimilarIcon_1": { "name": "add", "similarityScore": 0.9976853 },
	*/
	
	@SuppressWarnings("unchecked")
	@POST    
	@Path("/iconSimilarityVGG16")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findIconVGG16UptrainedHandler(String requestJSONString) throws DeserializationException, ParseException, IOException {  //return type was Response
		System.out.println("------------------------iconSimilarityVGG16: Query Started[TimeStamp: "+dateTimeFormat.format(LocalDateTime.now())+"]----------------------");
		System.out.println("Request JSON String:==>"+requestJSONString);
		
		JSONObject responseJSONObj = new JSONObject();
	      String iconInStringFormat;
	      
	      //Map <String,String>params = CustomHTTPServerJSON.queryToMap(httpExchange.getRequestURI().getQuery());
	      String jsonStringFromReqBody = requestJSONString; //IOUtils.toString(httpExchange.getRequestBody());
	      System.out.println("------iconSimilarityVGG16: Request body json:"+ jsonStringFromReqBody);
	      
	      JSONParser parser = new JSONParser();
	      JSONObject reqJSONObj = null;
	      
	      try {
			Object jsonObjFromReqBody = parser.parse(jsonStringFromReqBody);
			reqJSONObj = (JSONObject)jsonObjFromReqBody;
			
	      } catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
	      }
	      
	      

	      iconInStringFormat = (String) reqJSONObj.get("iconInStringFormat"); // reqJSONObj format is: {"iconInStringFormat": "<iconInStringFormat.......................>"}
	      
	      //QueryForIconSimilarity QFISObj = QueryForIconSimilarity.getInstance();
	      byte[] base64Decoded = DatatypeConverter.parseBase64Binary(iconInStringFormat);
		  System.out.println("base64Decoded:==>"+base64Decoded.toString());
		    
		  BufferedImage recreatedImage = byteArrayToImage(base64Decoded); 
		  File file = new File("/scratch/thlai/OSAO/ImageClassifier/VGG16_pretrainedWorkSpace/tempHoldForVGG16UptrainedModel.png");
		  ImageIO.write(recreatedImage, "png", file);
	      
	      responseJSONObj.put("iconInStringFormat", iconInStringFormat);  // put "iconInStringFormat" in responseJSONObj
	      
	      StringBuffer sb = new StringBuffer();
	      String s = null;
	      //Process p = Runtime.getRuntime().exec("python /scratch/thlai/OSAO/ImageClassifier/tensorflow-master/tensorflow/examples/label_image/label_image.py --image=./tempHold.png");
	      //Process p = Runtime.getRuntime().exec("python /scratch/thlai/OSAO/ImageClassifier/hub-master/examples/label_image/label_image.py --image=./tempHold.png");
	      Process p = Runtime.getRuntime().exec("python /scratch/thlai/OSAO/ImageClassifier/VGG16_pretrainedWorkSpace/Single_Query_VGG16_Uptrained_model.py");
	      try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      //System.out.println("----------->"+p1.toString());
	      BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));   //p.getErrorStream()
			//read the output from the command
			System.out.println("Here is the standard output of command:");
			while((s = stdInput.readLine()) != null){
				//System.out.println("--->"+s);
				sb.append(s+',');
			}
		  System.out.println(sb);
		  //Value of sb is: "home 0.99702555,profile 0.0006951755,cancel 0.00046152622,calendar 0.00044027422,add 0.00039851325,"
	      String top5Similarity = sb.substring(0,sb.length()-1);
		  responseJSONObj.put("top5SimilarityString", top5Similarity);   // last character is ','
	      String iconNameAndScoreArray[] = top5Similarity.split(",");
	      double totalSimilarityScore = 0;
	      //Already the scores are sorted.
	      for(int i=0;i<5;i++){
	    	  JSONObject holdJSONObj = new JSONObject(); 
	    	  holdJSONObj.put("name", iconNameAndScoreArray[i].substring(0, iconNameAndScoreArray[i].lastIndexOf(" ")));  // iconNameAndScoreArray[i]: home 0.99702555
	    	  holdJSONObj.put("similarityScore", Double.parseDouble(iconNameAndScoreArray[i].substring(iconNameAndScoreArray[i].lastIndexOf(" ")+1)));
	    	  
	    	  totalSimilarityScore += Double.parseDouble(iconNameAndScoreArray[i].substring(iconNameAndScoreArray[i].lastIndexOf(" ")));
	    	  
	    	  responseJSONObj.put("mostSimilarIcon_"+(i+1), holdJSONObj);
	      }
	      responseJSONObj.put("maxScore", Double.parseDouble(iconNameAndScoreArray[0].substring(iconNameAndScoreArray[0].lastIndexOf(" "))));
	      responseJSONObj.put("mostSimilarIcon", iconNameAndScoreArray[0].substring(0, iconNameAndScoreArray[0].lastIndexOf(" ")));
	      responseJSONObj.put("averageScore", totalSimilarityScore/5);
	      
		
		System.out.println(":===> STRING FORMAT of responseJSONObject: "+responseJSONObj.toJSONString());
		System.out.println("------------------------iconSimilarityVGG16: Query Finished[TimeStamp: "+dateTimeFormat.format(LocalDateTime.now())+"]----------------------");
			
		return Response.ok(responseJSONObj.toJSONString()).build();

	//return Response.status(Response.Status.BAD_REQUEST).build();	//OSAO: Changed Response.Status
	}

	
	
	  /**
	   * returns the url parameters in a map
	   * @param query
	   * @return map
	   */
	  public static Map<String, String> queryToMap(String query){
	    Map<String, String> result = new HashMap<String, String>();
	    for (String param : query.split("&")) {
	        String pair[] = param.split("=");
	        if (pair.length>1) {
	            result.put(pair[0], pair[1]);
	        }else{
	            result.put(pair[0], "");
	        }
	    }
	    return result;
	  }
	  
	  // Method for getting the maximum value
	  public static double getMax(double[] inputArray){ 
	    double maxValue = inputArray[0]; 
	    for(int i=1;i < inputArray.length;i++){ 
	      if(inputArray[i] > maxValue){ 
	         maxValue = inputArray[i]; 
	      } 
	    } 
	    return maxValue; 
	  }
	  
	//Method for getting the maximum value's index
	 public static int getMaxIndex(double[] inputArray){ 
	   int maxValueIndex = 0;
	   double maxValue = inputArray[0];
	   for(int i=1;i < inputArray.length;i++){ 
	     if(inputArray[i] > maxValue){ 
	        maxValue = inputArray[i]; 
	        maxValueIndex = i;
	     } 
	   } 
	   return maxValueIndex; 
	 }
	 
	//Method for getting the average of all values
	public static double getAverageScore(double[] inputArray){ 
	  //int maxValueIndex = 0;
	  double maxValue = inputArray[0];
	  double sumTotal = 0;
	  for(int i=0;i < inputArray.length;i++){ 
	    sumTotal = sumTotal + inputArray[i];
	  } 
	  double averageScore = sumTotal/inputArray.length;
	  return averageScore; 
	}
	 
	  // Method for getting the minimum value
	  public static double getMin(int[] inputArray){ 
	    double minValue = inputArray[0]; 
	    for(int i=1;i<inputArray.length;i++){ 
	      if(inputArray[i] < minValue){ 
	        minValue = inputArray[i]; 
	      } 
	    } 
	    return minValue; 
	  }
	
	
	private String findSimilarity(String sentence1, String sentence2) throws Exception {
		StringBuilder response = new StringBuilder();
		response.append("sentence1:==> " + sentence1);
		response.append("\nsentence2:==> " + sentence2);
		VectorMeanComparison VMC_Obj = VectorMeanComparison.getInstance();
		response.append("\nSimilarity:==> " + 
				Double.toString(VMC_Obj.Main(sentence1, sentence2)));
		
		return response.toString();
	}
	
	
	public static Map<String, Double> sortByComparator(Map<String, Double> unsortMap, final boolean order)
	  {

	      List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());

	      // Sorting the list based on values
	      Collections.sort(list, new Comparator<Entry<String, Double>>()
	      {
	          public int compare(Entry<String, Double> o1,
	                  Entry<String, Double> o2)
	          {
	              if (order)
	              {
	                  return o1.getValue().compareTo(o2.getValue());
	              }
	              else
	              {
	                  return o2.getValue().compareTo(o1.getValue());

	              }
	          }
	      });

	      // Maintaining insertion order with the help of LinkedList
	      Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
	      for (Entry<String, Double> entry : list)
	      {
	          sortedMap.put(entry.getKey(), entry.getValue());
	      }

	      return sortedMap;
	  }

	  public static void printMap(Map<String, Double> map)
	  {	  System.out.println("Sorted based on Similarity Score");
	      for (Entry<String, Double> entry : map.entrySet())
	      {
	          System.out.println("Key : " + entry.getKey() + ", Value : "+ entry.getValue());
	      }
	  }
	  
	  public static BufferedImage  byteArrayToImage(byte[] bytes){  
	      BufferedImage bufferedImage=null;
	      try {
	          InputStream inputStream = new ByteArrayInputStream(bytes);
	          bufferedImage = ImageIO.read(inputStream);
	      } catch (IOException ex) {
	          System.out.println(ex.getMessage());
	      }
	      return bufferedImage;
	  }
	
	/*private String findSimilarity(String sentence1, List<String> sentences) {
		StringBuilder response = new StringBuilder();
		double holdCosSim = 0;
		double similarityScores[] = new double[sentences.size()];
		VectorMeanComparison VMC_Obj = VectorMeanComparison.getInstance();
		response.append("sentence:==> " + sentence1);
		response.append("\nlist:==> " + sentences);
		int i = 0;
		for (Iterator<String> it = sentences.iterator(); it.hasNext();) {
			String sentence = it.next();
			try {
				holdCosSim = VMC_Obj.Main(sentence1, sentence);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			response.append("\n" + sentence1 +" -vs- " + sentence + "=" + holdCosSim);
        	similarityScores[i++] = holdCosSim;
		}
		
		response.append("\n\nMost Similar:==>"+getMax(similarityScores));
		
		return response.toString();
	}*/

}
