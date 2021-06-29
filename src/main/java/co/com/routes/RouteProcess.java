package co.com.routes;


import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import co.com.dto.Response;
import co.com.util.Constants;

@Component
public class RouteProcess extends RouteBuilder{
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	 

	@Override
	public void configure() throws Exception {
		// TODO Auto-generated method stub
		
		onException(UnknownHostException.class)
	 	.handled(true)
	 	.setBody(constant("ERROR CONEXIÓN"))
	 	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
	 	.end();
		
		onException(ConnectTimeoutException.class)
	 	.handled(true)
	 	.setBody(constant("ERROR CONEXIÓN"))
	 	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
	 	.end();
		
		onException(Exception.class)
	 	.handled(true)
	 	.setBody(constant("ERROR INTERNO"))
	 	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
	 	.end();
		
		from(Constants.ROUTE_PROCESS_REQUEST).routeId("ROUTE_PROCESS_REQUEST").streamCaching()
			.log(LoggingLevel.INFO, logger, "Inicio la ruta: ${body}")
			.setHeader("ip", simple("{{service.sirc.ip}}"))
			.setHeader("username", simple("{{service.sirc.username}}"))
			.setHeader("password", simple("{{service.sirc.password}}"))
			.to("velocity://request.vm?contentCache=true&propertiesFile=velocity.properties")
			.log(LoggingLevel.INFO, logger, "Request al servicio: ${body}")
			.to(Constants.ROUTE_CONSUME_SERVICE)
			.log(LoggingLevel.INFO, logger, "Respuesta con exito")
			.process(x->{
	 			String body = x.getIn().getBody(String.class);
				XmlMapper xmlMapper = new XmlMapper();
				xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				
				Response dto = xmlMapper.readValue(body.getBytes(StandardCharsets.UTF_8), Response.class);
			    			
				ObjectMapper objectMapper = new ObjectMapper();
				
				
				String jsondto = objectMapper.writeValueAsString(dto);
				x.getIn().setBody(jsondto);
				
				
				
			})
			.log(LoggingLevel.INFO, logger, "Finalizo")
			.removeHeaders("*")
			.unmarshal().json(JsonLibrary.Jackson)
			.setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
			.end();
		
		from(Constants.ROUTE_CONSUME_SERVICE).routeId("ROUTE_CONSUME_SERVICE").streamCaching()
			.removeHeaders("*")
			.setHeader(Exchange.HTTP_URI, simple("{{service.sirc.url}}"))
			.setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_XML))
			.setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
			.to("http:dummy?httpClient.connectTimeout={{servicio.connection.timeout}}&httpClient.socketTimeout={{servicio.connection.timeout}}&throwExceptionOnFailure=true")
			.end();
		
	}

}
