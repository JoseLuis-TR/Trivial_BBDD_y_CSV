import clasesPrograma.TablasBBDD
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class TestTablasBBDD {
    val BBDD = TablasBBDD()

    // Antes de cada test se conecta a la base de datos para que se puedan realizar las transacciones sin problema
    @BeforeEach
    internal fun conectarBBDD()
    {
        BBDD.conexionBaseDeDatos()
    }

    /**
     * ## QUE HACE EL TEST:
     *   Este test sirve para revisar que la conexión a la base de datos se realiza correctamente haciendo uso del método isClosed
     *   que nos devolvera True o False en caso de que este conectada la base de datos o no
     */
    @Test
    internal fun testConexionBBDD()
    {
        val conexion = transaction { connection.isClosed }

        assertFalse(conexion,"Debe de dar false ya que la conexion con la base de datos no esta cerrada")

        println("Pasa correctamente el test de conexión a la base de datos")
    }

    /**
     * ## QUE HACE EL TEST:
     *   Este test sirve para revisar que se realizan correctamente los insert en la tabla de usuarios cuando lo necesitamos. Para ello
     *   creamos un usuario con un nombre con 11 caracteres (en el programa no se llama a la función de insert si el nombre de usuario supera los 10 caracteres)
     *   para evitar problemas con la base de datos, comprobamos que existe y finalmente lo borramos
     */
    @Test
    internal fun testInsertTablaUsuarios()
    {
        // Se crea el usuario con un nombre de 11 caracteres
        BBDD.insertNuevoUsuario("12345678911","test")

        // Se llama a la función que devuelve 0 (si el usuario no existe) o 1 (si el usuario si existe)
        val comprobarUsuario = BBDD.comprobarUsuarioExiste("12345678911")

        // Se comprueba que hayamos recibido un 1 en confirmación de que existe el usuario
        assertEquals(1,comprobarUsuario,"La función de comprobar usuario debe devolver 1 ya que existe el usuario")

        // Realizamos un delete del usuario con el que hemos testeado
        transaction {
            TablasBBDD.DatosUsuario.deleteWhere { TablasBBDD.DatosUsuario.usuario eq "12345678911" }
        }
        println("Pasa correctamente el test de insertar nuevo usuario")
    }

    /**
     *  ## QUE HACE EL TEST:
     *    En este test se comprueba que los insert a la tabla de puntuaciones se realiza correctamente. Para ello hacemos primero un selectAll().count()
     *    para conocer el tamaño de la tabla antes del insert, realizamos el insert y justo después hacemos otro selectAll().count() para comprobar el tamaño
     *    de la tabla después del insert. Se realiza el test para comprobar el tamaño antes y después y finalmente se borra esta ultima entrada.
     */
    @Test
    internal fun testInsertarTablaPuntuaciones()
    {
        // Realizamos un primer selectAll() para comprobar el tamaño de la tabla antes del insert
        val cantidadPartidasAntesInsert = transaction {
            TablasBBDD.ResultadosTrivial.slice(TablasBBDD.ResultadosTrivial.nomJugador).selectAll().count()
        }

        // Realizamos el insert
        BBDD.insertNuevaPartidaJugada("12345678911",10,"123")

        // Realizamos un segundo selectAll() para comprobar el tamaño de la tabla despues del insert
        val cantidadPartidasDespuesInsert = transaction {
            TablasBBDD.ResultadosTrivial.slice(TablasBBDD.ResultadosTrivial.nomJugador).selectAll().count()
        }
        // Se comprueba que el tamaño de ambas tablas no sea el mismo
        assertNotEquals(cantidadPartidasAntesInsert,cantidadPartidasDespuesInsert,"Deben de dar números diferentes de tamaño")

        // Borramos el ultimo insert de prueba realizado
        transaction {
            TablasBBDD.ResultadosTrivial.deleteWhere { TablasBBDD.ResultadosTrivial.nomJugador eq "12345678911" }
        }
        println("Pasa correctamente el test de insertar nueva partida jugada")
    }

    /**
     * ## QUE HACE EL TEST:
     *   En este test se comprueba que el autoincremento en el ID del usuario se realiza correctamente al hacer el insert. Para ello
     *   cogeremos antes de hacer un insert el último ID añadido a la tabla, se realiza el insert y después cogeremos el último ID que
     *   se encuentra en la tabla. En el test se comprueba que el primer ID es diferente al segundo ID debido al insert.
     */
    @Test
    internal fun testAutoincrementoIDTablaUsuario()
    {
        // Se realiza un select para sacar el ID de la última entrada de la tabla usuarios
        val ultimoID = transaction {
            TablasBBDD.DatosUsuario.slice(TablasBBDD.DatosUsuario.id).selectAll().last()[TablasBBDD.DatosUsuario.id]
        }

        // Realizamos insert en la tabla usuarios
        BBDD.insertNuevoUsuario("12345678911","test")

        // Se realiza un segundo select para sacar el ID de la última entrada de la tabla usuarios que deberá corresponder
        // al último usuario añadido
        val ultimoIDdespuesInsert = transaction {
            TablasBBDD.DatosUsuario.slice(TablasBBDD.DatosUsuario.id).selectAll().last()[TablasBBDD.DatosUsuario.id]
        }

        // Se comprueba que el primer ID sacado no sea el mismo que el segundo ID sacado
        assertNotEquals(ultimoID,ultimoIDdespuesInsert,"Los IDs no pueden ser los mismos ya que al añadir un nuevo usuario se autoincrementa")

        // Eliminamos el último insert realizado
        transaction {
            TablasBBDD.DatosUsuario.deleteWhere { TablasBBDD.DatosUsuario.usuario eq "12345678911" }
        }
        println("Pasa correctamente el test de autoincremento de ID")
    }
}