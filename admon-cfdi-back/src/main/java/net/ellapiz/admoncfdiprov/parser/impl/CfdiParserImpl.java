package net.ellapiz.admoncfdiprov.parser.impl;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.ellapiz.admoncfdiprov.exception.AdmonCfdiProvException;
import net.ellapiz.admoncfdiprov.exception.AdmonCfdiProvException.ErrorCodes;
import net.ellapiz.admoncfdiprov.parser.CfdiParser;
import net.ellapiz.admoncfdiprov.vo.CfdiRecibidoVO;
import net.ellapiz.admoncfdiprov.vo.CfdiRelacionadoVO;
import net.ellapiz.admoncfdiprov.vo.CfdiRelacionadosVO;
import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;
import net.ellapiz.admoncfdiprov.vo.DoctoRelacionadoVO;
import net.ellapiz.admoncfdiprov.vo.EmisorVO;
import net.ellapiz.admoncfdiprov.vo.ImpuestosVO;
import net.ellapiz.admoncfdiprov.vo.ItemVO;
import net.ellapiz.admoncfdiprov.vo.PagoItemVO;
import net.ellapiz.admoncfdiprov.vo.PagoVO;
import net.ellapiz.admoncfdiprov.vo.ReceptorVO;
import net.ellapiz.admoncfdiprov.vo.RetencionVO;
import net.ellapiz.admoncfdiprov.vo.TimbreVO;
import net.ellapiz.admoncfdiprov.vo.TrasladoVO;
import net.ellapiz.cfdi40.CTipoDeComprobante;
import net.ellapiz.cfdi40.Comprobante;
import net.ellapiz.cfdi40.Comprobante.CfdiRelacionados;
import net.ellapiz.cfdi40.Comprobante.CfdiRelacionados.CfdiRelacionado;
import net.ellapiz.cfdi40.Comprobante.Complemento;
import net.ellapiz.cfdi40.Comprobante.Conceptos;
import net.ellapiz.cfdi40.Comprobante.Conceptos.Concepto;
import net.ellapiz.cfdi40.common.CFDI40Commons;
import net.ellapiz.cfdi40.comp_pago.Pagos;
import net.ellapiz.cfdi40.comp_pago.Pagos.Pago;
import net.ellapiz.cfdi40.comp_pago.Pagos.Pago.DoctoRelacionado;
import net.ellapiz.cfdi40.comp_pago.Pagos.Pago.DoctoRelacionado.ImpuestosDR;
import net.ellapiz.cfdi40.comp_pago.Pagos.Pago.DoctoRelacionado.ImpuestosDR.RetencionesDR;
import net.ellapiz.cfdi40.comp_pago.Pagos.Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR;
import net.ellapiz.cfdi40.tfd11.TimbreFiscalDigital;



