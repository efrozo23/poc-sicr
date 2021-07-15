package co.com.routes;


import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLContext;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
	
	
	@Value("${folder}")
	private String certificate;
	
	 

	@Override
	public void configure() throws Exception {
		// TODO Auto-generated method stub
		
		setupSSLConext();
		
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
	 	.log(LoggingLevel.ERROR, logger, "Error encontrado: ${body}")
	 	.log(LoggingLevel.ERROR, logger, "Error encontrado: ${exception.stacktrace}")
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
			.toD().allowOptimisedComponents(false).cacheSize(10)
			.uri("https://dummy?sslContextParameters=#getSSLContextParameters&ssl=true&httpClient.connectTimeout={{servicio.connection.timeout}}&httpClient.socketTimeout={{servicio.connection.timeout}}&throwExceptionOnFailure=true")
			
			//.to(httpsEndpoint)
			.end();
		
	}
	
	private void setupSSLConext() throws Exception {
		      		

		KeyStoreParameters ksp = new KeyStoreParameters();
		ksp.setResource(certificate);
		ksp.setPassword("password");

		KeyManagersParameters kmp = new KeyManagersParameters();
		kmp.setKeyStore(ksp);
		kmp.setKeyPassword("password");
		

		
		SSLContextParameters scp = new SSLContextParameters();
		scp.setKeyManagers(kmp);
		scp.setSecureSocketProtocol("SSL");
		

		HttpComponent httpComponent = getContext().getComponent("https", HttpComponent.class);
		httpComponent.setSslContextParameters(scp);
		
		 httpComponent.setX509HostnameVerifier(new AllowAllHostnameVerifier());
		 System.out.println("------------------------"+System.getProperty("https.protocols"));
        
    }

}
