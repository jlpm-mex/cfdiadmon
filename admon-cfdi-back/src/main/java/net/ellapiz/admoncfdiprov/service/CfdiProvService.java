package net.ellapiz.admoncfdiprov.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import net.ellapiz.admoncfdiprov.dao.CompraProvRepository;
import net.ellapiz.admoncfdiprov.dao.PagoProvRepository;
import net.ellapiz.admoncfdiprov.vo.CfdiRecibidoVO;
import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;
import net.ellapiz.admoncfdiprov.vo.PagoVO;
import net.ellapiz.cfdi40.CTipoDeComprobante;

@Component
public class CfdiProvService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(CfdiProvService.class);
	@Autowired
	private CompraProvRepository cpr;
	@Autowired
	private PagoProvRepository ppr;
	
	
	
	public CfdiRecibidoVO guardar(CfdiRecibidoVO cfdiRecibidoVO) {
		LOGGER.info("guardar()");
		
		return
				cpr.save(cfdiRecibidoVO);
	}
	
	public PagoVO guardar(PagoVO pagoVO) {
		LOGGER.info("guardar()");
		
		return
				ppr.save(pagoVO);
	}
	
	public List<ComprobanteVO> findByFolioRfcAndType(
			String fcFolioFiscal, String fcRFC, String tipoDeComprobante){
		LOGGER.info("findCfdiDuplicates()");
		if(tipoDeComprobante.compareTo(CTipoDeComprobante.I.value()) == 0 ||
				tipoDeComprobante.compareTo(CTipoDeComprobante.E.value()) == 0) {
			return
				cpr.findByFolioRfcAndType(fcFolioFiscal, fcRFC);
		}else if(tipoDeComprobante.compareTo(CTipoDeComprobante.P.value()) == 0){
			return 
					ppr.findByFolioRfcAndType(fcFolioFiscal, fcRFC);
		}
	
		return Collections.emptyList();
	}
	
	
	public Page<CfdiRecibidoVO> findByDate(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable){
		LOGGER.info("findByDate()");
		
		return
				cpr.findByFdFechaBetweenOrderByFdFechaDesc(startDate, endDate, pageable);
	}
	
	public Page<PagoVO> findPagoByDate(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable){
		LOGGER.info("findPagoByDate()");
		
		return
				ppr.findByFdFechaBetweenOrderByFdFechaDesc(startDate, endDate, pageable);
	}
	
	public Page<CfdiRecibidoVO> findByFechaComprobante(LocalDate startDate, LocalDate endDate, Pageable pageable){
		LOGGER.info("findByFechaComprobante");
		return
				cpr.findByFdFechaComprobanteBetweenOrderByFdFechaComprobanteDesc(startDate, endDate, pageable);
	}
	
	public Page<PagoVO> findByFechaPago(LocalDate startDate, LocalDate endDate, Pageable pageable){
		LOGGER.info("findByFechaPago");
		return
				ppr.findByFdFechaComprobanteBetweenOrderByFdFechaComprobanteDesc(startDate, endDate, pageable);
	}
	
	public Page<CfdiRecibidoVO> findByProveedor(String proveedorRFC, int year, Pageable pageable){
		LOGGER.info("findByProveedor");
		return cpr.findByProveedor(proveedorRFC, year, pageable);
	}
	
	public Page<PagoVO> findPagoByProveedor(String proveedorRFC, int year, Pageable pageable){
		LOGGER.info("findPagoByProveedor");
		LOGGER.info("proveedorRfc = {}, year ={}", proveedorRFC, year);
		return ppr.findByProveedor(proveedorRFC, year, pageable);
	}
	
}
