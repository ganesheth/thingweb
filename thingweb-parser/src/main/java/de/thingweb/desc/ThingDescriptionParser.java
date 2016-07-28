/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Siemens AG and the thingweb community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.thingweb.desc;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.siemens.ct.exi.EXIFactory;
import com.siemens.ct.exi.exceptions.EXIException;
import com.siemens.ct.exi.helpers.DefaultEXIFactory;
import com.siemens.ct.exi.json.EXIforJSONParser;

import de.thingweb.thing.Action;
import de.thingweb.thing.Event;
import de.thingweb.thing.Metadata;
import de.thingweb.thing.Property;
import de.thingweb.thing.Thing;
import de.thingweb.thing.ThingMetadata;

public class ThingDescriptionParser
{

  private static final String WOT_TD_CONTEXT = "http://w3c.github.io/wot/w3c-wot-td-context.jsonld";

//  private static final JsonNode TD_SCHEMA;
//  
//  static {
//    File f = new File("schema", "td-schema.json");
//    try
//    {
//      TD_SCHEMA = JsonLoader.fromFile(f);
//    }
//    catch (IOException e)
//    {
//      throw new RuntimeException("Expected location for TD JSON schema: schema/td-schema.json", e);
//    }
//  }

  public static Thing fromJavaMap(Object json) throws IOException
  {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readValue(json.toString(), JsonNode.class);
    
    try {
      return parse(root);
    } catch (Exception e) {
      return null;
    }
  }

  public static Thing fromURL(URL url) throws JsonParseException, IOException
  {

    InputStream is = new BufferedInputStream(url.openStream());
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int b;
    while ((b = is.read()) != -1)
    {
      baos.write(b);
    }
    return fromBytes(baos.toByteArray());
  }
  
	static List<String> SHARED_STRINGS_EXI_FOR_JSON = Arrays.asList(new String[] { "@context", "@id", "@type", "@value", "Brightness",
			"Car", "CoAP", "DecreaseColor", "Distance", "Door", "EXI", "EXI4JSON", "Fan", "HTTP", "IncreaseColor",
			"JSON", "Lamp", "Lighting", "Off", "On", "OnOff", "PowerConsumption", "RGBColor", "RGBColorBlue",
			"RGBColorGreen", "RGBColorRed", "Speed", "Start", "Stop", "Switch", "Temperature", "Thing", "Toggle",
			"TrafficLight", "WS", "actions", "associations", "celsius", "dogont", "encodings", "events", "hrefs",
			"http://w3c.github.io/wot/w3c-wot-td-context.jsonld",
			"https://w3c.github.io/wot/w3c-wot-common-context.jsonld", "inch", "inputData", "interactions", "joule",
			"kelvin", "kmh", "kwh", "lgdo", "m", "max", "mile", "min", "mm", "mph", "name", "outputData",
			"properties", "protocols", "qu", "reference", "schema", "security", "unit", "uris", "valueType",
			"writable", "xsd:boolean", "xsd:byte", "xsd:float", "xsd:int", "xsd:short", "xsd:string",
			"xsd:unsignedByte", "xsd:unsignedInt", "xsd:unsignedShort" });

	public static Thing fromBytes(byte[] data) throws JsonParseException, IOException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			EXIFactory ef = DefaultEXIFactory.newInstance();
			ef.setSharedStrings(SHARED_STRINGS_EXI_FOR_JSON);

			EXIforJSONParser e4j = new EXIforJSONParser(ef);
			e4j.parse(new ByteArrayInputStream(data), baos);

