package net.ellapiz.admoncfdiprov.service;

import java.nio.file.Path;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import net.ellapiz.admoncfdiprov.dto.PendingCfdi;
import net.ellapiz.admoncfdiprov.exception.AdmonCfdiProvException;
import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;

/**
 * Servicio para la gestión de archivos CFDI (XML y PDF) en el sistema de archivos.
 */
public interface CfdiFileService {
	
	/**
	 * 
	 * @param oldXML
	 * @param oldPDF
	 * @param comprobanteVO
	 */
	public void renombrarArchivos(PendingCfdi pendingCfdi);
	
	/**
	 * Restores the failed files to unprocessed directory.
	 * 
	 * <p>This method restores the files that couldn't be processed, to the unprocessed directory.</p>
	 * 
	 * @param processedList
	 */
	public void restaurarProcesados(List<PendingCfdi> processedFileLst);
	
	public List<PendingCfdi> getPendingDocuments() throws AdmonCfdiProvException;
	
	public List<String> createFiles(MultipartFile[] files);
	
	public Path getFullProcessedPath(ComprobanteVO comprobanteVO);
}
