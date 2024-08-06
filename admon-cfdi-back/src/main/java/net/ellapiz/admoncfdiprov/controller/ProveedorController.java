package net.ellapiz.admoncfdiprov.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import net.ellapiz.admoncfdiprov.management.ProveedorManagement;
import net.ellapiz.admoncfdiprov.to.ProveedorResponseTO;
import net.ellapiz.preventa.vo.ErrorVO;
import net.ellapiz.preventa.vo.HeaderVO;
import net.ellapiz.preventa.vo.MessageVO;
import net.ellapiz.proveedor.vo.ProveedorVO;

@Controller
public class ProveedorController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CfdiProvController.class);
	
	@Autowired
	private ProveedorManagement management;
	
	@PostMapping("/getallproveedor")
	private @ResponseBody MessageVO getAllProveedor(@RequestBody MessageVO messageVO) {
		LOGGER.info("getAllProveedor()");
		LOGGER.info(messageVO.toString());	
		
		HeaderVO header = messageVO.getHeader() == null ?  new HeaderVO() : messageVO.getHeader();
		messageVO.setHeader(header);
		try {
			List<ProveedorVO> proveedorList = management.getAllProveedor();
			ProveedorResponseTO response = new ProveedorResponseTO();
			response.setProveedoresList(proveedorList);
			messageVO.getBody().put(MessageVO.RESPONSE_MESSAGE, response);
			messageVO.getHeader().setMessageStatus(HeaderVO.SUCCESS);
			
			
		}catch(Exception e) {
			LOGGER.error(e.getMessage());
			addError(messageVO, e);
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
