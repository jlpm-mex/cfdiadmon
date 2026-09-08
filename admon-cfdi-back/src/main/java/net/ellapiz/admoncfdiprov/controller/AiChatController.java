package net.ellapiz.admoncfdiprov.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import net.ellapiz.admoncfdiprov.service.TextToSqlService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiChatController {

    private final TextToSqlService textToSqlService;

    public AiChatController(TextToSqlService textToSqlService) {
        this.textToSqlService = textToSqlService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> handleChatMessage(
            @RequestBody Map<String, String> request,
            @RequestAttribute("currentTenantId") Long tenantId) { // Extraído previamente de tu filtro JWT

        // PASO 1: Recibir mensaje del cliente
        String userMessage = request.get("message");

        // PASOS 2-4: Procesar pipeline de IA + SQL
        String aiResponse = textToSqlService.processUserQuery(userMessage, tenantId);

        // PASO 5: Responder al usuario (JSON)
        return ResponseEntity.ok(Map.of("response", aiResponse));
    }
}