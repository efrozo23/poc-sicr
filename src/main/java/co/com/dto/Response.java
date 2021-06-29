package co.com.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class Response {
	
	@JsonProperty(value = "Body")
	public JsonNode header = JsonNodeFactory.instance.objectNode();

}
