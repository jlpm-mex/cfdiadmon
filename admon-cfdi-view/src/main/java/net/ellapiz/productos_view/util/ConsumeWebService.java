package net.ellapiz.productos_view.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.ellapiz.preventa.vo.BodyVO;
import net.ellapiz.preventa.vo.HeaderVO;
import net.ellapiz.preventa.vo.MessageVO;

public abstract class ConsumeWebService {

	private String backendServerURL;
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumeWebService.class);
	
	protected ConsumeWebService(String backendServerURL) {
		this.backendServerURL = backendServerURL;
	}
	
	/**
	 * Consume el servicio web indicado, devolviendo un objeto de tipo BodyVO 
	 * @param servicePath debe ser enviado sin el primer "/"
	 * @param request
	 * @return MessageVO
	 * @throws IOException
	 */
	public MessageVO executeService(String servicePath, BodyVO request, String token) throws IOException{
		
		LOGGER.info("executeService()");
		MessageVO message = new MessageVO();
		try{
			
			URL url = new URL(backendServerURL + servicePath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
//			conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestMethod("POST"); 
			conn.setRequestProperty("Authorization", "Bearer "+token);
			message.getBody().put(MessageVO.REQUEST_MESSAGE, request);			
			
			ObjectMapper mapper = new ObjectMapper();
			String requestJson = mapper.writeValueAsString(message);
			LOGGER.info("reqJSON= "+requestJson);			
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(requestJson);
			wr.flush();
			wr.close();
			
			if (conn.getResponseCode() != 200) {
				LOGGER.warn(conn.getResponseMessage()+" "+conn.getResponseCode());
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}
			
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output + "\n");
			}
			
			message = mapper.readValue(sb.toString(), MessageVO.class);
			LOGGER.info(message.toString());
					
			conn.disconnect();
			
			
		}catch(IOException e){
			LOGGER.error(e.getMessage());
			throw e;
		} 
		
		return message;
	}

	
	public MessageVO executeService(String servicePath, BodyVO request, String token, HeaderVO header) 
			throws IOException{
		LOGGER.info("executeService()");
		
		MessageVO message = new MessageVO();
		try{
			
			URL url = new URL(backendServerURL + servicePath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
//			conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestMethod("POST"); 
			conn.setRequestProperty("Authorization", "Bearer "+token);
			message.getBody().put(MessageVO.REQUEST_MESSAGE, request);			
			message.setHeader(header);
			ObjectMapper mapper = new ObjectMapper();
			String requestJson = mapper.writeValueAsString(message);
						
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(requestJson);
			wr.flush();
			wr.close();
			
			if (conn.getResponseCode() != 200) {
				LOGGER.warn(conn.getResponseMessage()+" "+conn.getResponseCode());
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}
			
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output + "\n");
			}
			
			message = mapper.readValue(sb.toString(), MessageVO.class);
			LOGGER.info(message.toString());
			
			//GetChangarritosResponseTO resto = (GetChangarritosResponseTO) msg.getBody().get(MessageVO.RESPONSE_MESSAGE);
			//changarritos = resto.getChangarritos();		
					
			conn.disconnect();
			
			
		}catch(IOException e){
			LOGGER.error(e.getMessage());
			throw e;
		} 
		
		return message;
	}
}
