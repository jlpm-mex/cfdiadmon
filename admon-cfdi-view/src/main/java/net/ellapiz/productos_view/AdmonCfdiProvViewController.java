package net.ellapiz.productos_view;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import net.ellapiz.preventa.vo.MessageVO;
import net.ellapiz.productos_view.business.AdmonCfdiProvViewManagement;

@Controller
public class AdmonCfdiProvViewController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdmonCfdiProvViewController.class);
	@Autowired
	private AdmonCfdiProvViewManagement management;
	    
    @GetMapping("/procesarcfdiprov")
    public @ResponseBody MessageVO procesarCfdiProv(){
    	LOGGER.info("procesarCfdiProv()");
    	
    	return management.procesarCdfiProv();
    }
    
	@GetMapping("/buscarporfecha")
	public @ResponseBody MessageVO buscarPorFecha(@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd")  Date fInicial
			, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date fFinal, @RequestParam int findByDate) {
		LOGGER.info("buscarporfecha()");
		
		return management.buscarPorFecha(fInicial, fFinal, findByDate);
	
	}
	
	@GetMapping("/buscarporproveedor")
	public @ResponseBody MessageVO buscarPorProveedor(@RequestParam int year
			, @RequestParam String rfc) {
		LOGGER.info("buscarPorProveedor()");
		
		return management.buscarPorProveedor(year, rfc);
	
	}
}
