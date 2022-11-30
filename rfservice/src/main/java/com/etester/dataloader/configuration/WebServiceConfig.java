package com.etester.dataloader.configuration;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {
	@Bean
	public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(applicationContext);
		servlet.setTransformWsdlLocations(true);
		return new ServletRegistrationBean<>(servlet, "/ws/*");
	}

//	@Bean(name = "tests")
//	public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema rfloaddataSchema) {
//		DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
//		wsdl11Definition.setPortTypeName("TestsPort");
//		wsdl11Definition.setLocationUri("/ws");
//		wsdl11Definition.setTargetNamespace("http://spring.io/guides/gs-producing-web-service");
//		wsdl11Definition.setSchema(rfloaddataSchema);
//		return wsdl11Definition;
//	}
//
//	@Bean
//	public XsdSchema rfloaddataSchema() {
//		return new SimpleXsdSchema(new ClassPathResource("RfLoadDataSchema.xsd"));
//	}
	
	
	
	// *********************************************************************************************************************
	// Follow this approach if your XSD has includes for other xsd's
	// https://stackoverflow.com/questions/42112775/spring-ws-wsdl-automatic-exposure-xsd-import-are-not-followed
	// *********************************************************************************************************************
	@Bean(name="tests")
	public DefaultWsdl11Definition defaultWsdl11Definition() throws Exception {
	    DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
	    wsdl11Definition.setPortTypeName("TestsPort");
	    wsdl11Definition.setLocationUri("/ws");
	    wsdl11Definition.setTargetNamespace("http://spring.io/guides/gs-producing-web-service");
	    wsdl11Definition.setSchemaCollection(updateContactXsd());
	    return wsdl11Definition;
	}   

	@Bean
	public XsdSchemaCollection updateContactXsd() throws Exception {
	    CommonsXsdSchemaCollection xsds = new CommonsXsdSchemaCollection(new ClassPathResource("RfLoadDataSchema.xsd"));
	    xsds.setInline(true); 
	    return xsds;
	}
	
}