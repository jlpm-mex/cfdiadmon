package net.ellapiz.admoncfdiprov.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;

@NoRepositoryBean
public interface CfdiProvRepository<T extends ComprobanteVO> extends JpaRepository<T,Integer>{

	@Query("SELECT cpa FROM #{#entityName} cpa WHERE " +
            "cpa.fcFoliofiscal LIKE :fcfoliofiscal"
            + " AND cpa.emisorVO.fcRfc LIKE %:fcrfc%")
	public List<ComprobanteVO> findByFolioRfcAndType(@Param("fcfoliofiscal") String fcfoliofiscal
			, @Param("fcrfc") String fcRFC );
	
//	@Query("SELECT cpa FROM #{#entityName} cpa WHERE "+
//			"Date(cpa.fdFecha) = :fddate "+
//			"order by cpa.fdFechaComprobante DESC")
//	public List<ComprobanteVO> findByDate(@Param("fddate") Date date);
	
	public Page<T> findByFdFechaBetweenOrderByFdFechaDesc(
			LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
			
	public Page<T> findByFdFechaComprobanteBetweenOrderByFdFechaComprobanteDesc(
			LocalDate startDate, LocalDate endDate, Pageable pageable);
	
	@Query("SELECT cpa FROM #{#entityName} cpa WHERE " +
            	" cpa.emisorVO.fcRfc LIKE :fcrfc "+
				" AND date_format(cpa.fdFechaComprobante,'%Y') = :year "+
            	" order by cpa.fdFechaComprobante DESC")
	public Page<T> findByProveedor(@Param("fcrfc") String proveedorRFC,
			@Param("year") int year, Pageable pageable);
}