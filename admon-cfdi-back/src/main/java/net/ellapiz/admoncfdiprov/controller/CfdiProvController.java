package net.ellapiz.admoncfdiprov.controller;


import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import net.ellapiz.admoncfdiprov.management.CfdiProvManagement;
import net.ellapiz.admoncfdiprov.to.FindCfdiProvRequestTO;
import net.ellapiz.admoncfdiprov.to.FindCfdiProvResponseTO;
import net.ellapiz.admoncfdiprov.to.ProcesarCfdiResponseTO;
import net.ellapiz.admoncfdiprov.to.SaveCfdiProvRequestTO;
import net.ellapiz.admoncfdiprov.to.SaveCfdiProvResponseTO;
import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;
import net.ellapiz.preventa.vo.ErrorVO;
import net.ellapiz.preventa.vo.HeaderVO;
import net.ellapiz.preventa.vo.MessageVO;

@Controller
public class CfdiProvController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CfdiProvController.class);
	
	@Autowired
	private CfdiProvManagement management;
	
	@PostMapping("/savecfdiprov")
	public @ResponseBody MessageVO savePreVenta(@RequestBody MessageVO messageVO) throws Exception {
		LOGGER.info("savePreVenta()");	
		
		HeaderVO header = messageVO.getHeader() == null ?  new HeaderVO() : messageVO.getHeader();
		messageVO.setHeader(header);
		
		SaveCfdiProvRequestTO request = (SaveCfdiProvRequestTO) 
				messageVO.getBody().get(MessageVO.REQUEST_MESSAGE);
		try {
			
			ComprobanteVO comprobanteVO = management.guardar(request.getComprobanteVO());
			SaveCfdiProvResponseTO response = new SaveCfdiProvResponseTO();
			response.setComprobanteVO(comprobanteVO);
			messageVO.getHeader().setMessageStatus(HeaderVO.SUCCESS);
			messageVO.getBody().put(MessageVO.RESPONSE_MESSAGE,response);
		}catch(Exception e) {
			LOGGER.info(e.getMessage());
			addError(messageVO,e);
		}
		return messageVO;
	}
	
	@PostMapping("/findbydate")
	public @ResponseBody MessageVO findByDate(@RequestBody MessageVO messageVO){
		LOGGER.info("findByDate()");
		LOGGER.info(messageVO.toString());	
		
		HeaderVO header = messageVO.getHeader() == null ?  new HeaderVO() : messageVO.getHeader();
		messageVO.setHeader(header);
		
		FindCfdiProvRequestTO request = (FindCfdiProvRequestTO) 
				messageVO.getBody().get(MessageVO.REQUEST_MESSAGE);
		
		try {
			
			FindCfdiProvResponseTO response = management.findByDate(
					request.getStartDate(), request.getEndDate());
			 			
			messageVO.getHeader().setMessageStatus(HeaderVO.SUCCESS);
			messageVO.getBody().put(MessageVO.RESPONSE_MESSAGE,response);
			
		}catch(Exception e) {
			LOGGER.info(e.getMessage());
			addError(messageVO,e);
		}
		
		return messageVO;
	}
	
	@PostMapping("/findbycomprobantedate")
	public @ResponseBody MessageVO findByComprobanteDate(@RequestBody MessageVO messageVO){
		LOGGER.info("findByComprobanteDate()");
		LOGGER.info(messageVO.toString());	
		
		HeaderVO header = messageVO.getHeader() == null ?  new HeaderVO() : messageVO.getHeader();
		messageVO.setHeader(header);
		
		FindCfdiProvRequestTO request = (FindCfdiProvRequestTO) 
				messageVO.getBody().get(MessageVO.REQUEST_MESSAGE);
		
		try {
			
			FindCfdiProvResponseTO response = management.findByComprobanteDate(
					request.getStartDate(), request.getEndDate());
			
			messageVO.getHeader().setMessageStatus(HeaderVO.SUCCESS);
			messageVO.getBody().put(MessageVO.RESPONSE_MESSAGE,response);
			
		}catch(Exception e) {
			LOGGER.info(e.getMessage());
			addError(messageVO,e);
		}
		
		return messageVO;
	}
	
	@PostMapping("/findbyproveedor")
	public @ResponseBody MessageVO findByProveedor(@RequestBody MessageVO messageVO){
		LOGGER.info("findByProveedor()");
		LOGGER.info(messageVO.toString());	
		
		HeaderVO header = messageVO.getHeader() == null ?  new HeaderVO() : messageVO.getHeader();
		messageVO.setHeader(header);
		
		FindCfdiProvRequestTO request = (FindCfdiProvRequestTO) 
				messageVO.getBody().get(MessageVO.REQUEST_MESSAGE);
		
		try {
			
			FindCfdiProvResponseTO response = management.findByProveedor(
					request.getRfc(), String.valueOf(request.getYear()));
			messageVO.getHeader().setMessageStatus(HeaderVO.SUCCESS);
			messageVO.getBody().put(MessageVO.RESPONSE_MESSAGE,response);
			
		}catch(Exception e) {
			LOGGER.info(e.getMessage());
			addError(messageVO,e);
		}
		
		return messageVO;
	}

	@PostMapping("/procesarCfdi")
	public @ResponseBody MessageVO procesarCfdi() {
		LOGGER.info("procesarCfdi()");
		MessageVO messageVO = new MessageVO();
		HeaderVO headerVO = new HeaderVO();
		try {
			ProcesarCfdiResponseTO response = management.procesarCfdi();
			messageVO.getBody().put(MessageVO.RESPONSE_MESSAGE, response);	
			headerVO.setMessageStatus(HeaderVO.SUCCESS);
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
			addError(messageVO,e);
		}
		
		return messageVO;
		
	}
	 
	
	private void addError(MessageVO msg,Exception e){
		ArrayList<ErrorVO> errors = new ArrayList<>();
		ErrorVO error = new ErrorVO();
		error.setMessage(e.getMessage());
		errors.add(error);
		msg.setErrors(errors);
		msg.getHeader().setMessageStatus(HeaderVO.ERROR);
	}
	
}
