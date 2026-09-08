package net.ellapiz.admoncfdiprov.management;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.ellapiz.admoncfdiprov.dto.PendingCfdi;
import net.ellapiz.admoncfdiprov.exception.AdmonCfdiProvException;
import net.ellapiz.admoncfdiprov.exception.AdmonCfdiProvException.ErrorCodes;
import net.ellapiz.admoncfdiprov.exception.PersistenceException;
import net.ellapiz.admoncfdiprov.parser.CfdiParser;
import net.ellapiz.admoncfdiprov.service.CfdiFileService;
import net.ellapiz.admoncfdiprov.service.CfdiProvService;
import net.ellapiz.admoncfdiprov.service.PdfValidatorService;
import net.ellapiz.admoncfdiprov.to.FindCfdiProvResponseTO;
import net.ellapiz.admoncfdiprov.to.PageResult;
import net.ellapiz.admoncfdiprov.to.ProcesarCfdiResponseTO;
import net.ellapiz.admoncfdiprov.util.CfdiPdfGenerator;
import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;
import net.ellapiz.admoncfdiprov.vo.EmisorVO;


@Component
public class CfdiManagement {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CfdiManagement.class);
	
	private final CfdiProvService cfdiProvservice;
	private final ProveedorManagement proveedorMgmt;
	private final PdfValidatorService pdfValidatorService;
	private final CfdiParser cfdiParser;
	private final CfdiFileService cfdiFileService;
	private final List<ComprobanteSaverStrategy> strategies;

	public CfdiManagement(CfdiProvService cfdiProvService, ProveedorManagement proveedorMgmt,
			PdfValidatorService pdfValidatorService, CfdiParser cfdiParser, CfdiFileService cfdiFileService,
			List<ComprobanteSaverStrategy> strategies) {
		this.cfdiProvservice = cfdiProvService;
		this.proveedorMgmt = proveedorMgmt;
		this.pdfValidatorService = pdfValidatorService;
		this.cfdiParser = cfdiParser;
		this.cfdiFileService = cfdiFileService;
		this.strategies = strategies;
	}

	@Transactional(rollbackFor = Exception.class)
	public ComprobanteVO guardar(ComprobanteVO comprobanteVO){
		LOGGER.info("guardar()");
		
		List<ComprobanteVO> duplicates = findCfdiDuplicates(
				comprobanteVO.getFcFoliofiscal(),
				comprobanteVO.getEmisorVO().getFcRfc(),
				comprobanteVO.getTipoDeComprobante());
		
		if(duplicates == null || duplicates.isEmpty()) {
			EmisorVO emisorVO = comprobanteVO.getEmisorVO();
			emisorVO = guardarProveedor(emisorVO);
			comprobanteVO.setEmisorVO(emisorVO);
			
			ComprobanteSaverStrategy strategy = strategies.stream()                                                                                                                                                                      
                    .filter(s -> s.supports(comprobanteVO.getTipoDeComprobante()))                                                                                                                                                            
                    .findFirst()                                                                                                                                                                                                              
                    .orElseThrow(() -> new AdmonCfdiProvException(                                                                                                                                                                            
                        "No existe una estrategia para el tipo: " + comprobanteVO.getTipoDeComprobante(),                                                                                                                                     
                        ErrorCodes.ERROR_DE_PROCESAMIENTO.getCodigo()                                                                                                                                                                         
                    ));                                                                                                                                                                                                                       
                                                                                                                                                                                                                                                         
            return strategy.guardar(comprobanteVO);
			
		}else {
			StringBuilder sb = new StringBuilder("Ya existe un cfdi con ");
			sb.append("folio fiscal: ");
			sb.append(comprobanteVO.getFcFoliofiscal());
			sb.append(" ");
			sb.append("RFCEmisor: ");
			sb.append(comprobanteVO.getEmisorVO().getFcRfc());
			sb.append(" ");
			LOGGER.warn(sb.toString());
			throw new AdmonCfdiProvException(sb.toString()
					,ErrorCodes.CFDI_YA_REGISTRADO.getCodigo());
		}

	}
	
	/**
	 * Persist a supplier
	 * @param proveedorVO {@link ProveedorVO}
	 * @return The saved provider {@link ProveedorVO}
	 * @throws AdmonCfdiProvException if provider already exists
	 * @throws PersistenceException if database error occurs
	 */
	private EmisorVO guardarProveedor(EmisorVO proveedorVO){

		try {
			
			EmisorVO tempProveedor = proveedorMgmt.buscarPorRFC(proveedorVO.getFcRfc());
			
			if(tempProveedor != null) 
				return tempProveedor;
			
			tempProveedor = proveedorMgmt.guardarProveedor(proveedorVO);
			
			return tempProveedor;
			
		}catch(SQLException e) {
			LOGGER.error("Error SQL al guardar proveedor con RFC: {}",
					proveedorVO.getFcRfc());
	        throw new PersistenceException(
	                "Error al guardar proveedor con RFC: " + proveedorVO.getFcRfc(), 
	                e
	            );
		}
	}
	
	public List<ComprobanteVO> findCfdiDuplicates(String fcfolioFiscal
			, String fcRFC, String tipoDeComprobante){
		LOGGER.info("findCfdiDuplicates()");
		
		return
				cfdiProvservice.findByFolioRfcAndType(
						fcfolioFiscal, fcRFC, tipoDeComprobante);
	}
	
	public FindCfdiProvResponseTO findByDate(LocalDate startDate, LocalDate endDate, Pageable pageable){
		LOGGER.info("findByDate()");
		
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
		FindCfdiProvResponseTO response = new FindCfdiProvResponseTO();
		
		response.setComprobantes(toPageResult(cfdiProvservice.findByDate(startDateTime, endDateTime, pageable)));
		response.setPagos(toPageResult(cfdiProvservice.findPagoByDate(startDateTime, endDateTime, pageable)));
		
		return
				response;
	}
	
	public FindCfdiProvResponseTO findByComprobanteDate(LocalDate startDate, LocalDate endDate, Pageable pageable){
		LOGGER.info("findByComprobanteDate()");
		FindCfdiProvResponseTO response = new FindCfdiProvResponseTO();
		response.setComprobantes(toPageResult(cfdiProvservice.findByFechaComprobante(startDate, endDate, pageable)));
		response.setPagos(toPageResult(cfdiProvservice.findByFechaPago(startDate, endDate, pageable)));
		
		return response;
	}
	
	public ProcesarCfdiResponseTO procesarCfdi() {
		LOGGER.info("procesarCfdi");
		int totalProcessedFiles  = 0;
		int totalFiles 		= 0;

		List<PendingCfdi> unprocessedList 	= new ArrayList<>();
		List<PendingCfdi> processedList = new ArrayList	<>();
		ProcesarCfdiResponseTO response				= new ProcesarCfdiResponseTO();
		
		List<PendingCfdi> pendingDocumentList = cfdiFileService.getPendingDocuments();
		totalFiles = pendingDocumentList.size();
		LOGGER.info("Archivos totales: {}",totalFiles);
		//1) Se itera sobre los archivos retornados
		//2) Se transforma de xml a ComprobanteVO
		//3) Se valida el uuid del xml vs el del pdf para ver que hagan match
		//	3.1)Si se valida:
		//		I)  Se persiste comprobanteVO en la bd
		//		II) Se renombran los archivos
		//		III)Se agrega a la lista de procesados
		//	3.2)No se valida:
		//		I) Se crea el mensaje, concatenando el nombre del archivo que no se valido
		//		II) Marcar como failure y agregar el mensaje de error
		for(PendingCfdi pendingCfdi:pendingDocumentList) {

			ComprobanteVO comprobanteVO = null;
			try{
				LOGGER.info("originalFileName: {}",pendingCfdi.getXmlName());
				comprobanteVO = cfdiParser.crearComprobanteVO(pendingCfdi.xml());
				
				if(pdfValidatorService.validarPDF(pendingCfdi.pdf(),
						comprobanteVO.getFcFoliofiscal())) {
					
					ComprobanteVO savedComprobanteVO = guardar(comprobanteVO);
					PendingCfdi pedingCfdiSuccess = pendingCfdi.success(savedComprobanteVO);
					processedList.add(pedingCfdiSuccess);
					cfdiFileService.renombrarArchivos(pedingCfdiSuccess);
					totalProcessedFiles++;
					
				} else {
					
					String errorMsg = "El archivo: " + pendingCfdi.getXmlName() +
					", no se pudo procesar por que El PDF no corresponde al XML";
					pendingCfdi = pendingCfdi.failure(errorMsg);
					unprocessedList.add(pendingCfdi);
					
				}
				
			}catch(Exception e) {
				
				String errorMsg = "nombre de archivo: " + pendingCfdi.getXmlName()+
						" error: " + e.getMessage();
				LOGGER.error(errorMsg);
				unprocessedList.add(pendingCfdi.failure(e.getMessage()));
				
			}
			
		}
		
		response.setTotalFiles(totalFiles);
		response.setTotalProcessedFiles(totalProcessedFiles);
		response.setProcessedList(processedList);
		response.setUnprocessedList(unprocessedList);
		
		
		return response;
	}
	
	public FindCfdiProvResponseTO findByProveedor(String proveedorRFC, Integer year, Pageable pageable){
		LOGGER.info("findByProveedor()");
		
	    if (year == null) {
	        year = LocalDate.now().getYear();
	    }
	    
		FindCfdiProvResponseTO response = new FindCfdiProvResponseTO();
		response.setComprobantes(toPageResult(cfdiProvservice.findByProveedor(proveedorRFC, year, pageable)));
		response.setPagos(toPageResult(cfdiProvservice.findPagoByProveedor(proveedorRFC, year, pageable)));
		
		return response;
	}

	public List<String> uploadFile(MultipartFile[] files) {
		LOGGER.info("uploadFile(), processing = {} files", files.length);
		
		return 
				cfdiFileService.createFiles(files);
	}
	
	public List<PendingCfdi> unprocessedFiles() throws AdmonCfdiProvException{
		LOGGER.info("unprocessedFiles()");
		return
				cfdiFileService.getPendingDocuments();
	}
	
    private <T> PageResult<T> toPageResult(Page<T> springPage) {                                                                                                                                                                              
        return new PageResult<>(
			springPage.getContent(),
			springPage.getNumber(),
			springPage.getSize(),
			springPage.getTotalElements(),
			springPage.getTotalPages()                                                                                                                                                                                                        
        );                                                                                                                                                                                                                                    
    }  
	
    public Path getFullProcessedPath(String documentId, String tipoDeDocumento) {
    	LOGGER.info("documentId = {}", documentId);
    	List<ComprobanteVO> comprobantes = 
    			cfdiProvservice.findByFolioRfcAndType(documentId, "", tipoDeDocumento);
    	
    	Path path =	cfdiFileService.getFullProcessedPath(comprobantes.get(0));
    	
    	return path.resolveSibling(path.getFileName()+".xml");
    }

	public Resource getOrCreatePdf(String documentId, String tipoDeDocumento) 
			throws MalformedURLException {
    	LOGGER.info("documentId = {}", documentId);
    	List<ComprobanteVO> comprobantes = 
    			cfdiProvservice.findByFolioRfcAndType(documentId, "", tipoDeDocumento);
    	
     	Path path =	cfdiFileService.getFullProcessedPath(comprobantes.get(0));
     	Path pathPDF = path.resolveSibling(path.getFileName()+".pdf");
    	Resource resource = new UrlResource(pathPDF.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        }
        
        try {
	        File xmlFile = new File(path.resolveSibling(path.getFileName()+".xml").toUri());
	        ComprobanteVO comprobanteVO = cfdiParser.crearComprobanteVO(xmlFile);
	        CfdiPdfGenerator pdfGen = new CfdiPdfGenerator();
	        byte [] bytearr = pdfGen.generatePdf(comprobanteVO);
	        Files.write(pathPDF, bytearr); 
	        
        }catch(Exception e) {
        	LOGGER.error(e.getMessage());
        }
        
        return resource = new UrlResource(pathPDF.toUri());
	}
    
}
