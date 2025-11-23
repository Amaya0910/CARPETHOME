package grupo.proyecto_aula_carpethome.Utilidades;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad para encriptar y validar contraseñas usando SHA-256
 */
public class PasswordUtils {

    /**
     * Encripta una contraseña usando SHA-256
     *
     * @param password Contraseña en texto plano
     * @return Contraseña encriptada en formato hexadecimal
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al encriptar la contraseña", e);
        }
    }

    /**
     * Convierte un array de bytes a string hexadecimal
     *
     * @param hash Array de bytes
     * @return String hexadecimal
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Verifica si una contraseña coincide con su hash
     *
     * @param password Contraseña en texto plano
     * @param hashedPassword Hash almacenado
     * @return true si coinciden, false si no
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        String hashedInput = hashPassword(password);
        return hashedInput.equals(hashedPassword);
    }
}