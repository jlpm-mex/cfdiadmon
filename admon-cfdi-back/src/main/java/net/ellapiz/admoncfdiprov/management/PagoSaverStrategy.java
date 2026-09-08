package net.ellapiz.admoncfdiprov.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.ellapiz.admoncfdiprov.service.CfdiProvService;
import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;
import net.ellapiz.admoncfdiprov.vo.PagoVO;
import net.ellapiz.cfdi40.CTipoDeComprobante;

@Component  
public class PagoSaverStrategy implements ComprobanteSaverStrategy {
	@Autowired
	private CfdiProvService service;  

    @Override                                                                                                                                                                                                                             
    public boolean supports(String tipo) { return CTipoDeComprobante.P.value().equalsIgnoreCase(tipo); }       
    
    @Override                                                                                                                                                                                                                             
    public ComprobanteVO guardar(ComprobanteVO comprobante) { return service.guardar((PagoVO) comprobante); }  

}
