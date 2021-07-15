package co.com;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.servlet.Servlet;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MySpringBootApplication {
	
	@Value("${folder}")
	private String certificate;

    /**
     * A main method to start this application.
     */
    public static void main(String[] args) {
    	System.setProperty("https.protocols", "TLSv1.2");
    	System.out.println(System.getProperty("https.protocols"));
        SpringApplication.run(MySpringBootApplication.class, args);
    }
    
    @Bean
    public ServletRegistrationBean<Servlet> servletRegistrationBean() {
        ServletRegistrationBean<Servlet> registration = new ServletRegistrationBean<>(new CamelHttpTransportServlet(), "/*");
        registration.setName("CamelServlet");
        return registration;
    }
    
    @Bean("getSSLContextParameters")
    public SSLContextParameters getSSLContextParameters() throws NoSuchAlgorithmException {
		KeyStoreParameters ksp = new KeyStoreParameters();
		ksp.setResource(certificate);
		ksp.setPassword("password");

		KeyManagersParameters kmp = new KeyManagersParameters();
		kmp.setKeyStore(ksp);
		kmp.setKeyPassword("password");

		
		
		SSLContextParameters scp = new SSLContextParameters();
		scp.setKeyManagers(kmp);
		scp.setSecureSocketProtocol("TLSv1.2");
		
		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		
		 SSLConnectionSocketFactory f = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1.2" }, null,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		
		System.out.println("***********************"+System.getProperty("https.protocols"));
		return scp;
    }
  

}
