import java.sql.*;
import java.util.Scanner;

/**
 * Aplicación para gestionar artistas y álbumes en la base de datos Chinook
 * Permite consultar, añadir, modificar y eliminar artistas, así como buscar
 * álbumes
 * 
 * @author Ahmed Aziz
 * @version 1.0
 */
public class ChinookApp {
    // Datos de conexión a la base de datos
    private static final String URL = "jdbc:postgresql://localhost:5432/chinook_v2"; // private static final String URL
                                                                                     // =
                                                                                     // "jdbc:postgresql://localhost:5432/postgres";

    private static final String USER = "ahmed";
    private static final String PASSWORD = "ahmed";
    private static Connection conexion;
    private static Scanner scanner = new Scanner(System.in);

    /**
     * Método principal que inicia la aplicación y muestra el menú de opciones
     * 
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        try {
            // Establecer conexión con la base de datos
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa");

            // Bucle principal del menú
            while (true) {
                System.out.println(
                        "\n1. Ver artistas\n2. Buscar artista\n3. Ver álbumes\n4. Añadir artista\n5. Modificar artista\n6. Borrar artista\n0. Salir");
                System.out.print("Elige opción: ");
                int opcion = Integer.parseInt(scanner.nextLine());

                // Salir del programa si la opción es 0
                if (opcion == 0)
                    break;

                // Procesar la opción seleccionada
                switch (opcion) {
                    case 1:
                        consultarTodosArtistas();
                        break;
                    case 2:
                        consultarArtistasPorNombre();
                        break;
                    case 3:
                        consultarPrimerosAlbumes();
                        break;
                    case 4:
                        agregarArtista();
                        break;
                    case 5:
                        modificarArtista();
                        break;
                    case 6:
                        borrarArtista();
                        break;
                    default:
                        System.out.println("Opción inválida");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            // Cerrar recursos
            try {
                if (conexion != null)
                    conexion.close();
            } catch (SQLException e) {
                // No hacer nada si falla el cierre
            }
            scanner.close();
        }
    }

    /**
     * Muestra todos los artistas existentes ordenados por ID
     */
    private static void consultarTodosArtistas() {
        try (Statement stmt = conexion.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT artist_id, name FROM artist ORDER BY artist_id")) {
            // Verificar si hay resultados
            if (!rs.next()) {
                System.out.println("No hi ha resultats.");
                return;
            }
            // Mostrar cada artista
            do {
                System.out.println("ID: " + rs.getInt("artist_id") + ", NOM: " + rs.getString("name"));
            } while (rs.next());
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Busca artistas por nombre (búsqueda parcial)
     */
    private static void consultarArtistasPorNombre() {
        System.out.print("Introduce el nombre del artista: ");
        String nombre = scanner.nextLine();
        // Validar longitud mínima
        if (nombre.length() < 2) {
            System.out.println("Introduce al menos 2 caracteres");
            return;
        }
        try (PreparedStatement pstmt = conexion
                .prepareStatement("SELECT artist_id, name FROM artist WHERE name LIKE ?")) {
            // Configurar la búsqueda con comodines
            pstmt.setString(1, "%" + nombre + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                // Verificar si hay resultados
                if (!rs.next()) {
                    System.out.println("No hi ha resultats.");
                    return;
                }
                // Mostrar cada artista encontrado
                do {
                    System.out.println("ID: " + rs.getInt("artist_id") + ", NOM: " + rs.getString("name"));
                } while (rs.next());
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Muestra hasta 5 álbumes de un artista buscado por nombre
     */
    private static void consultarPrimerosAlbumes() {
        System.out.print("Introduce el nombre del artista: ");
        String nombre = scanner.nextLine();
        // Validar longitud mínima
        if (nombre.length() < 2) {
            System.out.println("Introduce al menos 2 caracteres");
            return;
        }
        // Consulta con JOIN para obtener los álbumes del artista
        String sql = "SELECT al.album_id, al.title FROM album al JOIN artist ar ON al.artist_id = ar.artist_id WHERE ar.name LIKE ? LIMIT 5";
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, "%" + nombre + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                // Verificar si hay resultados
                if (!rs.next()) {
                    System.out.println("No hi ha resultats.");
                    return;
                }
                // Mostrar cada álbum encontrado
                do {
                    System.out.println("ID_ALBUM: " + rs.getInt("album_id") + ", NOM_ALBUM: " + rs.getString("title"));
                } while (rs.next());
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Añade un nuevo artista a la base de datos con ID manual
     */
    private static void agregarArtista() {
        try {
            // Solicitar datos del nuevo artista
            System.out.print("Introduce el ID del nuevo artista: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Introduce el nombre del nuevo artista: ");
            String nombre = scanner.nextLine();

            // Validar longitud mínima del nombre
            if (nombre.length() < 2) {
                System.out.println("Introduce al menos 2 caracteres");
                return;
            }

            // Verificar si el ID ya existe para evitar duplicados
            try (PreparedStatement checkStmt = conexion.prepareStatement("SELECT 1 FROM artist WHERE artist_id = ?")) {
                checkStmt.setInt(1, id);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Error: Ya existe un artista con ese ID");
                        return;
                    }
                }
            }

            // Insertar el nuevo artista
            try (PreparedStatement pstmt = conexion
                    .prepareStatement("INSERT INTO artist (artist_id, name) VALUES (?, ?)")) {
                pstmt.setInt(1, id);
                pstmt.setString(2, nombre);
                int rows = pstmt.executeUpdate();
                System.out.println(rows > 0 ? "Artista añadido correctamente" : "Error al añadir el artista");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: El ID debe ser un número entero");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Modifica el nombre de un artista existente
     */
    private static void modificarArtista() {
        System.out.print("Introduce la ID del artista: ");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.print("Introduce el nuevo nombre del artista: ");
        String nuevoNombre = scanner.nextLine();
        // Validar longitud mínima
        if (nuevoNombre.length() < 2) {
            System.out.println("Introduce al menos 2 caracteres");
            return;
        }
        // Actualizar el nombre del artista
        try (PreparedStatement pstmt = conexion.prepareStatement("UPDATE artist SET name = ? WHERE artist_id = ?")) {
            pstmt.setString(1, nuevoNombre);
            pstmt.setInt(2, id);
            int rows = pstmt.executeUpdate();
            System.out.println(rows > 0 ? "Artista modificado correctamente" : "No se encontró el artista");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Elimina un artista de la base de datos
     */
    private static void borrarArtista() {
        System.out.print("Introduce la ID del artista a eliminar: ");
        int id = Integer.parseInt(scanner.nextLine());
        // Eliminar el artista
        try (PreparedStatement pstmt = conexion.prepareStatement("DELETE FROM artist WHERE artist_id = ?")) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            System.out.println(rows > 0 ? "Artista eliminado correctamente" : "No se encontró el artista");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}