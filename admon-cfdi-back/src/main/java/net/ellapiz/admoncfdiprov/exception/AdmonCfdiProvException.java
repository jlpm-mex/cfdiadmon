package net.ellapiz.admoncfdiprov.exception;

public class AdmonCfdiProvException extends Exception {
	
	private int errorCode;
	
	public AdmonCfdiProvException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public enum ErrorCodes {
		CFDI_YA_REGISTRADO(1),
		ARCHIVOS_NO_CORRESPONDEN(2),
		ARCHIVOS_EN_USO(3),
		TIPO_DE_COMPROBANTE_NO_RECONOCIDO(4),
		XML_NO_SE_PUEDE_PARSEAR(5);
		
		private int codigo;
		
		ErrorCodes(int codigo){
			this.codigo = codigo;
		}

		public int getCodigo() {
			return codigo;
		}

		public void setCodigo(int codigo) {
			this.codigo = codigo;
		}
		
		
		
		
	}

}
