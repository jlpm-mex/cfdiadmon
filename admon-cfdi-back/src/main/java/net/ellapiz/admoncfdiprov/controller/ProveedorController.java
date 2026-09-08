package net.ellapiz.admoncfdiprov.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.ellapiz.admoncfdiprov.management.ProveedorManagement;
import net.ellapiz.admoncfdiprov.vo.EmisorVO;



@RequestMapping("/api/cfdis/proveedores")
@RestController
public class ProveedorController {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ProveedorManagement management;
	
	/**
	 * Gets all the on boarded suppliers.
	 * 
	 * @param 
	 * 
	 * @return a list of objects {@link ProveedorVO};
	 */
	@CrossOrigin(origins = "${upload.files.cross.origin}")
	@GetMapping
	private ResponseEntity<List<EmisorVO>> getProveedores() {
		LOGGER.info("getAllProveedor()");
	
		List<EmisorVO> proveedorList = management.getAllProveedor();	
		
		return ResponseEntity.status(HttpStatus.OK)
                .body(proveedorList);
	}

}
