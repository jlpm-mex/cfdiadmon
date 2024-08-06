package net.ellapiz.admoncfdiprov.management;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.ellapiz.admoncfdiprov.service.ProveedorService;
import net.ellapiz.proveedor.vo.ProveedorVO;

@Component
public class ProveedorManagement {
	
	@Autowired
	private ProveedorService service;
	
	public ProveedorVO guardarProveedor(ProveedorVO proveedorVO) throws SQLException{
		return
				service.guardarProveedor(proveedorVO);
	}
	
	public ProveedorVO buscarPorRFC(String rfc) {
		return
				service.buscarPorRfc(rfc);
	}
	
	public List<ProveedorVO> getAllProveedor(){
		return
				service.getAllProveedor();
	}
}
