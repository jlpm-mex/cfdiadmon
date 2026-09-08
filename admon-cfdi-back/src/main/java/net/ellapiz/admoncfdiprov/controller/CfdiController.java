package net.ellapiz.admoncfdiprov.controller;


import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.ellapiz.admoncfdiprov.dto.PendingCfdi;
import net.ellapiz.admoncfdiprov.exception.ErrorResponse;
import net.ellapiz.admoncfdiprov.management.CfdiManagement;
import net.ellapiz.admoncfdiprov.to.FindCfdiProvResponseTO;
import net.ellapiz.admoncfdiprov.to.ProcesarCfdiResponseTO;
import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;

@RequestMapping("/api/cfdis")
@RestController
public class CfdiController {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final CfdiManagement management;
	
	public CfdiController(CfdiManagement management){
		this.management = management;
	}
	
	/**
	 * Persists a CFDI comprobante to the database.
	 *
	 * This method performs the following operations:
	 * <ul>
	 *   <li>Validates the comprobante data</li>
	 *   <li>Saves the entity to the database</li>
	 *   <li>Returns the entity with the generated ID</li>
	 * </ul>
	 *
	 * @param comprobanteVO The comprobante to save (cannot be {@code null})
	 * @return {@link ResponseEntity} containing the saved comprobante with HTTP 201 (Created) status
	 * @see ComprobanteVO
	 * @see org.springframework.http.HttpStatus#CREATED
	 */
	@PostMapping
	public ResponseEntity<ComprobanteVO> saveCFDI(@Validated @RequestBody
			ComprobanteVO comprobanteVO) {
		LOGGER.info("saveCFDI() - Procesando comprobante: {}",
				comprobanteVO.getFcFolio());

		ComprobanteVO guardado = management.guardar(comprobanteVO);

		return ResponseEntity.status(HttpStatus.CREATED).body(guardado);

	}

