package net.ellapiz.admoncfdiprov.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	public List<ComprobanteVO> findCfdiDuplicates(
			String fcFolioFiscal, String fcRFC, String tipoDeComprobante){
		LOGGER.info("findCfdiDuplicates()");
		if(tipoDeComprobante.compareTo(CTipoDeComprobante.I.value()) == 0) {
			return
				cpr.findCfdiProvDuplicates(
						fcFolioFiscal, fcRFC);
		}else if(tipoDeComprobante.compareTo(CTipoDeComprobante.P.value()) == 0){
			return 
					ppr.findCfdiProvDuplicates(
					fcFolioFiscal, fcRFC);
		}
	
		return Collections.emptyList();
	}
	
	
	public List<ComprobanteVO> findByDate(Date startDate, Date endDate){
		LOGGER.info("findByDate()");
		
		return
				cpr.findByFdFechaBetweenOrderByFdFechaDesc(startDate, endDate);
	}
	
	public List<ComprobanteVO> findPagoByDate(Date startDate, Date endDate){
		LOGGER.info("findPagoByDate()");
		
		return
				ppr.findByFdFechaBetweenOrderByFdFechaDesc(startDate, endDate);
	}
	
	public List<ComprobanteVO> findByFechaComprobante(Date startDate, Date endDate){
		LOGGER.info("findByFechaComprobante");
		return
				cpr.findByFdFechaComprobanteBetweenOrderByFdFechaComprobanteDesc(startDate, endDate);
	}
	
	public List<ComprobanteVO> findByFechaPago(Date startDate, Date endDate){
		LOGGER.info("findByFechaPago");
		return
				ppr.findByFdFechaComprobanteBetweenOrderByFdFechaComprobanteDesc(startDate, endDate);
	}
	
	public List<ComprobanteVO> findByProveedor(String proveedorRFC, String year){
		LOGGER.info("findByProveedor");
		return cpr.findByProveedor(proveedorRFC, year);
	}
	
	public List<ComprobanteVO> findPagoByProveedor(String proveedorRFC, String year){
		LOGGER.info("findPagoByProveedor");
		return ppr.findByProveedor(proveedorRFC, year);
	}
	
}
