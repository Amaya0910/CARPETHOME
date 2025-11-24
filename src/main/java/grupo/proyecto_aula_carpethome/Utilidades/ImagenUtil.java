package grupo.proyecto_aula_carpethome.Utilidades;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Clase utilitaria para gestionar imágenes en el sistema de historial de etapas
 */
public class ImagenUtil {

    // Directorio base para almacenar imágenes
    private static final String DIRECTORIO_BASE = "images/prendas/";

    // Formatos de imagen permitidos
    private static final List<String> FORMATOS_PERMITIDOS = Arrays.asList("jpg", "jpeg", "png", "gif");

    // Tamaño máximo de archivo en bytes (5MB)
    private static final long TAMANO_MAXIMO = 5 * 1024 * 1024;

    /**
     * Inicializa el directorio de imágenes si no existe
     */
    public static void inicializarDirectorio() {
        try {
            Path path = Paths.get(DIRECTORIO_BASE);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("✓ Directorio de imágenes creado: " + DIRECTORIO_BASE);
            }
        } catch (IOException e) {
            System.err.println("✗ Error al crear directorio de imágenes: " + e.getMessage());
        }
    }

    /**
     * Abre un FileChooser para seleccionar una imagen
     * @param ownerWindow Ventana padre del diálogo
     * @return Archivo seleccionado o null si se cancela
     */
    public static File seleccionarImagen(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Referencia");

        // Configurar extensiones permitidas
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Imágenes (*.jpg, *.jpeg, *.png, *.gif)",
                "*.jpg", "*.jpeg", "*.png", "*.gif"
        );
        fileChooser.getExtensionFilters().add(imageFilter);

        // Establecer directorio inicial (escritorio por defecto)
        File userHome = new File(System.getProperty("user.home"));
        File desktop = new File(userHome, "Desktop");
        if (desktop.exists()) {
            fileChooser.setInitialDirectory(desktop);
        }

        return fileChooser.showOpenDialog(ownerWindow);
    }

    /**
     * Valida que un archivo sea una imagen válida
     * @param archivo Archivo a validar
     * @throws IllegalArgumentException si el archivo no es válido
     */
    public static void validarImagen(File archivo) {
        if (archivo == null || !archivo.exists()) {
            throw new IllegalArgumentException("El archivo no existe");
        }

        if (!archivo.isFile()) {
            throw new IllegalArgumentException("La ruta no corresponde a un archivo");
        }

        // Validar tamaño
        if (archivo.length() > TAMANO_MAXIMO) {
            double tamanoMB = archivo.length() / (1024.0 * 1024.0);
            throw new IllegalArgumentException(
                    String.format("El archivo es demasiado grande (%.2f MB). Máximo permitido: 5 MB", tamanoMB)
            );
        }

        // Validar extensión
        String nombreArchivo = archivo.getName().toLowerCase();
        boolean formatoValido = FORMATOS_PERMITIDOS.stream()
                .anyMatch(nombreArchivo::endsWith);

        if (!formatoValido) {
            throw new IllegalArgumentException(
                    "Formato de imagen no válido. Formatos permitidos: " + String.join(", ", FORMATOS_PERMITIDOS)
            );
        }
    }

    /**
     * Guarda una imagen en el directorio del sistema con nombre único
     * @param archivoOrigen Archivo de imagen a guardar
     * @param idPrenda ID de la prenda
     * @param idEtapa ID de la etapa
     * @return Ruta relativa del archivo guardado
     * @throws IOException si hay error al copiar el archivo
     */
    public static String guardarImagen(File archivoOrigen, String idPrenda, String idEtapa) throws IOException {
        // Validar archivo
        validarImagen(archivoOrigen);

        // Inicializar directorio
        inicializarDirectorio();

        // Obtener extensión del archivo
        String nombreOriginal = archivoOrigen.getName();
        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));

        // Generar nombre único: PRE001_ETP001_20250523143045.jpg
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String nombreNuevo = String.format("%s_%s_%s%s", idPrenda, idEtapa, timestamp, extension);

        // Ruta completa del archivo destino
        Path rutaDestino = Paths.get(DIRECTORIO_BASE + nombreNuevo);

        // Copiar archivo
        Files.copy(archivoOrigen.toPath(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("✓ Imagen guardada: " + rutaDestino);

        // Retornar ruta relativa para la BD
        return DIRECTORIO_BASE + nombreNuevo;
    }

    /**
     * Carga una imagen desde una ruta relativa
     * @param rutaRelativa Ruta relativa de la imagen
     * @return Objeto Image de JavaFX o null si no existe
     */
    public static Image cargarImagen(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.trim().isEmpty()) {
            return null;
        }

        try {
            File archivo = new File(rutaRelativa);
            if (!archivo.exists()) {
                System.err.println("✗ Imagen no encontrada: " + rutaRelativa);
                return null;
            }

            return new Image(archivo.toURI().toString());
        } catch (Exception e) {
            System.err.println("✗ Error al cargar imagen: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si existe un archivo de imagen
     * @param rutaRelativa Ruta relativa de la imagen
     * @return true si el archivo existe, false en caso contrario
     */
    public static boolean existeImagen(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.trim().isEmpty()) {
            return false;
        }

        File archivo = new File(rutaRelativa);
        return archivo.exists() && archivo.isFile();
    }

    /**
     * Elimina físicamente un archivo de imagen
     * @param rutaRelativa Ruta relativa de la imagen a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public static boolean eliminarImagen(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.trim().isEmpty()) {
            return false;
        }

        try {
            File archivo = new File(rutaRelativa);
            if (archivo.exists()) {
                boolean eliminado = archivo.delete();
                if (eliminado) {
                    System.out.println("✓ Imagen eliminada: " + rutaRelativa);
                } else {
                    System.err.println("✗ No se pudo eliminar la imagen: " + rutaRelativa);
                }
                return eliminado;
            }
            return false;
        } catch (Exception e) {
            System.err.println("✗ Error al eliminar imagen: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el tamaño de una imagen en MB
     * @param archivo Archivo de imagen
     * @return Tamaño en MB
     */
    public static double obtenerTamanoMB(File archivo) {
        if (archivo == null || !archivo.exists()) {
            return 0.0;
        }
        return archivo.length() / (1024.0 * 1024.0);
    }

    /**
     * Verifica si el formato de un archivo es válido
     * @param nombreArchivo Nombre del archivo
     * @return true si el formato es válido
     */
    public static boolean esFormatoValido(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            return false;
        }

        String nombreLower = nombreArchivo.toLowerCase();
        return FORMATOS_PERMITIDOS.stream().anyMatch(nombreLower::endsWith);
    }

    /**
     * Obtiene la extensión de un archivo
     * @param nombreArchivo Nombre del archivo
     * @return Extensión sin el punto (ej: "jpg")
     */
    public static String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Limpia el directorio de imágenes eliminando archivos huérfanos
     * (imágenes que no tienen referencia en la base de datos)
     * Este método debería ejecutarse periódicamente como tarea de mantenimiento
     */
    public static void limpiarImagenesHuerfanas() {
        try {
            Path directorio = Paths.get(DIRECTORIO_BASE);
            if (!Files.exists(directorio)) {
                return;
            }

            // Nota: Esta es una operación básica. En producción debería
            // verificar contra la base de datos qué imágenes están en uso
            System.out.println("ℹ Limpieza de imágenes huérfanas - Implementar verificación con BD");

        } catch (Exception e) {
            System.err.println("✗ Error al limpiar imágenes: " + e.getMessage());
        }
    }
}