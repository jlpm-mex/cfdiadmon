package net.ellapiz.admoncfdiprov.management;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.ellapiz.admoncfdiprov.exception.AdmonCfdiProvException;
import net.ellapiz.admoncfdiprov.exception.AdmonCfdiProvException.ErrorCodes;
import net.ellapiz.admoncfdiprov.service.CfdiProvService;
import net.ellapiz.admoncfdiprov.to.FindCfdiProvResponseTO;
import net.ellapiz.admoncfdiprov.to.ProcesarCfdiResponseTO;
import net.ellapiz.admoncfdiprov.vo.CfdiRecibidoVO;
import net.ellapiz.admoncfdiprov.vo.CfdiRelacionadoVO;
import net.ellapiz.admoncfdiprov.vo.CfdiRelacionadosVO;
import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;
import net.ellapiz.admoncfdiprov.vo.ItemVO;
import net.ellapiz.admoncfdiprov.vo.PagoItemVO;
import net.ellapiz.admoncfdiprov.vo.PagoVO;
import net.ellapiz.cfdi40.CTipoDeComprobante;
import net.ellapiz.cfdi40.Comprobante;
import net.ellapiz.cfdi40.Comprobante.CfdiRelacionados;
import net.ellapiz.cfdi40.Comprobante.CfdiRelacionados.CfdiRelacionado;
import net.ellapiz.cfdi40.Comprobante.Complemento;
import net.ellapiz.cfdi40.Comprobante.Conceptos;
import net.ellapiz.cfdi40.Comprobante.Conceptos.Concepto;
import net.ellapiz.cfdi40.comp_pago.Pagos;
import net.ellapiz.cfdi40.comp_pago.Pagos.Pago;
import net.ellapiz.cfdi40.comp_pago.Pagos.Pago.DoctoRelacionado;
import net.ellapiz.cfdi40.tfd11.TimbreFiscalDigital;
import net.ellapiz.cfdi40.common.CFDI40Commons;
import net.ellapiz.proveedor.vo.ProveedorVO;