	/**
	 * Searches for CFDI comprobantes by database registration date range.
	 * 
	 * <p>This method queries the database and returns all entities whose registration
	 * date falls between the specified startDate and endDate (inclusive).</p>
	 * 
	 * @param startDate {@link LocalDate} filter starting date (cannot be {@code null})
	 * @param endDate {@link LocalDate} filter finishing date (cannot be {@code null})
	 * @return {@link ResponseEntity} containing the search results with HTTP 200 (OK) status
	 * @see FindCfdiProvResponseTO
	 * @see org.springframework.http.HttpStatus#OK
	 */
	@CrossOrigin(origins = "${upload.files.cross.origin}")
	@GetMapping("/by-registration-date")
	public ResponseEntity<FindCfdiProvResponseTO> findByRegistryDate(
			@RequestParam LocalDate startDate, @RequestParam LocalDate endDate,                                                                                                                                                                                                  
            @RequestParam(defaultValue = "0") int page,                                                                                                                                                                                       
            @RequestParam(defaultValue = "10") int size) { 
		LOGGER.info("findByRegistryDate(), startDate = {}, endDate = {}, page = {}, size = {}",
				startDate, endDate, page, size);	
		
		Pageable pageable = PageRequest.of(page, size);
		FindCfdiProvResponseTO response = management.findByDate(startDate, endDate, pageable);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	/**
	 * Searches for CFDI comprobantes by comprobante date range.
	 * 
	 * <p>This method queries the database and returns all entities whose comprobante
	 * date falls between the specified startDate and endDate (inclusive).</p>
	 * 
	 * @param startDate {@link LocalDate} filter starting date (cannot be {@code null})
	 * @param endDate {@link LocalDate} filter finishing date (cannot be {@code null})
	 * @return {@link ResponseEntity} containing the search results with HTTP 200 (OK) status
	 * @see FindCfdiProvResponseTO
	 * @see org.springframework.http.HttpStatus#OK
	 */
    @CrossOrigin(origins = "${upload.files.cross.origin}")                                                                                                                                                                                    
    @GetMapping("/by-comprobante-date")                                                                                                                                                                                                       
    public ResponseEntity<FindCfdiProvResponseTO> findByComprobanteDate(                                                                                                                                                                      
            @RequestParam LocalDate startDate,                                                                                                                                                                                                
            @RequestParam LocalDate endDate,                                                                                                                                                                                                  
            @RequestParam(defaultValue = "0") int page,                                                                                                                                                                                       
            @RequestParam(defaultValue = "10") int size) {                                                                                                                                                                                    
                                                                                                                                                                                                                                              
        LOGGER.info("findByComprobanteDate(), startDate = {}, endDate = {}, page = {}, size = {}", 
				startDate, endDate , page, size);	
		
		Pageable pageable = PageRequest.of(page, size);   
		FindCfdiProvResponseTO response = management.findByComprobanteDate(
					startDate, endDate, pageable);
		
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	
	/**
	 * Searches for CFDI comprobantes by supplier's RFC and emission year.
	 * 
	 * <p>This method queries the database and returns all entities whose supplier's
	 * RFC matches the given value and whose emission year matches the given year.</p>
	 * 
	 * @param year the comprobante emission year in format "yyyy" (optional).
	 * @param rfc Supplier's Registro Federal de Contribuyentes        
	 * @apiNote If the year parameter is not present, the current year will be used as default
	 * @return {@link ResponseEntity} containing the search results with HTTP 200 (OK) status
	 * @see FindCfdiProvResponseTO
	 * @see org.springframework.http.HttpStatus#OK
	 */
	@CrossOrigin(origins = "${upload.files.cross.origin}")
	@GetMapping("/by-rfc")
	public ResponseEntity<FindCfdiProvResponseTO> findByProveedor(@RequestParam String rfc,
			Integer year,                                                                                                                                                                                                  
            @RequestParam(defaultValue = "0") int page,                                                                                                                                                                                       
            @RequestParam(defaultValue = "10") int size) {  
		LOGGER.info("findByProveedor() rfc = {}, year = {}, page = {}, size = {}",
				rfc, year, page, size);
		
		Pageable pageable = PageRequest.of(page, size);
		FindCfdiProvResponseTO response = management.findByProveedor(
					rfc, year, pageable);
		return
				ResponseEntity.status(HttpStatus.OK).body(response);

	}
	
	/**
	 * Starts the process of validation and registration of CFDI comprobantes.
	 * 
	 * <p>This method performs the following operations:</p>
	 * <ul>
	 *   <li>Validates that for every XML file there is a corresponding PDF file</li>
	 *   <li>Creates a {@link ComprobanteVO} entity from the XML data</li>
	 *   <li>Persists the entity to the database</li>
	 *   <li>Returns a summary of processed and unprocessed files</li>
	 * </ul>
	 * 
	 * @return {@link ResponseEntity} containing a {@link ProcesarCfdiResponseTO}
	 *         with lists of processed and unprocessed files, and HTTP 200 (OK) status
	 * @see ProcesarCfdiResponseTO
	 * @see org.springframework.http.HttpStatus#OK
	 */
	@CrossOrigin(origins = "${upload.files.cross.origin}")
	@PostMapping("/start-process")
	public ResponseEntity<ProcesarCfdiResponseTO> procesarCfdi() {
		LOGGER.info("procesarCfdi()");

		ProcesarCfdiResponseTO response = management.procesarCfdi();
		
		return ResponseEntity.status(HttpStatus.OK).body(response);
		
	}
	
	/**
	 * Uploads files to the server.
	 * 
	 * <p>This method performs the following operations:</p>
	 * <ul>
	 *   <li>Uploads files to the server</li>
	 *   <li>Renames the files</li>
	 *   <li>Places them in the specified location</li>
	 * </ul>
	 * 
	 * @param files the files to be uploaded (cannot be {@code null})
	 * @return {@link ResponseEntity} containing a list of the renamed files,
	 *         with HTTP 200 (OK) status
	 * @see org.springframework.http.HttpStatus#OK
	 */
	@CrossOrigin(origins = "${upload.files.cross.origin}")
	@PostMapping("/files") 
    public ResponseEntity<Object> uploadMultipartFile(
    		@RequestParam MultipartFile[] files) {
		LOGGER.info("uploadMultipartFile(), files = {}", files.length);
		
	    if (files == null || files.length == 0) {
	        ErrorResponse error = new ErrorResponse(
	            "BAD_REQUEST",
	            "No se enviaron archivos para subir",
	            "/api/cfdis/files"
	        );
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	    }

		List<String> uploadedFiles = management.uploadFile(files);
		
		return ResponseEntity.status(HttpStatus.OK).body(uploadedFiles);
    }
	
	@CrossOrigin(origins = "${upload.files.cross.origin}")
	@GetMapping("/unprocessed-docs")
	public ResponseEntity<ProcesarCfdiResponseTO> unprocessedDocs(){
		LOGGER.info("procesarCfdi()");
		List<PendingCfdi> unprocessedDocList = Collections.emptyList();
		ProcesarCfdiResponseTO response = new ProcesarCfdiResponseTO();
		try {
			unprocessedDocList = management.unprocessedFiles();
			
			response.setUnprocessedList(unprocessedDocList);
		}catch(Exception e) {
			LOGGER.error(e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@CrossOrigin(origins = "${upload.files.cross.origin}")
	@GetMapping("/{documentId}/{tipoDeDocumento}/xml")
	public ResponseEntity<Resource> getXmlFile(@PathVariable String documentId, @PathVariable String tipoDeDocumento){
		LOGGER.info("documentId = {}, tipoDeDocumento = {}",documentId, tipoDeDocumento);
		try {
            
			
			Path filePath =  management.getFullProcessedPath(documentId, tipoDeDocumento);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            }
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
	}
	
	@GetMapping("/{documentId}/{tipoDeDocumento}/pdf")
	public ResponseEntity<Resource> getPdfFile(@PathVariable String documentId, @PathVariable String tipoDeDocumento) {
		try	{
		    Resource pdfResource = management.getOrCreatePdf(documentId, tipoDeDocumento);
		    
		    return ResponseEntity.ok()
		        .contentType(MediaType.APPLICATION_PDF)
		        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename="+pdfResource.getFilename())
		        .body(pdfResource);
		} catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
	}
	
}
