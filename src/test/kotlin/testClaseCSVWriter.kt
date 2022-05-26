import clasesPrograma.TablasBBDD
import clasesPrograma.csvWriter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class testClaseCSVWriter {

    val BBDD = TablasBBDD()

    // Antes de cada test se conecta a la base de datos para que se puedan realizar las transacciones necesarias sin problema
    @BeforeEach
    internal fun conexionBBDD()
    {
        BBDD.conexionBaseDeDatos()
    }

    /**
     * ## QUE HACE EL TEST:
     *   En este test se revisa que la clase csvWriter funcione correctamente y cree los documentos necesarios del volcado de la base
     *   de datos. Primero nos aseguramos que los archivos de test no esten creados aún y entonces se crean usando la clase correspondiente
     *   para luego revisar que se hayan creado sin problema. Finalmente los archivos de test se borran
     */
    @Test
    internal fun testCreacionDocumentos()
    {
        // Creamos unas variables con la posición de los archivos que queremos probar
        val archivoUsuario = File("src/main/resources/testArchivoUsuarios.csv")
        val archivoResultados = File("src/main/resources/testArchivoResultados.csv")

        // Primero comprobamos que estos archivos existen ya o no, en caso de existir no se realiza el test ya que daría un resultado incorrecto
        if(archivoUsuario.exists() || archivoResultados.exists()){
            println("No se han borrado los archivos anteriores de test correctamente")
            return
        // Si estos documentos no existen entonces seguimos con el test
        } else {
            // Se crea un objeto csvWriter que se encargará de crear los documentos con los nombres que necesitamos en nuestro test
            csvWriter(BBDD,"testArchivoUsuarios.csv","testArchivoResultados.csv")
            if(archivoUsuario.exists() && archivoResultados.exists()){
                // Si ambos archivos se han creado entonces se ha pasado el test correctamente
                println("Se crean correctamente los archivos")
            } else {
                // Si alguno de los dos o ninguno se crea, el test ha fallado
                println("Error, uno o ninguno de los archivos necesarios se han creado")
            }
        }
        // Nos aseguramos de borrar los documentos de test después de la realización de los test
        archivoUsuario.delete()
        archivoResultados.delete()
    }
}