@Component
public class CfdiProvManagement {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CfdiProvManagement.class);
	@Autowired
	private CfdiProvService service;
	@Autowired
	private ProveedorManagement proveedorMgmt;
	
	@Value("${base.cfdi.path}")
	private String baseCfdiPath;
	@Value("${processed.path}")
	private String processedPath;
	@Value("${unprocessed.path}")
	private String unprocessedPath;
	
	private final String CLEANING_UUID_PATTERN = "[^A-Za-z0-9]";
	
	public ComprobanteVO guardar(ComprobanteVO comprobanteVO) throws Exception{
		LOGGER.info("guardar()");
		
		List<ComprobanteVO> duplicates = findCfdiDuplicates(
				comprobanteVO.getFcFoliofiscal(),comprobanteVO.getProveedorVO().getFcRfc(), comprobanteVO.getTipoDeComprobante());
		
		if(duplicates == null || duplicates.isEmpty()) {
			ProveedorVO proveedorVO = comprobanteVO.getProveedorVO();
			proveedorVO = guardarProveedor(proveedorVO);
			comprobanteVO.setProveedorVO(proveedorVO);
			
			if(comprobanteVO.getTipoDeComprobante().compareTo(CTipoDeComprobante.P.value()) == 0) {
				return service.guardar((PagoVO) comprobanteVO);
			}else{
				return service.guardar((CfdiRecibidoVO) comprobanteVO);
			}
			
		}else {
			StringBuilder sb = new StringBuilder("Ya existe un cfdi con ");
			sb.append("folio fiscal: ");
			sb.append(comprobanteVO.getFcFoliofiscal());
			sb.append(" ");
			sb.append("RFCEmisor: ");
			sb.append(comprobanteVO.getProveedorVO().getFcRfc());
			sb.append(" ");
			LOGGER.warn(sb.toString());
			throw new AdmonCfdiProvException(sb.toString()
					,AdmonCfdiProvException.ErrorCodes.CFDI_YA_REGISTRADO.getCodigo());
		}

	}
		
	private ProveedorVO guardarProveedor(ProveedorVO proveedorVO) throws SQLException{
		ProveedorVO tempProveedor = null;
		try {
			
			tempProveedor = proveedorMgmt.buscarPorRFC(proveedorVO.getFcRfc());
			
			if(tempProveedor == null){			
				tempProveedor = proveedorMgmt.guardarProveedor(proveedorVO);
			}	
			
		}catch(DataIntegrityViolationException e) {
			if(((ConstraintViolationException)e.getCause()).getErrorCode() == 1062) {

			}else {
				throw e;
			}
			
		}catch(Exception e) {
			throw e;
		}
		
		return tempProveedor;
	}
	
	public List<ComprobanteVO> findCfdiDuplicates(String fcfolioFiscal
			, String fcRFC, String tipoDeComprobante){
		LOGGER.info("findCfdiDuplicates()");
		
		return
				service.findCfdiDuplicates(
						fcfolioFiscal, fcRFC, tipoDeComprobante);
	}
	
	public FindCfdiProvResponseTO findByDate(Date startDate, Date endDate){
		LOGGER.info("findByDate()");
		FindCfdiProvResponseTO response = new FindCfdiProvResponseTO();
		
		response.setComprobantes(service.findByDate(startDate, endDate));
		response.setPagos(service.findPagoByDate(startDate, endDate));
		
		return
				response;
	}
	
	public FindCfdiProvResponseTO findByComprobanteDate(Date startDate, Date endDate){
		LOGGER.info("findByComprobanteDate()");
		FindCfdiProvResponseTO response = new FindCfdiProvResponseTO();
		response.setComprobantes(service.findByFechaComprobante(startDate, endDate));
		response.setPagos(service.findByFechaPago(startDate, endDate));
		
		return response;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public ProcesarCfdiResponseTO procesarCfdi() throws Exception {
		LOGGER.info("procesarCfdi");
		int processedFiles  = 0;
		int totalFiles 		= 0;

		List<String> unprocessedList 	= new ArrayList<>();
		Map<String,ComprobanteVO> processedList 	= new HashMap<>();
		//Map<String,String> reasonsMap 				= new HashMap<>();
		ProcesarCfdiResponseTO response				= new ProcesarCfdiResponseTO();
		
		String[] files = buscarCfdi();
		totalFiles = files.length;
		LOGGER.info("Archivos totales: {}",totalFiles);
		for(String f:files) {
			File oldXML 				= new File(baseCfdiPath+unprocessedPath+"/"+f);
			File oldPDF 				= new File(baseCfdiPath+unprocessedPath+"/"+f.replaceAll(".xml", ".pdf"));
			ComprobanteVO comprobanteVO = null;
			try{
				LOGGER.info("originalFileName: {}",f);
				comprobanteVO = crearComprobanteVO(oldXML);
				
				if(validarPDF(oldPDF, comprobanteVO.getFcFoliofiscal())) {
					ComprobanteVO savedComprobanteVO = guardar(comprobanteVO);
					renombrarArchivos(oldXML, oldPDF, comprobanteVO);
					processedList.put(oldXML.getName(), savedComprobanteVO);
					processedFiles++;
				}else {
					/*reasonsMap.put(comprobanteVO.getFcFoliofiscal()
							, "El PDF no corresponde al XML: "+oldPDF.getName());*/
					//unprocessedList.put(oldXML.getName(), comprobanteVO);
					StringBuilder sb = new StringBuilder();
					sb.append("El archivo: ");
					sb.append(oldXML.getName());
					sb.append(", no se pudo procesar por que El PDF no corresponde al XML");
					unprocessedList.add(sb.toString());
				}
				
			}catch(AdmonCfdiProvException e) {
				LOGGER.error(e.getMessage());
				if(e.getErrorCode() == ErrorCodes.ARCHIVOS_EN_USO.getCodigo() /*||
						e.getErrorCode() == ErrorCodes.TIPO_DE_COMPROBANTE_NO_RECONOCIDO.getCodigo()*/) {
					restaurarProcesados(processedList);
					throw e;
				}
				StringBuilder sbError = new StringBuilder();
				sbError.append("El archivo: ");
				sbError.append(oldXML.getName());
				sbError.append(", no se pudo procesar por: ");
				sbError.append(e.getMessage());
				/*if(comprobanteVO != null) {
					reasonsMap.put(comprobanteVO.getFcFoliofiscal()
							, sbError.toString());
				}*/
				/*unprocessedList.put(oldXML.getName(), comprobanteVO);*/
				unprocessedList.add(sbError.toString());
			}catch(Exception e) {
				LOGGER.error(e.getMessage());
				restaurarProcesados(processedList);
				StringBuilder sb = new StringBuilder();
				sb.append("nombre de archivo: ");
				sb.append(oldXML.getName());
				sb.append("error: ");
				sb.append(e.getMessage());
				throw new Exception(sb.toString());
			}
			
		}
		response.setProcessedFiles(processedFiles);
		response.setProcessedList(processedList);
		response.setUnprocessedList(unprocessedList);
		//response.setReasonsMap(reasonsMap);
		response.setTotalFiles(totalFiles);
		
		return response;
	}

	public boolean validarPDF(File file,String uuid) {
		LOGGER.info("validarPDF()");
		try {
		 PDDocument document = PDDocument.load(file);
	      //Instantiate PDFTextStripper class
	      PDFTextStripper pdfStripper = new PDFTextStripper();
	      //Retrieving text from PDF document
	      String text;
	      text = pdfStripper.getText(document);
	      document.close();
	      String subUUID = uuid.substring(24,36);
	      boolean evalRes = false;
	      if(text.indexOf(subUUID) != -1) {
		      String extractedUUID = text.substring(text.indexOf(subUUID)-24,text.indexOf(subUUID)+12);
		      extractedUUID = extractedUUID.replaceAll(CLEANING_UUID_PATTERN, "");
		      evalRes = extractedUUID.compareTo(uuid.replaceAll(CLEANING_UUID_PATTERN, "")) == 0;
	      }
	      
	      if(!evalRes) {
	    	  LOGGER.info("Validando por nombre de archivo:");
	    	  String nombreDeArchivo  = file.getName().replace(".pdf", "").replaceAll(CLEANING_UUID_PATTERN, "");
	    	  evalRes = nombreDeArchivo.compareTo(uuid.replaceAll(CLEANING_UUID_PATTERN, "")) == 0;
	      }
	      
	      return  evalRes;
	      
		}catch(IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}catch(StringIndexOutOfBoundsException e) {
			LOGGER.error("No se localizo el UUID");
			return false;
		}
	}
	
	public String[] buscarCfdi() throws AdmonCfdiProvException {
		LOGGER.info("buscarCfdi");
		File selectedFile = new File(Paths.get(baseCfdiPath+unprocessedPath).toString());
		
		String[]files = selectedFile.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.toLowerCase().endsWith(".xml")) {
					File pdfFile = new File(dir.toString()+"/"+name.replaceAll(".xml", ".pdf"));
					return pdfFile.exists();
				}else {
					return false;
				}
			}
			
		});
		
		if(files == null || files.length == 0) {
			throw new AdmonCfdiProvException("No se ha encontrado xml, con sus respectivos pdf"
					,AdmonCfdiProvException.ErrorCodes.ARCHIVOS_NO_CORRESPONDEN.getCodigo());
		}
		
		return files;
		
	}
	
	public ComprobanteVO crearComprobanteVO(File xml) throws Exception {
		LOGGER.info("crearComprobanteVO()");
		ComprobanteVO comprobanteVO = null;
		try {
				CFDI40Commons cfdiCommons 	= new CFDI40Commons();
				Comprobante comprobante 	=  cfdiCommons.xmlToComprobante(xml.toString());

				String tipoDeComprobante 	= comprobante.getTipoDeComprobante().value();
				String folio 				= comprobante.getFolio() == null ? "" : comprobante.getFolio();
				String serie				= comprobante.getSerie() == null ? "" : comprobante.getSerie();
				Date date = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss").parse(comprobante.getFecha().toString());

				String rfcEmisor	= comprobante.getEmisor().getRfc();
				String nombre 		= comprobante.getEmisor().getNombre();
				String usoCfdi 		= comprobante.getReceptor().getUsoCFDI().value();
				String rfcReceptor 	= comprobante.getReceptor().getRfc();
				String uuid 		= extraerUUID(comprobante);

				ProveedorVO proveedorVO = new ProveedorVO();
				proveedorVO.setFcnombre(nombre);
				proveedorVO.setFcRfc(rfcEmisor);
				
				
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
					comprobanteVO.setProveedorVO(proveedorVO);
					comprobanteVO.setFcUsoCFDI(usoCfdi);
					comprobanteVO.setFcFoliofiscal(uuid);
					comprobanteVO.setFdFechaComprobante(date);
					comprobanteVO.setFcRfcReceptor(rfcReceptor);
					comprobanteVO.setTipoDeComprobante(tipoDeComprobante);
					comprobanteVO.setFcFolio(folio);
					comprobanteVO.setFcSerie(serie);
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
	
	private void renombrarArchivos(File oldXML, File oldPDF, ComprobanteVO comprobanteVO) throws AdmonCfdiProvException {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(comprobanteVO.getFdFechaComprobante());
		String añoCFDI 	= String.valueOf(cal.get(Calendar.YEAR));
		String rfcEmisor 	= comprobanteVO.getProveedorVO().getFcRfc().replaceAll("", "").toUpperCase();
		StringBuilder sbPath = new StringBuilder();
		
		sbPath.append(rfcEmisor);
		sbPath.append("/");
		sbPath.append(añoCFDI);
		crearPath(sbPath.toString());
		
		StringBuilder sbNombre		= cacularNombreDeArchivo(comprobanteVO.getTipoDeComprobante()
				, comprobanteVO.getFcUsoCFDI(), comprobanteVO.getProveedorVO().getFcRfc()
				, comprobanteVO.getFcFolio(), comprobanteVO.getFcFoliofiscal());

		String xmlName 		= sbNombre.toString()+".xml";
		String pdfName 		= sbNombre.toString()+".pdf";
		
		LOGGER.info("pathDeRenombrado: {}/{}",sbPath,sbNombre);
		
		File newXML = new File(baseCfdiPath+processedPath+"/"+sbPath+"/"+xmlName);
		File newPDF = new File(baseCfdiPath+processedPath+"/"+sbPath+"/"+pdfName);	
		File copyOldXML = new File(baseCfdiPath+unprocessedPath+"/"+oldXML.getName());
		File copyOldPDF = new File(baseCfdiPath+unprocessedPath+"/"+oldPDF.getName());
		
		if(!oldXML.renameTo(newXML) || !oldPDF.renameTo(newPDF)) {
			LOGGER.debug("No se pudieron renombrar archivos, posiblemente este abiertos o en uso");
			newPDF.renameTo(copyOldPDF);
			newXML.renameTo(copyOldXML);
			throw new AdmonCfdiProvException ("Por favor válide que los archivos no este abiertos o en uso"
					, AdmonCfdiProvException.ErrorCodes.ARCHIVOS_EN_USO.getCodigo());
		}
	}
	
	private void restaurarProcesados(Map<String,ComprobanteVO> processedList) {
		LOGGER.info("restaurarProcesados()");
		
		processedList.forEach((k,v) -> {
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(v.getFdFechaComprobante());
			String añoCFDI	 	= String.valueOf(cal.get(Calendar.YEAR)); 
			//StringBuilder sbDirectory 	= crearPath(cal);
			String rfcEmisor 	= v.getProveedorVO().getFcRfc().replaceAll("", "").toUpperCase();;
			StringBuilder sbPath = new StringBuilder();
			
			sbPath.append(rfcEmisor);
			sbPath.append("/");
			sbPath.append(añoCFDI);
			crearPath(sbPath.toString());
			
			StringBuilder sbNombre		= cacularNombreDeArchivo(v.getTipoDeComprobante()
					, v.getFcUsoCFDI(), v.getProveedorVO().getFcRfc()
					, v.getFcFolio(), v.getFcFoliofiscal());
			String xmlName 		= sbNombre.toString()+".xml";
			String pdfName 		= sbNombre.toString()+".pdf";
			
			File xml = new File(baseCfdiPath+processedPath+"/"+sbPath+"/"+xmlName);
			File pdf = new File(baseCfdiPath+processedPath+"/"+sbPath+"/"+pdfName);
			
			if (!(xml.renameTo(new File(baseCfdiPath+unprocessedPath+"/"+xmlName)) &&
			pdf.renameTo(new File(baseCfdiPath+unprocessedPath+"/"+pdfName)))) {
				LOGGER.error("No fue posible restaurar los siguientes archivos");
				LOGGER.error("xml: {}",xml.getPath());
				LOGGER.error("pdf: {}",pdf.getPath());
			}
		});
	}
	
	private void crearPath(String path) {
//		StringBuilder sbDirectory = new StringBuilder();
//		sbDirectory.append(cal.get(Calendar.YEAR));
//		sbDirectory.append("/");
//		sbDirectory.append(String.format("%02d", cal.get(Calendar.MONTH)+1));
//		sbDirectory.append("/");
//		sbDirectory.append(String.format("%02d",cal.get(Calendar.DATE)));

		File dir = new File(baseCfdiPath+processedPath+"/"+path);

		if(!dir.exists()) {
			dir.mkdirs();
		}

	}
	
	private StringBuilder cacularNombreDeArchivo(String tipoDeComprobante,
			String usoCfdi, String rfcEmisor, String folio, String uuid) {
		
		StringBuilder sb = new StringBuilder();
		usoCfdi = tipoDeComprobante.toUpperCase().compareTo("P")== 0 ? "CDP" : usoCfdi;	
		sb.append(usoCfdi);
		sb.append("_");
		sb.append(rfcEmisor.toUpperCase());
		sb.append("_");
		folio = folio.length() > 0 ? folio : uuid;
		sb.append(folio);
		
		return sb;
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
		Double total 		= cfdi.getTotal().doubleValue();
		cfdiRecibidoVO.setFdTotal(total);
		cfdiRecibidoVO.setFcFormaDePago(formaDePago);
		cfdiRecibidoVO.setFcMetodoDePago(metodoDePago);
		List<ItemVO> items = new ArrayList<>();
		
		Conceptos conceptos = cfdi.getConceptos();
		
		for(Concepto concepto : conceptos.getConcepto()) {
			ItemVO item = new ItemVO(); 
			item.setFcDescripcion(concepto.getDescripcion());
			item.setFdCantidad(concepto.getCantidad().doubleValue());
			double fdDescuento 			= concepto.getDescuento() == null ? 0.0 : concepto.getDescuento().doubleValue();
			double descuentoUnitario 	= fdDescuento / item.getFdCantidad();	 
			double precioUnitario 		= concepto.getValorUnitario().doubleValue() - descuentoUnitario;
			item.setFdPrecioUnitario(precioUnitario);
			item.setFcClaveProducto(concepto.getNoIdentificacion());
			item.setCfdiRecibidoVO(cfdiRecibidoVO);
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
					pagoItemVO.setFdFechaPago(new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss").parse(pago.getFechaPago().toString()));
					pagoItemVO.setFdMonto(pago.getMonto().doubleValue());
					
					for(DoctoRelacionado doctoRelacionado : pago.getDoctoRelacionado()) {
						pagoItemVO.setFcFolio(doctoRelacionado.getFolio());
						pagoItemVO.setFcSerie(doctoRelacionado.getSerie());
						pagoItemVO.setFcUuid(doctoRelacionado.getIdDocumento());
					}
					
					pagoItemVO.setPagoVO(pagoVO);
					pagoList.add(pagoItemVO);
					pagoVO.setFdTotal(cfdi.getTotal().doubleValue());
				}
				
			}catch(ClassCastException cce) {
				LOGGER.error("El complemento no es del tipo pago");
			} catch (ParseException e) {
				LOGGER.error(e.getMessage());
			}
		}
		
		pagoVO.setPagos(pagoList);
		
		return pagoVO;
	}
	
	public FindCfdiProvResponseTO findByProveedor(String proveedorRFC, String year){
		LOGGER.info("findByProveedor()");
		FindCfdiProvResponseTO response = new FindCfdiProvResponseTO();
		response.setComprobantes(service.findByProveedor(proveedorRFC, year));
		response.setPagos(service.findPagoByProveedor(proveedorRFC, year));
		
		return response;
	}
}
