package grupo.proyecto_aula_carpethome.Utilidades;

import java.time.LocalDate;
import java.util.Date;
import java.util.regex.Pattern;

public class Validador {


    private static final Pattern PATTERN_EMAIL = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );


    // Validar correo
    public static void validarCorreo(String correo) {
        if (correo != null && !correo.trim().isEmpty()) {
            if (!PATTERN_EMAIL.matcher(correo).matches()) {
                throw new IllegalArgumentException("Correo electrónico inválido");
            }
            if (correo.length() > 40) {
                throw new IllegalArgumentException("El correo no puede exceder 40 caracteres");
            }
        }
    }

    // Validar teléfono
    public static void validarTelefono(Long telefono) {
        if(telefono != null){
            String telefonoStr = telefono.toString();

            if (telefonoStr.length() != 10) {
                throw new IllegalArgumentException("El teléfono debe tener exactamente 10 dígitos. Actual: " + telefonoStr.length());
            }

            if (telefono <= 0) {
                throw new IllegalArgumentException("El teléfono debe ser un número positivo");
            }
        }
    }

    // Validar nombres
    public static void validarNombre(String nombre, String campo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }
        if (nombre.length() > 15) {
            throw new IllegalArgumentException(campo + " no puede exceder 15 caracteres");
        }
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            throw new IllegalArgumentException(campo + " solo puede contener letras");
        }
    }
    // Validar contraseña
    public static void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (contrasena.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        if (contrasena.length() > 15) {
            throw new IllegalArgumentException("La contraseña no puede exceder 15 caracteres");
        }
    }

    // Validar texto genérico con límite
    public static void validarTexto(String texto, String campo, int maxLength, boolean obligatorio) {
        if (obligatorio && (texto == null || texto.trim().isEmpty())) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }
        if (texto != null && texto.length() > maxLength) {
            throw new IllegalArgumentException(campo + " no puede exceder " + maxLength + " caracteres");
        }
    }

    // Validar número positivo
    public static void validarNumeroPositivo(Number numero, String campo) {
        if (numero != null && numero.doubleValue() < 0) {
            throw new IllegalArgumentException(campo + " no puede ser negativo");
        }
    }

    // Validar rango de fechas
    public static void validarRangoFechas(Date fechaInicio, Date fechaFin) {
        if (fechaInicio != null && fechaFin != null) {
            if (fechaFin.before(fechaInicio)) {
                throw new IllegalArgumentException("La fecha de finalización no puede ser anterior a la fecha de inicio");
            }
        }
    }

    //validar precio
    public static void validarPrecio(Double precio, String campo) {
        if (precio == null) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException(campo + " debe ser mayor a cero");
        }

    }

    public static void validarCedula (String cedula) {
        if(cedula == null || cedula.trim().isEmpty()){
            throw new IllegalArgumentException("La cedula es obligatoria");
        }
        if (cedula.length() != 10) {
            throw new IllegalArgumentException("La cedula debe tener exactamente 10 caracteres");
        }

    }



}
