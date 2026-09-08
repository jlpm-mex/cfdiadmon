package net.ellapiz.admoncfdiprov.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.ellapiz.admoncfdiprov.vo.EmisorVO;




public interface ProveedorRepository extends JpaRepository<EmisorVO,Integer>{
	
	@Query("SELECT prov FROM EmisorVO prov WHERE " +
            "prov.fcRfc LIKE :fcrfc")
	public EmisorVO buscarPorRFC(@Param("fcrfc") String rfc);
	public List<EmisorVO> findByfcnombreContaining(String nombre);
	
}
