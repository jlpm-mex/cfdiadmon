package net.ellapiz.admoncfdiprov.service.impl;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.ellapiz.admoncfdiprov.service.PdfValidatorService;

@Service
public class PdfValidatorServiceImpl implements PdfValidatorService {
	
	private final String CLEANING_UUID_PATTERN = "[^A-Za-z0-9]";
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	/**
	 * Verifies that the UUID embedded in the PDF file matches the UUID from the XML file.
	 * 
	 * <p>This method is used to ensure that a PDF document corresponds to the same
	 * CFDI (Comprobante Fiscal Digital por Internet) as its associated XML file.</p>
	 * 
	 * @param pdfFile the PDF file to validate (must not be {@code null})
	 * @param xmlUuid the UUID extracted from the XML file (must not be {@code null})
	 * @return {@code true} if the UUIDs match, {@code false} otherwise
	 */
	public boolean validarPDF(File pdfFile,String xmlUuid) {
		LOGGER.info("validarPDF() file = {}, uuid = {}", pdfFile.getName(), xmlUuid);
		try {
			PDDocument document = PDDocument.load(pdfFile);
			//Instantiate PDFTextStripper class
			PDFTextStripper pdfStripper = new PDFTextStripper();
			//Retrieving text from PDF document
			String text;
			text = pdfStripper.getText(document);
			document.close();
			String subUUID = xmlUuid.substring(24,36);
			boolean evalRes = false;
			
			if(text.indexOf(subUUID) != -1) {
			    String extractedUUID = text.substring(text.indexOf(subUUID)-24,text.indexOf(subUUID)+12);
			    extractedUUID = extractedUUID.replaceAll(CLEANING_UUID_PATTERN, "");
			    evalRes = extractedUUID.compareTo(xmlUuid.replaceAll(CLEANING_UUID_PATTERN, "")) == 0;
			}
			 
			if(!evalRes) {
				LOGGER.info("Validando por nombre de archivo:");
				String nombreDeArchivo  = pdfFile.getName().replace(".pdf", "").replaceAll(CLEANING_UUID_PATTERN, "");
				evalRes = nombreDeArchivo.compareTo(xmlUuid.replaceAll(CLEANING_UUID_PATTERN, "")) == 0;
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

}
