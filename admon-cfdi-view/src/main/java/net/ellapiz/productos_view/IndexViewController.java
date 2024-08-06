package net.ellapiz.productos_view;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import net.ellapiz.productos_view.business.AdmonCfdiProvViewManagement;
import net.ellapiz.proveedor.vo.ProveedorVO;

@Controller
public class IndexViewController {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	@Autowired
	private AdmonCfdiProvViewManagement management;
	
    @GetMapping("/")
    public String index(Model model) {
    	LOGGER.info("index()");
    	
    	List<ProveedorVO> proveedoresList = management.getAllProveedor();
    	model.addAttribute("proveedoresList",proveedoresList);
    	
        return "index";
    }

}