			// push-back EXI-generated JSON
			data = baos.toByteArray();
		} catch (EXIException | IOException e) {
			// something went wrong with EXI --> use "plain-text" json
		}

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readValue(new InputStreamReader(new ByteArrayInputStream(data), "UTF-8"),
				JsonNode.class);

		try {
			return parse(root);
		} catch (Exception e) {
			// try old parser if by chance it was an old TD
			//return parseOld(root);
			return null;
		}
	}

  public static Thing fromFile(String fname) throws FileNotFoundException, IOException
  {
    Path path = Paths.get(fname);
    byte[] data = Files.readAllBytes(path);
    return fromBytes(data);
  }
  
  public static byte[] toBytes(Thing thing) throws IOException
  {
    ObjectNode td = toJsonObject(thing);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectMapper mapper = new ObjectMapper();

    //TODO catch the IOException here and throw a runtimeexception
    // as this is no case where a developer could react
    mapper.writeValue(baos, td);

    return baos.toByteArray();
  }

  // TODO set as private (and check it is not called elsewhere)
  public static ObjectNode toJsonObject(Thing thing) throws JsonProcessingException, IOException {
    JsonNodeFactory factory = new JsonNodeFactory(false);
    ObjectMapper mapper = new ObjectMapper();
    //ArrayNode contexts = factory.arrayNode();
    
    ObjectNode thingNode = factory.objectNode();
    ArrayNode contextsNode = thingNode.putArray("@context");
    //td.put("@context", WOT_TD_CONTEXT);
    
    List<String> additionalContexts = thing.getMetadata().getAll(ThingMetadata.METADATA_ELEMENT_CONTEXT);

    //if(!additionalContexts.contains(WOT_TD_CONTEXT))
    //	contextsNode.add(new TextNode(WOT_TD_CONTEXT));
    for(String context : additionalContexts){
    	try{    		
    		JsonNode contextNode = mapper.readTree(context);
    		contextsNode.add(contextNode);
    	}catch(Exception e){}
    }
    thingNode.put("name", thing.getName());
    if(thing.getMetadata().getAssociations().size() > 0)
    	thingNode.putPOJO("associations", thing.getMetadata().getAssociations());
    
    Metadata metadata = thing.getMetadata();
    Map<String, List<String>> metadataItems = metadata.getItems();
    
    for(String key : metadataItems.keySet()){
    	if(key == ThingMetadata.METADATA_ELEMENT_CONTEXT)
    		continue;
    	
    	if(thing.getMetadata().getAll(key).size() > 1){
	    	ArrayNode metadataNode = thingNode.putArray(key);
	        for (String e : thing.getMetadata().getAll(key)) {
	    		//JsonNode m = mapper.readTree(e);
	    		metadataNode.add(e);
	        }
    	} else if (thing.getMetadata().getAll(key).size() == 1){
    		thingNode.put(key, thing.getMetadata().getAll(key).get(0));
    	}
    }

    ArrayNode propertiesNode = thingNode.putArray("properties");
    for (Property prop : thing.getProperties()) {
      ObjectNode propertyNode = propertiesNode.objectNode();// factory.objectNode();
      propertyNode.put("name", prop.getName());
      propertyNode.put("writable", prop.isWritable());
      if(prop.isObservable())
    	  propertyNode.put("observable", prop.isObservable());
      JsonNode valueTypeNode = mapper.readTree( prop.getValueType());
      propertyNode.set("valueType", valueTypeNode);

      if (prop.getHrefs().size() > 1) {
        ArrayNode hrefs = propertyNode.putArray("hrefs");
        for (String href : prop.getHrefs()) {
          hrefs.add(href);
        }
      } else if (prop.getHrefs().size() == 1) {
        propertyNode.put("hrefs", prop.getHrefs().get(0));
      }
      if(prop.getMetadata().getAssociations().size() > 0)
    	  propertyNode.putPOJO("associations", prop.getMetadata().getAssociations());
      
      Metadata propertyMetadata = prop.getMetadata();
      Map<String, List<String>> propMetaItems = propertyMetadata.getItems();
      
      for(String key : propMetaItems.keySet()){
    	  if(prop.getMetadata().getAll(key).size() > 1){
    		  ArrayNode metadataElements = propertyNode.putArray(key);
	          for (String e : prop.getMetadata().getAll(key)) {
	          	metadataElements.add(e);
	          }
    	  }else{
    		  propertyNode.put(key, prop.getMetadata().get(key));
    	  }    		  
      }      
      propertiesNode.add(propertyNode);
    }

    ArrayNode actionsNode = thingNode.putArray("actions");
    for (Action action : thing.getActions()) {
      ObjectNode actionNode = actionsNode.objectNode();
      actionNode.put("name", action.getName());

      if (!action.getInputType().isEmpty()) {
      	JsonNode inputTypeNode = mapper.readTree(action.getInputType());
        actionNode.set("inputData", inputTypeNode);
      }

      if (!action.getOutputType().isEmpty()) {
    	JsonNode outputTypeNode = mapper.readTree(action.getOutputType());
        actionNode.set("outputData", outputTypeNode);
      }

      if (action.getHrefs().size() > 1) {
        ArrayNode hrefs = actionNode.putArray("hrefs");
        for (String href : action.getHrefs()) {
          hrefs.add(href);
        }
      } else if (action.getHrefs().size() == 1) {
        actionNode.put("hrefs", action.getHrefs().get(0));
      }
      if(action.getMetadata().getAssociations().size() > 0)
    	  actionNode.putPOJO("associations", action.getMetadata().getAssociations());
  
      Metadata actionMetadata = action.getMetadata();
      Map<String, List<String>> actionMetaItems = actionMetadata.getItems();
      for(String key : actionMetaItems.keySet()){
    	  if(action.getMetadata().getAll(key).size() > 1){
    		  ArrayNode metadataElements = actionNode.putArray(key);
	          for (String e : action.getMetadata().getAll(key)) {
	          	metadataElements.add(e);
	          }
    	  }else{
    		  actionNode.put(key, action.getMetadata().get(key));
    	  }    		  
      }      
      actionsNode.add(actionNode);
    }

    ArrayNode events = factory.arrayNode();
    for (Event event : thing.getEvents()) {
      ObjectNode eventNode = factory.objectNode();
      eventNode.put("name", event.getName());

      if (!event.getInputType().isEmpty()) {
        	JsonNode inputTypeNode = mapper.readTree(event.getInputType());
            eventNode.set("inputData", inputTypeNode);
        }
      
      if (!event.getOutputType().isEmpty()) {
        	JsonNode inputTypeNode = mapper.readTree(event.getInputType());
        }      

      if (event.getHrefs().size() > 1) {
        ArrayNode hrefs = factory.arrayNode();
        for (String href : event.getHrefs()) {
          hrefs.add(href);
        }
        eventNode.put("hrefs", hrefs);
      } else if (event.getHrefs().size() == 1) {
        eventNode.put("hrefs", factory.textNode(event.getHrefs().get(0)));
      }

      events.add(eventNode);
    }
    thingNode.put("events", events);

    return thingNode;
  }

  /**
   * reshapes the input JSON-LD object using the standard Thing Description context and having the
   * thing description resource as object root.
   * 
   * @param data
   *          UTF-8 encoded JSON-LD object
   * @return the reshaped JSON-LD object
   * @throws IOException
   *           error
   */
  public static String reshape(byte[] data) throws IOException
  {
    ObjectMapper om = new ObjectMapper();

    try
    {
      Object jsonld = JsonUtils.fromInputStream(new ByteArrayInputStream(data));
      // TODO put the frame online instead
      Object frame = om.readValue("{\"http://www.w3c.org/wot/td#hasMetadata\":{}}", HashMap.class);

      jsonld = JsonLdProcessor.frame(jsonld, frame, new JsonLdOptions());
      return null;
//      return removeBlankNodes(compactJson(jsonld)).toString();
    }
    catch (JsonLdError e)
    {
      throw new IOException("Can't reshape triples", e);
    }
  }
  
 
  
  private static Thing parse(JsonNode td) throws Exception {
//    ProcessingReport report = JsonSchemaFactory.byDefault().getValidator().validate(TD_SCHEMA, td);
//    if (!report.isSuccess()) {
//      throw new IOException("JSON data not valid");
//    }
    
    Thing thing = new Thing(td.get("name").asText());
    
    Iterator<String> tdIterator = td.fieldNames();
    while (tdIterator.hasNext()) {
      String thingField = tdIterator.next();
      switch (thingField) {
        case "uris":
          for (String uri : stringOrArray(td.get("uris"))) {
            thing.getMetadata().add("uris", uri);
          }
          break;
          
        case "properties":
          for (JsonNode propertyNode : td.get("properties")) {
            Property.Builder builder = Property.getBuilder(propertyNode.get("name").asText());
            Iterator<String> it = propertyNode.fieldNames();
            HashMap<String, JsonNode> metadataNodes = new HashMap<>();
            while (it.hasNext()) {
            	String s = it.next();
              switch (s) {
                case "valueType":
                  builder.setXsdType(propertyNode.get("valueType").toString());
                  break;
                case "writable":
                  builder.setWriteable(propertyNode.get("writable").asBoolean());
                  break;
                case "observable":
                    builder.setObservable(propertyNode.get("observable").asBoolean());
                    break;                  
                case "hrefs":
                  builder.setHrefs(stringOrArray(propertyNode.get("hrefs")));
                  break;                  
                default:
                	metadataNodes.put(s,propertyNode.get(s));
              }
            }
            Property property = builder.build();
            for(String key : metadataNodes.keySet()){
            	if(!(metadataNodes.get(key).isArray() || metadataNodes.get(key).isTextual())){
            		property.getMetadata().add(key, metadataNodes.get(key).toString());
            	}else{            	
	            	List<String> stringOrArray = stringOrArray( metadataNodes.get(key));
	        		for(String m : stringOrArray)
	        			property.getMetadata().add(key, m);
            	}
            }
            thing.addProperty(property);
          }
          break;
          
        case "actions":
          for (JsonNode action : td.get("actions")) {
            Action.Builder builder = Action.getBuilder(action.get("name").asText());
            HashMap<String, JsonNode> metadataNodes = new HashMap<>();
            Iterator<String> it = action.fieldNames();
            while (it.hasNext()) {
              String s = it.next();
              switch (s) {
                case "inputData":
                  builder.setInputType(action.get("inputData").toString());
                  //if(action.get("inputData").has("defaults"))
                //	  builder.setInputDefaults(action.get("inputData").get("defaults").toString());
                  break;
                case "outputData":
                  builder.setOutputType(action.get("outputData").toString());
                  break;
                case "hrefs":
                  builder.setHrefs(stringOrArray(action.get("hrefs")));
                  break;
                default:
                	metadataNodes.put(s, action.get(s));                  
              }
            }
            Action a = builder.build();
            for(String key : metadataNodes.keySet()){
            	List<String> stringOrArray = stringOrArray( metadataNodes.get(key));
        		for(String m : stringOrArray)
        			a.getMetadata().add(key, m);
            }
            thing.addAction(a);
          }
          break;
          
        case "events":
            for (JsonNode event : td.get("events")) {
                Event.Builder builder = Event.getBuilder(event.get("name").asText());
                HashMap<String, JsonNode> metadataNodes = new HashMap<>();
                Iterator<String> it = event.fieldNames();
                while (it.hasNext()) {
                  String s = it.next();
                  switch (s) {
                  case "inputData":
                      builder.setInputType(event.get("inputData").toString());
                      //if(event.get("inputData").has("defaults"))
                    	//  builder.setInputDefaults(event.get("inputData").get("defaults").toString());
                      break;
                    case "outputData":
                      builder.setOutputType(event.get("outputData").toString());
                      break;
                    case "hrefs":
                      builder.setHrefs(stringOrArray(event.get("hrefs")));
                      break;
                    default:
                    	metadataNodes.put(s, event.get(s));                      
                  }
                }
                Event e = builder.buildEvent();
                for(String key : metadataNodes.keySet()){
                	List<String> stringOrArray = stringOrArray( metadataNodes.get(key));
            		for(String m : stringOrArray)
            			e.getMetadata().add(key, m);
                }                
                thing.addEvent(e);
              }
              break;
        
        case "@context":
            for (JsonNode context : td.get("@context")) {
            		thing.getMetadata().add("@context", context.toString());
            }
            break;          

        default:
        	if(thingField == "name")
        		continue;
            for (String meta : stringOrArray(td.get(thingField))) {
                thing.getMetadata().add(thingField, meta);
              }
            break;  
      }
    }
    
    return thing;
  }
  
  private static List<String> stringOrArray(JsonNode node) {
    List<String> array = new ArrayList<String>();
    
    if (node.isTextual()) {
      array.add(node.asText());
    } else if (node.isArray()) {
      for (JsonNode subnode : node) {
        array.add(subnode.asText());
      }
    }
    
    return array;
  }

  public static void main(String[] args) throws FileNotFoundException, IOException
  {
    Thing thing = fromFile("jsonld" + File.separator + "led.v2.jsonld");
    System.out.println(new String(toBytes(thing)));
  }

}
