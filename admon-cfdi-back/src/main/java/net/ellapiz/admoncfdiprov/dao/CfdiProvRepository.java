package net.ellapiz.admoncfdiprov.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;

@NoRepositoryBean
public interface CfdiProvRepository<T extends ComprobanteVO> extends CrudRepository<T,Integer>{

	@Query("SELECT cpa FROM #{#entityName} cpa WHERE " +
            "cpa.fcFoliofiscal LIKE :fcfoliofiscal"
            + " AND cpa.proveedorVO.fcRfc LIKE :fcrfc")
	public List<ComprobanteVO> findCfdiProvDuplicates(@Param("fcfoliofiscal") String fcfoliofiscal
			, @Param("fcrfc") String fcRFC );
	
//	@Query("SELECT cpa FROM #{#entityName} cpa WHERE "+
//			"Date(cpa.fdFecha) = :fddate "+
//			"order by cpa.fdFechaComprobante DESC")
//	public List<ComprobanteVO> findByDate(@Param("fddate") Date date);
	
	public List<ComprobanteVO> findByFdFechaBetweenOrderByFdFechaDesc(Date startDate, Date endDate);
	public List<ComprobanteVO> findByFdFechaComprobanteBetweenOrderByFdFechaComprobanteDesc(Date startDate, Date endDate);
	
	@Query("SELECT cpa FROM #{#entityName} cpa WHERE " +
            	" cpa.proveedorVO.fcRfc LIKE :fcrfc "+
				" AND date_format(cpa.fdFechaComprobante,'%Y') = :year "+
            	" order by cpa.fdFechaComprobante DESC")
	public List<ComprobanteVO> findByProveedor(@Param("fcrfc") String proveedorRFC, @Param("year") String year);
}