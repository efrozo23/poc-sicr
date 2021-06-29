package co.com.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import co.com.util.Constants;

@Component
public class RouteService extends RouteBuilder{
	
	
	
	@Autowired
    private Environment env;

	@Override
	public void configure() throws Exception {
		// TODO Auto-generated method stub
		
		 onException(Exception.class)
		 	.handled(true)
		 	.setBody(constant("ERROR"))
		 	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
		 	.end();
		 
		 
	     restConfiguration()
	         .component("servlet")
	         .bindingMode(RestBindingMode.json)
	         .dataFormatProperty("prettyPrint", "true")
	         .enableCORS(true)
	         .port(env.getProperty("server.port", "8080"));
	         
     
	     rest().description(env.getProperty("api.description"))
	         .consumes(MediaType.APPLICATION_JSON_VALUE)
	         .produces(MediaType.APPLICATION_JSON_VALUE)
	         
	       .get(env.getProperty("endpoint.healtcheck")).outType(String.class)
	        	.route().routeId("HEALTCHECK").setBody(constant("OK")).endRest()
	       .get(env.getProperty("endpoint.ani")).outType(String.class)
	       		.param().name("nuip")
	       			.required(true)
	       			.type(RestParamType.path)
	       		.endParam()
	       		.to(Constants.ROUTE_PROCESS_REQUEST);
		
	}

}
