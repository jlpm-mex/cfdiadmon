package net.ellapiz.productos_view.business;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import net.ellapiz.admoncfdiprov.to.FindCfdiProvRequestTO;
import net.ellapiz.admoncfdiprov.to.ProveedorResponseTO;
import net.ellapiz.preventa.vo.MessageVO;
import net.ellapiz.productos_view.util.ConsumeWebServiceAdmonCfdiProv;
import net.ellapiz.productos_view.util.ServicePath;
import net.ellapiz.proveedor.vo.ProveedorVO;

@Component
public class AdmonCfdiProvViewManagement {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdmonCfdiProvViewManagement.class);
	private final int FIND_BY_DATE = 0; 
	
	@Autowired
	private ConsumeWebServiceAdmonCfdiProv cws;
	
	
	public List<ProveedorVO> getAllProveedor() {
		LOGGER.info("getAllProveedor");
		MessageVO msg = null;
		List<ProveedorVO> proveedoresList = new ArrayList<>();
		try {
			msg = cws.executeService(ServicePath.GET_ALL_PROVEEDOR, null, "");
			ProveedorResponseTO response = (ProveedorResponseTO) 
					msg.getBody().get(MessageVO.RESPONSE_MESSAGE);
			proveedoresList = response.getProveedoresList();
			
			
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		
		return proveedoresList;
	}
	
	public MessageVO procesarCdfiProv() {
		LOGGER.info("procesaCfdiProv()");
		MessageVO msg = null;
		try {
			msg = cws.executeService(ServicePath.PROCESAR_CFDI_PROV, null, "");
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		return msg;
	}
	
	
	public MessageVO buscarPorFecha(Date fInicial, Date fFinal, int type) {
		LOGGER.info("buscarPorFecha()");
		MessageVO msg = null;
		
		FindCfdiProvRequestTO request = new FindCfdiProvRequestTO();
		request.setStartDate(fInicial);
		request.setEndDate(fFinal);
		
		String url = type == FIND_BY_DATE ? ServicePath.FIND_BY_DATE : ServicePath.FIND_BY_COMPROBANTE_DATE;
		try {
			msg = cws.executeService(url, request, "");
		}catch(IOException e) {
			LOGGER.error(e.getMessage());
		}
		return msg;
		
	}
	
	public MessageVO buscarPorProveedor(int  year, String rfc) {
		LOGGER.info("buscarPorProveedor");
		MessageVO msg = null;
		
		FindCfdiProvRequestTO request = new FindCfdiProvRequestTO();
		request.setRfc(rfc);
		request.setYear(year);
		
		try {
			msg = cws.executeService(ServicePath.FIND_BY_PROVEEDOR, request, "");
			
		}catch(IOException e) {
			LOGGER.error(e.getMessage());
		}
		return msg;
		
	}

}
