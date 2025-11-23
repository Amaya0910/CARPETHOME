package grupo.proyecto_aula_carpethome.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.sql.Connection;
import java.util.*;

/**
 * Servicio para interactuar con Claude AI y ejecutar consultas SQL
 * basadas en preguntas en lenguaje natural.
 * Implementa principios SOLID: Single Responsibility y Dependency Inversion
 */
public class ClaudeAIService {

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_API_KEY = "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"; // Configurar desde archivo de propiedades
    private static final String MODEL = "claude-3-haiku-20240307";

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Connection dbConnection;



    // Esquema de la base de datos para que Claude genere SQL correcto
    private static final String DB_SCHEMA = """
    Base de datos de taller de costura CarpetHome:
    
    Tablas:
    - ADMINISTRADORES (ID_ADMIN, CEDULA, CONTRASENA)
    - CLIENTES (ID_CLIENTE, CEDULA)
    - EMPLEADOS (ID_EMPLEADO, CEDULA, CARGO, CONTRASENA)
    - PERSONAS (CEDULA, P_NOMBRE, S_NOMBRE, P_APELLIDO, S_APELLIDO, 
               P_CORREO, S_CORREO, P_TELEFONO, S_TELEFONO)
    - PROYECTOS (ID_PROYECTO, NOMBRE_PROYECTO, TIPO_PRODUCCION, FECHA_INICIO, 
                 FECHA_ENTREGA_ESTIMADA, FECHA_ENTREGA_REAL, ESTADO, 
                 COSTO_ESTIMADO, ID_CLIENTE)
    - PRENDAS (ID_PRENDA, NOMBRE_PRENDA, DESCRIPCION, COSTO_MATERIALES, 
               COSTO_TOTAL_ESTIMADO, ID_PROYECTO, ID_MEDIDA)
    - MEDIDAS (ID_MEDIDA, NOMBRE_MEDIDA, TIPO_MEDIDA, C_BUSTO, C_CINTURA, 
               C_CADERA, ALTURA_BUSTO, SEPARACION_BUSTO, RADIO_BUSTO, 
               BAJO_BUSTO, LARGO_FALDA, LARGO_CADERA, LARGO_VESTIDO, 
               LARGO_PANTALON, LARGO_MANGA)
    - ETAPAS (ID_ETAPA, NOMBRE_ETAPA, DESCRIPCION)
    - HIST_ETAPA (ID_PRENDA, ID_ETAPA, FECHA_INICIO, FECHA_FINAL, OBSERVACIONES)
    - GASTOS (ID_GASTO, NOMBRE_GASTO, ID_PRENDA, GASTO, DESCRIPCION)
    """;

    public ClaudeAIService(Connection dbConnection) {
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
        this.dbConnection = dbConnection;
    }

    /**
     * Procesa una pregunta del usuario y retorna una respuesta en lenguaje natural
     * @param preguntaUsuario Pregunta en lenguaje natural
     * @return Respuesta interpretada por Claude
     * @throws IOException Si hay error en la comunicación con la API
     * @throws SQLException Si hay error en la ejecución de SQL
     */
    public String procesarConsulta(String preguntaUsuario) throws IOException, SQLException {
        // Paso 1: Pedir a Claude que genere el SQL
        String sqlGenerado = generarSQL(preguntaUsuario);

        // Paso 2: Validar que el SQL sea seguro (solo SELECT)
        if (!esSQLSeguro(sqlGenerado)) {
            return "Lo siento, solo puedo realizar consultas de lectura (SELECT) por seguridad.";
        }

        // Paso 3: Ejecutar el SQL en Oracle
        List<Map<String, Object>> resultados = ejecutarSQL(sqlGenerado);

        // Paso 4: Pedir a Claude que interprete los resultados
        String respuestaFinal = interpretarResultados(preguntaUsuario, sqlGenerado, resultados);

        return respuestaFinal;
    }

