package com.etester.dataloader;

import java.io.FileInputStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.etester.dataloader.util.DataloaderService;
import com.etester.dataloader.util.DataloaderServiceImpl;
import com.rulefree.rfloaddataschema.LoadSectionsRequest;
import com.rulefree.rfloaddataschema.LoadSectionsResponse;

import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Profile("!cloud")
public class EtesterLoadCommandLineRunner implements CommandLineRunner {
	private DataloaderServiceImpl dataloaderServiceImpl;
	private final ConfigurableApplicationContext applicationContext;

	public EtesterLoadCommandLineRunner(DataloaderServiceImpl dataloaderServiceImpl,
			ConfigurableApplicationContext applicationContext) {
		this.dataloaderServiceImpl = dataloaderServiceImpl;
		this.applicationContext = applicationContext;
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("EXECUTING : command line runner");

		for (int i = 0; i < args.length; ++i) {
			log.info("args[{}]: {}", i, args[i]);
		}

		try {
			Resource resource = new ClassPathResource("LoadParagraphCorrectionXML.xml");
			FileInputStream inputFile = new FileInputStream(resource.getFile());

			// parse the XML and return an instance of the AppConfig class
//			File inputFile = new File(
//					"C:\\Eclipse\\Workspaces\\GXTPlaypen\\rfserver\\rfservice\\src\\main\\java\\com\\rulefree\\dataloader\\LoadParagraphCorrectionXML.xml");
//			LoadSectionsRequest sectionsPayload = (LoadSectionsRequest) dataloaderServiceImpl
//					.getPayloadUnmarshaller(LoadSectionsRequest.class).unmarshal(inputFile);
			// create a blank response
			LoadSectionsResponse response = new LoadSectionsResponse();
			response.setStatus(DataloaderService.UPLOAD_SUCCESS_STATUS_CODE);
			response.setMessage(DataloaderService.UPLOAD_SUCCESS_MESSAGE);
			dataloaderServiceImpl.getPayloadMarshaller(LoadSectionsResponse.class).marshal(response, System.out);
		} catch (JAXBException e) {
			// if things went wrong...
			System.out.println("error parsing xml: ");
			e.printStackTrace();
			// force quit
			System.exit(1);
		}

//		log.info("Execution complete. Shutting down");
//		applicationContext.close();

	}

}