@Component
public class CfdiParserImpl implements CfdiParser {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	public ComprobanteVO crearComprobanteVO(File xml) throws Exception {
		LOGGER.info("crearComprobanteVO()");
		ComprobanteVO comprobanteVO = null;
		try {
				CFDI40Commons cfdiCommons 	= new CFDI40Commons();
				Comprobante comprobante 	=  cfdiCommons.xmlToComprobante(xml.toString());

				String tipoDeComprobante 	= comprobante.getTipoDeComprobante().value();
				String folio 				= comprobante.getFolio() == null ? "" : comprobante.getFolio();
				String serie				= comprobante.getSerie() == null ? "" : comprobante.getSerie();
				XMLGregorianCalendar calendar = comprobante.getFecha();
				LocalDateTime date = calendar.toGregorianCalendar()
						.toZonedDateTime().toLocalDateTime();

				String rfcEmisor	= comprobante.getEmisor().getRfc();
				String nombre 		= comprobante.getEmisor().getNombre();
				String usoCfdi 		= comprobante.getReceptor().getUsoCFDI().value();
				String rfcReceptor 	= comprobante.getReceptor().getRfc();
				String uuid 		= extraerUUID(comprobante);
				String moneda		= comprobante.getMoneda().name();
				BigDecimal descuento = comprobante.getDescuento() == null ? new BigDecimal("0.00") : comprobante.getDescuento();
				BigDecimal subtotal = comprobante.getSubTotal();
				

				EmisorVO emisorVO = new EmisorVO();
				emisorVO.setFcnombre(nombre);
				emisorVO.setFcRfc(rfcEmisor);
				emisorVO.setFcRegimenFiscal(comprobante.getEmisor().getRegimenFiscal());
				
				ReceptorVO receptorVO = new ReceptorVO();
				receptorVO.setFcNombre(comprobante.getReceptor().getNombre());
				receptorVO.setFcRegimenFiscalReceptor(comprobante.getReceptor().getRegimenFiscalReceptor());
				receptorVO.setFcRfc(comprobante.getReceptor().getRfc());
				receptorVO.setFcDomicilioFiscalReceptor(comprobante.getReceptor().getDomicilioFiscalReceptor());
				receptorVO.setFcUsoCFDI(comprobante.getReceptor().getUsoCFDI().name());
				
				
				TimbreFiscalDigital tfd = comprobante.getComplemento().getAny()
						.stream().filter(obj -> obj instanceof TimbreFiscalDigital)
						.map(obj -> (TimbreFiscalDigital)obj)
						.findFirst()
						.orElse(null);
				
				TimbreVO timbreVO = new TimbreVO();
				XMLGregorianCalendar xmlCalendar = tfd.getFechaTimbrado();
				LocalDateTime dateTimbre = xmlCalendar.toGregorianCalendar()
						.toZonedDateTime().toLocalDateTime();
				timbreVO.setFechaTimbrado(dateTimbre);
				timbreVO.setNoCertificadoSAT(tfd.getNoCertificadoSAT());
				timbreVO.setRfcProvCertif(tfd.getRfcProvCertif());
				timbreVO.setSelloCFD(tfd.getSelloCFD());
				timbreVO.setSelloSAT(tfd.getSelloSAT());
				timbreVO.setUuid(tfd.getUUID());
				
				
				LOGGER.info("rfcProveedor: {}",rfcEmisor);
				LOGGER.info("folioFiscal: {}", uuid);
				LOGGER.info("fechaComprobante: {}",date);
					
				if(tipoDeComprobante.compareTo(CTipoDeComprobante.P.value()) != 0 && 
						tipoDeComprobante.compareTo(CTipoDeComprobante.I.value()) != 0 &&
							tipoDeComprobante.compareTo(CTipoDeComprobante.E.value()) != 0){
					StringBuilder sb = new StringBuilder();
					sb.append("El Archivo: ");
					sb.append(xml.getName());
					sb.append(", con el tipo de comprobante: ");
					sb.append(comprobante.getTipoDeComprobante());
					sb.append(" no se puede procesar, por que solo se admiten tipos P,E o I");
					LOGGER.error("{}",sb);
					throw new AdmonCfdiProvException(sb.toString(),
							ErrorCodes.TIPO_DE_COMPROBANTE_NO_RECONOCIDO.getCodigo());
				}
				
				if(tipoDeComprobante.compareTo(CTipoDeComprobante.P.value()) == 0) {
					comprobanteVO = crearPagoVO(comprobante);
				}
				
				if(tipoDeComprobante.compareTo(CTipoDeComprobante.I.value()) == 0 ||
						tipoDeComprobante.compareTo(CTipoDeComprobante.E.value()) == 0) {
					comprobanteVO = crearCompraVO(comprobante);
				}
				
				if(comprobanteVO != null) {
					comprobanteVO.setEmisorVO(emisorVO);
					comprobanteVO.setReceptorVO(receptorVO);
					comprobanteVO.setFcUsoCFDI(usoCfdi);
					comprobanteVO.setFcFoliofiscal(uuid);
					comprobanteVO.setFdFechaComprobante(date);
					comprobanteVO.setFcRfcReceptor(rfcReceptor);
					comprobanteVO.setTipoDeComprobante(tipoDeComprobante);
					comprobanteVO.setFcFolio(folio);
					comprobanteVO.setFcSerie(serie);
					comprobanteVO.setFcMoneda(moneda);
					comprobanteVO.setDescuento(descuento);
					comprobanteVO.setSubTotal(subtotal);
					comprobanteVO.setTimbreVO(timbreVO);
				}
				
		}catch(AdmonCfdiProvException e) {
			LOGGER.error(e.getMessage());
			throw e;
		}catch(Exception e) {
			LOGGER.error(e.getMessage());
			throw new Exception("xml inválido, no pudo ser parseado");
		}
		