    /**
     * Solicita a Claude que genere una consulta SQL basada en la pregunta
     */
    private String generarSQL(String pregunta) throws IOException {
        String prompt = String.format("""
            Eres un experto en SQL y bases de datos Oracle. 
            
            %s
            
            Pregunta del usuario: "%s"
            
            Genera ÚNICAMENTE la consulta SQL necesaria para responder esta pregunta.
            Responde SOLO con el código SQL, sin explicaciones adicionales.
            Usa sintaxis de Oracle SQL.
            La fecha actual es SYSDATE.
            Para comparar fechas de hoy usa: TRUNC(fecha_columna) = TRUNC(SYSDATE)
            """, DB_SCHEMA, pregunta);

        String respuestaClaude = llamarClaudeAPI(prompt);

        // Limpiar el SQL (remover markdown si existe)
        return limpiarSQL(respuestaClaude);
    }

    /**
     * Solicita a Claude que interprete los resultados de la consulta
     */
    private String interpretarResultados(String preguntaOriginal, String sqlEjecutado,
                                         List<Map<String, Object>> resultados) throws IOException {
        String prompt = String.format("""
            El usuario preguntó: "%s"
            
            Ejecuté esta consulta SQL: %s
            
            Resultados obtenidos: %s
            
            Interpreta estos resultados y responde al usuario en un lenguaje natural, 
            claro y profesional. Si no hay resultados, indícalo amablemente.
            No menciones el SQL ejecutado, solo presenta la información de forma clara.
            """, preguntaOriginal, sqlEjecutado, gson.toJson(resultados));

        return llamarClaudeAPI(prompt);
    }

    /**
     * Realiza la llamada a la API de Claude
     */
    /**
     * Realiza la llamada a la API de Claude
     */
    private String llamarClaudeAPI(String prompt) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);
        requestBody.addProperty("max_tokens", 1000);
        requestBody.add("messages", messages);

        // Imprimir para debug (quitar después)
        System.out.println("Request JSON: " + requestBody.toString());

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(CLAUDE_API_URL)
                .addHeader("x-api-key", CLAUDE_API_KEY)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();

            // Imprimir respuesta completa para debug
            System.out.println("Response Code: " + response.code());
            System.out.println("Response Body: " + responseBody);

            if (!response.isSuccessful()) {
                throw new IOException("Error en API de Claude: " + response.code() +
                        "\nDetalle: " + responseBody);
            }

            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            // Extraer el texto de la respuesta
            JsonArray content = jsonResponse.getAsJsonArray("content");
            return content.get(0).getAsJsonObject().get("text").getAsString();
        }
    }

    /**
     * Ejecuta la consulta SQL y retorna los resultados
     */
    private List<Map<String, Object>> ejecutarSQL(String sql) throws SQLException {
        List<Map<String, Object>> resultados = new ArrayList<>();

        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    fila.put(columnName, value);
                }
                resultados.add(fila);
            }
        }

        return resultados;
    }

    /**
     * Valida que el SQL sea una consulta SELECT segura
     */
    private boolean esSQLSeguro(String sql) {
        String sqlLimpio = sql.trim().toUpperCase();

        // Solo permitir SELECT
        if (!sqlLimpio.startsWith("SELECT")) {
            return false;
        }

        // Prohibir palabras clave peligrosas
        String[] palabrasPeligrosas = {
                "INSERT", "UPDATE", "DELETE", "DROP", "CREATE",
                "ALTER", "TRUNCATE", "EXEC", "EXECUTE"
        };

        for (String palabra : palabrasPeligrosas) {
            if (sqlLimpio.contains(palabra)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Limpia el SQL removiendo marcado markdown si existe
     */
    /**
     * Limpia el SQL removiendo marcado markdown y punto y coma final
     */
    private String limpiarSQL(String sql) {
        return sql.trim()
                .replaceAll("```sql", "")
                .replaceAll("```", "")
                .trim()
                .replaceAll(";\\s*$", "");  // ✅ AGREGAR ESTA LÍNEA - Elimina ; al final
    }
}