package net.ellapiz.admoncfdiprov.parser;

import java.io.File;

import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;

public interface CfdiParser {
	
	public ComprobanteVO crearComprobanteVO(File xml) throws Exception;
	
	
}
