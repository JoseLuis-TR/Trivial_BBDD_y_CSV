package clasesPrograma

import com.floern.castingcsv.castingCSV
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

// Data class que necesitaremos para que la libreria castingCSV cree el archivo que necesitemos
data class Usuarios(
    val ID: String,
    val Jugador: String,
    val Contrasenya: String,
    val NumJugadas: String
)

// Data class que necesitaremos para que la libreria castingCSV cree el archivo que necesitemos
data class Resultados(
    val Jugador:String,
    val Puntuacion:String,
    val Fecha:String
)

/**
 * ## QUE HACE:
 *   Es una clase que es creada cuando el jugador decide no seguir jugando y lo que hace es crear dos documentos CSV
 *   con los datos obtenidos de las dos tabla de la base de datos que manejamos en este trivial.
 */
class csvWriter(
    private var baseDatos:TablasBBDD,
    private var archivoUsuarios:String,
    private var archivoResultados:String) {

    init {
        gestionArchivoCSVSalida()
    }

    /**
     * ## QUE HACE:
     *   Esta función se encarga de gestionar los archivos CSV ya creados, borrandolos en caso de ya existir y luego los crea
     *   pero aún se encuentran vacios
     */
    fun gestionArchivoCSVSalida()
    {
        // Obtenemos el Path de ambos archivos CSV que tendrán la información de nuestras tablas
        val pathUsuarios = Paths.get("src/main/resources/${archivoUsuarios}")
        val pathResultados = Paths.get("src/main/resources/${archivoResultados}")

        // Si existen estos archivos, son eliminado para evitar que se repita la misma información en los archivos
        Files.deleteIfExists(pathUsuarios)
        Files.deleteIfExists(pathResultados)

        // Se crean de nuevo los dos archivos csv con sendos nombres
        val csvUsuariosFile = File("src/main/resources/${archivoUsuarios}")
        val csvResultadosFile = File("src/main/resources/${archivoResultados}")
        csvUsuariosFile.createNewFile()
        csvResultadosFile.createNewFile()

        recogidaInfoTablas(csvUsuariosFile,csvResultadosFile)
    }

    /**
     * ## QUE HACE:
     *   Se encarga de llamar a la clase Base De Datos para que le devuelva un selectAll con una lista
     *   de las tablas de puntuaciones y usuarios que se necesitaran en la función siguiente para la creación de los CSV
     */
    fun recogidaInfoTablas(csvUsuariosFile:File, csvResultadosFile:File)
    {
        // Usamos la libreria castingCSV para la creación de estos archivos. Recogemos la info de las data class que hemos creado
        // al principio del documento para poder crear una lista de listas en las que añadiremos las distintas filas de la
        // que constarán nuestros CSV
        val filasCSVUsuarios = baseDatos.selectAllUsuarios()
        val filasCSVResultados = baseDatos.selectALLResultadosTrivial()

        escrituraCSV(csvUsuariosFile,filasCSVUsuarios,csvResultadosFile,filasCSVResultados)
    }

    /**
     * ## QUE HACE:
     *   Usa la libreria castingCSV para la escritura de los datos recogidos de las select realizadas en la función anterior
     *   en los archivos que hemos creado en la primera función de este objeto
     */
    fun escrituraCSV(csvUsuariosFile: File,filasCSVUsuarios:MutableList<Usuarios>,csvResultadosFile: File, filasCSVResultados:MutableList<Resultados>)
    {
        // Se escribe la información de la tabla usuarios en el archivo CSV de usuarios
        castingCSV().toCSV(filasCSVUsuarios,csvUsuariosFile.outputStream())

        // Se escribe la información de la tabla resultados_trivial en el archivo CSV de resultados de las partidas
        castingCSV().toCSV(filasCSVResultados,csvResultadosFile.outputStream())
    }
}