package net.ellapiz.admoncfdiprov.service.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.ellapiz.admoncfdiprov.dto.PendingCfdi;
import net.ellapiz.admoncfdiprov.exception.AdmonCfdiProvException;
import net.ellapiz.admoncfdiprov.exception.AdmonCfdiProvException.ErrorCodes;
import net.ellapiz.admoncfdiprov.service.CfdiFileService;
import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;

@Service
public class CfdFileServiceImpl implements CfdiFileService {
	
	@Value("${base.cfdi.path}")
	private String baseCfdiPath;
	@Value("${processed.path}")
	private String processedPath;
	@Value("${unprocessed.path}")
	private String unprocessedPath;
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	/**
	 * Retrieves all unprocessed XML files from the configured unprocessed directory.
	 * 
	 * <p>This method scans the loaded unprocessed path and collects all files
	 * with the {@code .xml} extension.</p>
	 * 
	 * @return An array of strings containing the names of all found XML files
	 * @throws AdmonCfdiProvException if the directory cannot be read or is inaccessible
	 */
	public List<PendingCfdi> getPendingDocuments() throws AdmonCfdiProvException {
		LOGGER.info("getPendingDocuments");
		LOGGER.info("path NoProcesados: {}",baseCfdiPath+unprocessedPath);
		File selectedFile = new File(Paths.get(baseCfdiPath+unprocessedPath).toString());
		
		List<PendingCfdi> pendingDocumentList =
			Arrays.stream(selectedFile.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if(name.toLowerCase().endsWith(".xml")) {
						File pdfFile = new File(dir.toString()+"/"+name.replaceAll(".xml", ".pdf"));
						return pdfFile.exists();
					}else {
						return false;
					}
				}
				
			})).map(name -> {
				File xmlFile = new File(baseCfdiPath+unprocessedPath+"/"+name);
				File pdfFile = new File(baseCfdiPath+unprocessedPath+"/"+name.replaceAll(".xml", ".pdf"));
				return new PendingCfdi(xmlFile, pdfFile);
			}).collect(Collectors.toList());
		
		if(pendingDocumentList == null || pendingDocumentList.isEmpty()) {
			LOGGER.warn("No se ha encontrado xml, con sus respectivos pdf");
			throw new AdmonCfdiProvException("No se ha encontrado xml, con sus respectivos pdf"
					,AdmonCfdiProvException.ErrorCodes.ARCHIVOS_NO_CORRESPONDEN.getCodigo());
		}
		
		return pendingDocumentList;
		
	}
	
	/**
	 * 
	 * @param comprobanteVO
	 * @return
	 */
	public String pathGenerator(ComprobanteVO comprobanteVO) {
		LOGGER.info("pathAndNameGenerator()");
		
		String añoCFDI 		= String.valueOf(comprobanteVO.getFdFechaComprobante().getYear());
		String rfcEmisor 	= comprobanteVO.getEmisorVO().getFcRfc().replaceAll("", "").toUpperCase();
		StringBuilder sbPath = new StringBuilder();
		
		sbPath.append(rfcEmisor);
		sbPath.append("/");
		sbPath.append(añoCFDI);
		
		return sbPath.toString();

	}
	
	/**
	 * 
	 */
	public void renombrarArchivos(PendingCfdi pendingCfdi) 
			throws AdmonCfdiProvException {
		
		String sbPath = pathGenerator(pendingCfdi.comprobanteVO());
		String sbNombre = calcularNombreDeArchivo(pendingCfdi.comprobanteVO());
		
		crearPath(sbPath.toString());

		String xmlName 		= sbNombre+".xml";
		String pdfName 		= sbNombre+".pdf";
		
		LOGGER.info("pathDeRenombrado: {}/{}",baseCfdiPath+processedPath+"/"+sbPath,sbNombre);
		
		File newXML = new File(baseCfdiPath+processedPath+"/"+sbPath+"/"+xmlName);
		File newPDF = new File(baseCfdiPath+processedPath+"/"+sbPath+"/"+pdfName);	
		boolean wasXMLMoved = false;
		
		if(newXML.exists() || newPDF.exists()) {
			
			throw new AdmonCfdiProvException ("Por favor verifique, ya existen documentos con ese nombre "
					, AdmonCfdiProvException.ErrorCodes.ARCHIVOS_EN_USO.getCodigo());
		}
		
		try {
			FileUtils.moveFile(pendingCfdi.xml(), newXML);
			wasXMLMoved = true;
			FileUtils.moveFile(pendingCfdi.pdf(), newPDF);
			
		}catch(IOException e) {
			LOGGER.debug(e.getMessage());
			if(wasXMLMoved)
				try {
					LOGGER.info("el path original es: {}{}", pendingCfdi.xml().getPath(), pendingCfdi.xml().getName());
					FileUtils.moveFile(newXML,pendingCfdi.xml());
				}catch(IOException se) {
					LOGGER.debug(se.getMessage());
					LOGGER.info("El archivo :{}, no pudo ser regresado a su ubicación,"
							+ " por favor regreselo manualmente. su ubicación actual es:",
							newXML.getName(),
							newXML.getPath());
				}
			throw new AdmonCfdiProvException ("Por favor válide que los archivos no este abiertos o en uso"
					, AdmonCfdiProvException.ErrorCodes.ARCHIVOS_EN_USO.getCodigo());
		}
		
	}
	
	/**
	 * 
	 */
	public void restaurarProcesados(List<PendingCfdi> processedFileLst) {
		LOGGER.info("restaurarProcesados()");
		
		processedFileLst.forEach(pendingCfdi -> {
			
			ComprobanteVO comprobanteVO = pendingCfdi.comprobanteVO();
			

			String añoCFDI	 	= String.valueOf(comprobanteVO.getFdFechaComprobante().getYear()); 
			String rfcEmisor 	= comprobanteVO.getEmisorVO().getFcRfc().replaceAll("", "").toUpperCase();;
			StringBuilder sbPath = new StringBuilder();
			
			sbPath.append(rfcEmisor);
			sbPath.append("/");
			sbPath.append(añoCFDI);
			
			String newName	= calcularNombreDeArchivo(pendingCfdi.comprobanteVO());
			String xmlName 	= newName.toString()+".xml";
			String pdfName 	= newName.toString()+".pdf";
			
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
	
	/**
	 * 
	 */
	public void crearPath(String path) {
		File dir = new File(baseCfdiPath+processedPath+"/"+path);

		if(!dir.exists()) {
			dir.mkdirs();
		}

	}
	
	/**
	 * 
	 */
	public String calcularNombreDeArchivo(ComprobanteVO comprobanteVO) {
		LOGGER.info("calcularNombre()");
		LOGGER.info("comprobanteVO = {}", comprobanteVO.getFcFolio());
		
		String usoCfdi = comprobanteVO.getTipoDeComprobante()
				.toUpperCase().compareTo("P") == 0 ? "CDP" : comprobanteVO.getFcUsoCFDI();	
		String folio = comprobanteVO.getFcFolio().length() > 0 ? comprobanteVO.getFcFolio() : comprobanteVO.getFcFoliofiscal();
		String newFileName = usoCfdi +  "_" +
				comprobanteVO.getEmisorVO().getFcRfc().toUpperCase() + "_" + folio;
		
		return newFileName;
	}
	
	@Override
	public List<String> createFiles(MultipartFile[] files) {
		try {
			List<String> uploadedFiles = new ArrayList<>();
			
	        for (MultipartFile multipartFile : files) {
	        	
	        	if (multipartFile == null || multipartFile.isEmpty()) {
	                throw new AdmonCfdiProvException("Los archivos no pueden estar vacios",
	                		ErrorCodes.ARCHIVO_VACIO.getCodigo());
	            }
	        	
	            File tempFile = new File(multipartFile.getOriginalFilename());
	            String originalFileName = tempFile.getName();
	            File file = new File(baseCfdiPath+"/"+unprocessedPath+"/"+originalFileName);
	            multipartFile.transferTo(file);
	            uploadedFiles.add(originalFileName);
	        }
	        
	        return uploadedFiles;
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return null;
	}
	
	public Path getFullProcessedPath(ComprobanteVO comprobanteVO) {
		String spath = pathGenerator(comprobanteVO);
		String name = calcularNombreDeArchivo(comprobanteVO);
		
		Path path = Paths.get(baseCfdiPath+processedPath+"/"+spath+"/"+name);
		
		return path;
		
	}

}
