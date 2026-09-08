package net.ellapiz.admoncfdiprov.service;

import java.io.File;

/**
 * Servicio para la validación de archivos PDF contra sus correspondientes XML.
 */
public interface PdfValidatorService {
	
	/**
	 * Validates that the UUID embedded in the PDF file matches the UUID from the XML file.
	 * 
	 * <p>This method extracts the UUID from the PDF content and compares it
	 * with the provided XML UUID to ensure they correspond to the same CFDI.</p>
	 * 
	 * @param pdfFile the PDF file of the CFDI (cannot be {@code null})
	 * @param xmlUuid the UUID from the XML file (cannot be {@code null})
	 * @return {@code true} if both UUIDs match, {@code false} otherwise
	 */
	public boolean validarPDF(File pdfFile, String xmlUuid);
}