		return comprobanteVO;
	}
	
	private String extraerUUID(Comprobante comprobante) {
		LOGGER.info("extraerUUID");
		Complemento complemento = comprobante.getComplemento(); 
		for(Object obj : complemento.getAny()) {
			try {
				return ((TimbreFiscalDigital) obj).getUUID();
			}catch(ClassCastException cce) {
				System.out.println("El complemento no es del tipo TFD");
			}
		}
		LOGGER.info("El archivo no contiene UUID");
		return "";
	}
	
	private CfdiRecibidoVO crearCompraVO(Comprobante cfdi) {
		
		CfdiRecibidoVO cfdiRecibidoVO = new CfdiRecibidoVO();
		
		String formaDePago 	= cfdi.getFormaPago();
		String metodoDePago = cfdi.getMetodoPago().value();
		cfdiRecibidoVO.setFdTotal(cfdi.getTotal());
		cfdiRecibidoVO.setFcFormaDePago(formaDePago);
		cfdiRecibidoVO.setFcMetodoDePago(metodoDePago);
		List<ItemVO> items = new ArrayList<>();
		
		Conceptos conceptos = cfdi.getConceptos();
		
		for(Concepto concepto : conceptos.getConcepto()) {
			ItemVO item = new ItemVO(); 
			item.setFcDescripcion(concepto.getDescripcion());
			item.setFdCantidad(concepto.getCantidad());
			BigDecimal fdDescuento 	 		= concepto.getDescuento() == null ? new BigDecimal("0.0") : concepto.getDescuento();	 
			BigDecimal precioUnitario 		= concepto.getValorUnitario();
			BigDecimal importe				= concepto.getImporte();
			item.setFdPrecioUnitario(precioUnitario);
			item.setFcClaveProducto(concepto.getNoIdentificacion());
			item.setCfdiRecibidoVO(cfdiRecibidoVO);
			item.setFdImporte(importe);
			item.setFdDescuento(fdDescuento);
			item.setFcNumeroDeIdentificacion(concepto.getNoIdentificacion());
			items.add(item);
		}
		cfdiRecibidoVO.setItems(items);
		 
		if(cfdi.getCfdiRelacionados() != null) {
			List<CfdiRelacionados> cfdiRelacionadosList = cfdi.getCfdiRelacionados();
			List<CfdiRelacionadosVO> myCfdiRelacionadoList = new ArrayList<>(); 
		
			for(CfdiRelacionados cfdiRelacionados : cfdiRelacionadosList) {
				CfdiRelacionadosVO cfdiRelacionadosVO = new CfdiRelacionadosVO();
				cfdiRelacionados.setTipoRelacion(cfdiRelacionados.getTipoRelacion());
				List<CfdiRelacionadoVO> cfdiRelacionadoList = new ArrayList<>();
				for(CfdiRelacionado cfdiRelacionado : cfdiRelacionados.getCfdiRelacionado()) {
					CfdiRelacionadoVO cfdiRelacionadoVO = new CfdiRelacionadoVO();
					cfdiRelacionadoVO.setFcUUID(cfdiRelacionado.getUUID());
					cfdiRelacionadoVO.setFcUUID(metodoDePago);
					cfdiRelacionadoList.add(cfdiRelacionadoVO);
				}
				
				cfdiRelacionadosVO.setCfdiRelacionadoList(cfdiRelacionadoList);
				myCfdiRelacionadoList.add(cfdiRelacionadosVO);
			}
			
			cfdiRecibidoVO.setCfdiRelacionadoList(myCfdiRelacionadoList);
		}
		
		return cfdiRecibidoVO;

	}
	
