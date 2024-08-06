package net.ellapiz.admoncfdiprov.service;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.ellapiz.admoncfdiprov.dao.ProveedorRepository;
import net.ellapiz.proveedor.vo.ProveedorVO;

@Component
public class ProveedorService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProveedorService.class);
	
	@Autowired
	private ProveedorRepository proveedorRepository;
	
	public ProveedorVO guardarProveedor(ProveedorVO proveedorVO) throws SQLException{
		return
				proveedorRepository.save(proveedorVO);
	}
	
	public ProveedorVO buscarPorRfc(String rfc) {
		return
				proveedorRepository.buscarPorRFC(rfc);
	}
	
	public List<ProveedorVO> getAllProveedor(){
		LOGGER.info("getAllProveedor()");
		
		return
				(List<ProveedorVO>) proveedorRepository.findAll();
	}
	
}
