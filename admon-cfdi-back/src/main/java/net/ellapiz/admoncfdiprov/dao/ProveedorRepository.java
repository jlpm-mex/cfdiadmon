package net.ellapiz.admoncfdiprov.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import net.ellapiz.proveedor.vo.ProveedorVO;


public interface ProveedorRepository extends CrudRepository<ProveedorVO,Integer>{
	
	@Query("SELECT prov FROM ProveedorVO prov WHERE " +
            "prov.fcRfc LIKE :fcrfc")
	public ProveedorVO buscarPorRFC(@Param("fcrfc") String rfc);
	public List<ProveedorVO> findByfcnombreContaining(String nombre);
	
}