	private PagoVO crearPagoVO(Comprobante cfdi) {
		PagoVO pagoVO = new PagoVO();
		Complemento complemento = cfdi.getComplemento();
		List<PagoItemVO> pagoList = new ArrayList<>();
		
		for(Object obj : complemento.getAny()) {
			try {
				PagoItemVO pagoItemVO = new PagoItemVO();
				Pagos pagos = ((Pagos) obj);
				
				for(Pago pago : pagos.getPago()) {
					pagoItemVO.setFcFormaDePagoP(pago.getFormaDePagoP());
					XMLGregorianCalendar calendar = pago.getFechaPago();
					LocalDateTime date = calendar.toGregorianCalendar()
							.toZonedDateTime().toLocalDateTime();
					pagoItemVO.setFdFechaPago(date);
					pagoItemVO.setFdMonto(pago.getMonto());
					List<DoctoRelacionadoVO> documentosRelacionados = new ArrayList<>();
					for(DoctoRelacionado doctoRelacionado : pago.getDoctoRelacionado()) {
//						pagoItemVO.setFcFolio(doctoRelacionado.getFolio());
//						pagoItemVO.setFcSerie(doctoRelacionado.getSerie());
//						pagoItemVO.setFcUuid(doctoRelacionado.getIdDocumento());
						DoctoRelacionadoVO documentoRelacionadoVO = 
								new DoctoRelacionadoVO();
						documentoRelacionadoVO.setIdDocumento(doctoRelacionado.getIdDocumento());
						documentoRelacionadoVO.setFolio(doctoRelacionado.getFolio());
						documentoRelacionadoVO.setEquivalenciaDR(doctoRelacionado.getEquivalenciaDR());
						documentoRelacionadoVO.setImpPagado(doctoRelacionado.getImpPagado());
						documentoRelacionadoVO.setImpSaldoAnt(doctoRelacionado.getImpSaldoAnt());
						documentoRelacionadoVO.setImpSaldoInsoluto(doctoRelacionado.getImpSaldoInsoluto());
						
						List<RetencionVO> retenciones = Optional.ofNullable(doctoRelacionado.getImpuestosDR())
						        .map(ImpuestosDR::getRetencionesDR)
						        .map(RetencionesDR::getRetencionDR)
						        .orElseGet(Collections::emptyList)
						        .stream()
						        .map(r -> {
						            RetencionVO retencionVO = new RetencionVO();
						            retencionVO.setBase(r.getBaseDR());
						            retencionVO.setImporte(r.getImporteDR());
						            retencionVO.setImpuesto(r.getImpuestoDR());
						            retencionVO.setTasaOCuota(r.getTasaOCuotaDR());
						            if (r.getTipoFactorDR() != null) {
						                retencionVO.setTipoFactor(r.getTipoFactorDR().name());
						            }
						            return retencionVO;
						        })
						        .collect(Collectors.toList());
						
						List<TrasladoVO> traslados =  Optional.ofNullable(doctoRelacionado.getImpuestosDR())
								.map(ImpuestosDR::getTrasladosDR)
								.map(TrasladosDR::getTrasladoDR)
								.orElseGet(Collections::emptyList)					
								.stream().map(t -> {
								TrasladoVO trasladoVO = new TrasladoVO();
								trasladoVO.setBase(t.getBaseDR());
								trasladoVO.setImporte(t.getImporteDR());
								trasladoVO.setImpuesto(t.getImpuestoDR());
								trasladoVO.setTasaOCuota(t.getTasaOCuotaDR());
								trasladoVO.setTipoFactor(t.getTipoFactorDR().name());
								
								return trasladoVO;
							}).collect(Collectors.toList());
						
						ImpuestosVO impuestosVO = new ImpuestosVO();
						impuestosVO.setRetenciones(retenciones);
						impuestosVO.setTraslados(traslados);

						documentoRelacionadoVO.setImpuestosDR(impuestosVO);
						documentoRelacionadoVO.setMonedaDR(doctoRelacionado.getMonedaDR().name());
						documentoRelacionadoVO.setNumParcialidad(doctoRelacionado.getNumParcialidad());
						documentoRelacionadoVO.setObjetoImpDR(doctoRelacionado.getObjetoImpDR());
						documentoRelacionadoVO.setSerie(doctoRelacionado.getSerie());
						documentosRelacionados.add(documentoRelacionadoVO);
					}
					pagoItemVO.setDoctosRelacionados(documentosRelacionados);
					pagoItemVO.setPagoVO(pagoVO);
					pagoList.add(pagoItemVO);
					pagoVO.setPagos(pagoList);
					pagoVO.setFdTotal(cfdi.getTotal());
					pagoVO.setMontoTotalPagos(pagos.getTotales().getMontoTotalPagos());
				}
				
			}catch(ClassCastException cce) {
				LOGGER.error("El complemento no es del tipo pago");
			}
		}
		
		pagoVO.setPagos(pagoList);
		
		return pagoVO;
	}
	


}
