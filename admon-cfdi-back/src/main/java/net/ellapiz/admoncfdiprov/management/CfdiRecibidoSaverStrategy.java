package net.ellapiz.admoncfdiprov.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.ellapiz.admoncfdiprov.service.CfdiProvService;
import net.ellapiz.admoncfdiprov.vo.CfdiRecibidoVO;
import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;
import net.ellapiz.cfdi40.CTipoDeComprobante;

@Component  
public class CfdiRecibidoSaverStrategy implements ComprobanteSaverStrategy {
	
	@Autowired
	private CfdiProvService service;

	@Override
	public boolean supports(String tipoDeComprobante) { return !CTipoDeComprobante.P.value().equalsIgnoreCase(tipoDeComprobante); }

	@Override
	public ComprobanteVO guardar(ComprobanteVO comprobanteVO) { return service.guardar((CfdiRecibidoVO) comprobanteVO);	}


}
