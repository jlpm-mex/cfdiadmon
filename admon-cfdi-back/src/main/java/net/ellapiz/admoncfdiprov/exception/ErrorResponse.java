package net.ellapiz.admoncfdiprov.exception;

import java.time.Instant;

public class ErrorResponse {
    private String code;
    private String message;
    private Instant timestamp;
    private String path;
    private int codeNumber;
    
    public ErrorResponse(String code, String message, String path) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now();
        this.path = path;
    }
    
    public ErrorResponse(int codeNumber, String message, String path) {
        this.message = message;
        this.timestamp = Instant.now();
        this.path = path;
        this.codeNumber = codeNumber;
    }
    
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Instant getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getCodeNumber() {
		return codeNumber;
	}
	public void setCodeNumber(int codeNumber) {
		this.codeNumber = codeNumber;
	}
	
    
}

