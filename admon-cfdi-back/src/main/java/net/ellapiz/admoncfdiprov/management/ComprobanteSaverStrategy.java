package net.ellapiz.admoncfdiprov.management;

import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;

public interface ComprobanteSaverStrategy {
    boolean supports(String tipoDeComprobante);                                                                                                                                                                                           
    ComprobanteVO guardar(ComprobanteVO comprobanteVO); 
}
