package net.ellapiz.admoncfdiprov.management;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.ellapiz.admoncfdiprov.service.ProveedorService;
import net.ellapiz.admoncfdiprov.vo.EmisorVO;


@Component
public class ProveedorManagement {
	
	@Autowired
	private ProveedorService service;
	
	public EmisorVO guardarProveedor(EmisorVO proveedorVO) throws SQLException{
		return
				service.guardarProveedor(proveedorVO);
	}
	
	
	/**
	 * Gets the provider by its Registro Federal de Contribuyentes RFC.
	 * 
	 * @param the provider RFC
	 * 
	 * @return an object {@link EmisorVO};
	 */
	public EmisorVO buscarPorRFC(String rfc) {
		return
				service.buscarPorRfc(rfc);
	}
	
	/**
	 * Gets all the on boarded providers.
	 * 
	 * @param 
	 * 
	 * @return a list of objects {@link ProveedorVO};
	 */
	public List<EmisorVO> getAllProveedor(){
		return
				service.getAllProveedor();
	}
}
