package net.ellapiz.admoncfdiprov.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TextToSqlService {

    private final ChatClient chatClient;
    private final SqlSecurityService sqlSecurityService;
    private final JdbcTemplate readOnlyJdbcTemplate;

    public TextToSqlService(ChatClient.Builder chatClientBuilder,
                            SqlSecurityService sqlSecurityService,
                            JdbcTemplate readOnlyJdbcTemplate) {
        this.chatClient = chatClientBuilder.build();
        this.sqlSecurityService = sqlSecurityService;
        this.readOnlyJdbcTemplate = readOnlyJdbcTemplate;
    }

    private static final String SCHEMA_PROMPT = """
        Eres un asistente experto en SQL para una base de datos PostgreSQL de facturación CFDI 4.0.
        Estructura de la BD:
        - comprobante (id, uuid, fecha, total, subtotal, emisor_rfc, receptor_rfc, tipo_de_comprobante, tenant_id)
        - concepto (id, comprobante_id, descripcion, importe, cantidad, valor_unitario, tenant_id)
        
        Reglas:
        1. Devuelve ÚNICAMENTE la consulta SQL ejecutable.
        2. NO agregues formato markdown ni bloques de código (sin ```sql).
        3. Solo utiliza sentencias SELECT.
        """;

    public String processUserQuery(String userMessage, Long tenantId) {
        // PASO 2: Generar SQL vía LLM
        String generatedSql = chatClient.prompt()
                .system(SCHEMA_PROMPT)
                .user(userMessage)
                .call()
                .content();

        try {
            // PASO 3: Inyectar tenant_id con JSqlParser
            String securedSql = sqlSecurityService.injectTenantId(generatedSql, tenantId);

            // PASO INTERMEDIO: Ejecutar consulta en BD
            List<Map<String, Object>> queryResults = readOnlyJdbcTemplate.queryForList(securedSql);

            // PASO 4: Enviar datos de la BD al LLM para redactar la respuesta
            String interpretationPrompt = String.format("""
                El usuario preguntó: "%s"
                El resultado de la base de datos es: %s
                Responde al usuario en español, de forma clara, directa y concisa.
                """, userMessage, queryResults.toString());

            return chatClient.prompt()
                    .user(interpretationPrompt)
                    .call()
                    .content();

        } catch (Exception e) {
            return "No pude procesar la consulta de forma segura. Intenta refrasear tu pregunta.";
        }
    }
}