package net.ellapiz.productos_view.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConsumeWebServiceAdmonCfdiProv extends ConsumeWebService {

	public ConsumeWebServiceAdmonCfdiProv(@Value("${backend.admon.server.url}") String backendServerURL) {
		super(backendServerURL);
	}

}
