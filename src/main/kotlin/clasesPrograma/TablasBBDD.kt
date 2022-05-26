package clasesPrograma

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


class TablasBBDD {

    /**
     * Tabla que guarda los resultados de los usuarios que juegan al trivial
     */
    object ResultadosTrivial : Table() {
        val nomJugador = text("Nombre_Jugador")
        val puntuacion = integer("Puntuación")
        val fecha = text("Fecha")
    }

    /**
     * Tabla que guarda los datos de los usuarios registrados en el trivial
     */
    object DatosUsuario : Table() {
        val id = integer("id").autoIncrement()
        val usuario = text("Nombre_Usuario")
        val contrasenya = text("Contraseña")
        val partidasJugadas = integer("Partidas_Jugadas")
        override val primaryKey = PrimaryKey(id, name ="PK_USUARIO_CONTRA")
    }

    /**
     * ## QUE HACE:
     *   Es llamada para inciar la base de datos, lo que será necesario para realizar cualquier operación en ella
     */
    fun conexionBaseDeDatos()
    {
        Database.connect("jdbc:sqlite:src/main/kotlin/database.db")
        transaction {
            SchemaUtils.create(ResultadosTrivial)
            SchemaUtils.create(DatosUsuario)
        }
    }

    /**
     * ## QUE HACE:
     *   Es una función que se usa cuando se quiere insertar un nuevo usuario con su contraseña y su contador de partidas
     *   jugadas a 0.
     */
    fun insertNuevoUsuario(nombreJugador:String, nuevaContrasenya:String)
    {
        // Se añade el nuevo usuario con su contraseña a la tabla de usuarios
        transaction {
            DatosUsuario.insert {
                it[usuario] = nombreJugador
                it[contrasenya] = nuevaContrasenya
                it[partidasJugadas] = 0
            }
        }
    }

    /**
     * ## QUE HACE:
     *   Es una función que se usa cuando se necesita recuperar la contraseña de algun usuario, devolviendo la contraseña
     *   de este
     */
    fun recuperarContrasenyaUsuario(nombreJugador: String): String
    {
        val contrasenya = transaction {
            DatosUsuario.select { DatosUsuario.usuario eq nombreJugador}.first()[DatosUsuario.contrasenya]
        }
        return contrasenya
    }

    // Se hace una query para saber cuantos usuarios existen en la tabla usuarios que contengan
    // el mismo nombre que el usuario actual
    /**
     * ## QUE HACE:
     *   Esta función hace una query donde cuenta cuantos usuarios existen en la tabla usuarios con el mismo nombre del usuario del juego
     *   actual, devolverá 1 (si ya existe el usuario) o 0 (si aun no existe el usuario),
     *   ya que no se permite crear nuevos usuarios con el mismo nombre
     */
    fun comprobarUsuarioExiste(nombreJugador: String): Long
    {
        val usuarioExiste = transaction {
            DatosUsuario.slice(DatosUsuario.usuario).select { DatosUsuario.usuario eq nombreJugador}.count()  }

        return usuarioExiste
    }

    /**
     * ## QUE HACE:
     *   Función que realiza una update en la tabla de usuarios, aumentando a 1 el número de partidas jugadas por el usuario
     *   que se indique
     */
    fun updateTablaUsuario(nombreJugador: String)
    {
        transaction {
            DatosUsuario.update ({ DatosUsuario.usuario eq nombreJugador }) {
                with(SqlExpressionBuilder) {
                    it.update(partidasJugadas,partidasJugadas + 1)
                }
            }
        }
    }

    /**
     * ## QUE HACE
     *   Función que es llamada cuando se desea añadir una nueva fila a la tabla de los datos de las partidas jugadas
     */
    fun insertNuevaPartidaJugada(nombreJugador: String,puntuacionPartida: Int, fechaPartida:String)
    {
        transaction {
            ResultadosTrivial.insert {
                it[nomJugador] = nombreJugador
                it[puntuacion] = puntuacionPartida
                it[fecha] = fechaPartida
            }
        }
    }

    /**
     * ## QUE HACE:
     *   Función que es llamada al terminar el programa para mostrar por pantalla la información de la tabla de puntuaciones.
     *   Tiene una variable de formato que la aplica a cada fila que saca para imprimirla en cierto formato.
     */
    fun printTablaPuntuaciones()
    {
        // Las variables de formato nos sirven para darle cierto formato a las tablas cuando se imprimen
        val formatoTablaPuntuaciones = "| %-11s | %-11d | %-27s |%n"

        transaction {
            // Se hace un selectAll de la tabla puntuaciones y se va recorriendo esta para imprimir por pantalla fila por fila
            val filas = ResultadosTrivial.selectAll().orderBy(ResultadosTrivial.puntuacion to SortOrder.DESC)
            filas.forEach {
                System.out.format(formatoTablaPuntuaciones, it[ResultadosTrivial.nomJugador], it[ResultadosTrivial.puntuacion], it[ResultadosTrivial.fecha])
            }
        }
    }

    /**
     * ## QUE HACE:
     *   Función que es llamada al terminar el programa para mostrar por pantalla la información de la tabla de usuarios.
     *   Tiene una variable de formato que la aplica a cada fila que saca para imprimirla en cierto formato.
     */
    fun printTablaUsuarios()
    {
        // Las variables de formato nos sirven para darle cierto formato a las tablas cuando se imprimen
        val formatoTablaUsuarios = "| %-4d | %-13s | %-13s | %-13d |%n"

        transaction {
            val filasUsuarios = DatosUsuario.selectAll().orderBy(DatosUsuario.id)
            // Se hace un selectAll de la tabla usuarios y se va recorriendo esta para imprimir por pantalla fila por fila
            filasUsuarios.forEach {
                System.out.format(formatoTablaUsuarios, it[DatosUsuario.id], it[DatosUsuario.usuario], it[DatosUsuario.contrasenya], it[DatosUsuario.partidasJugadas])
            }
        }
    }

    /**
     * ## QUE HACE:
     *   Función que es llamada cuando se necesita hacer un selectAll de la tabla de resultados
     */
    fun selectALLResultadosTrivial(): MutableList<Resultados>
    {
        val filasCSVResultados = mutableListOf<Resultados>()

        transaction {
            val selectResultados = ResultadosTrivial.selectAll().orderBy(ResultadosTrivial.puntuacion to SortOrder.DESC)
            selectResultados.forEach {
                filasCSVResultados += Resultados(
                    "\'${it[ResultadosTrivial.nomJugador]}\'","\'${it[ResultadosTrivial.puntuacion]}\'",
                    "\'${it[ResultadosTrivial.fecha]}\'"
                )
            }
        }

        return filasCSVResultados
    }

    /**
     * ## QUE HACE:
     *   Función que es llamada cuando se necesitar hacer un selectAll de la tabla de usuarios
     */
    fun selectAllUsuarios(): MutableList<Usuarios>
    {
        val filasCSVUsuarios = mutableListOf<Usuarios>()

        transaction {
            val selectUsuarios = DatosUsuario.selectAll().orderBy(DatosUsuario.id)
            selectUsuarios.forEach {
                filasCSVUsuarios += Usuarios("\'${it[DatosUsuario.id]}\'",
                    "\'${it[DatosUsuario.usuario]}\'", "\'${it[DatosUsuario.contrasenya]}\'", "\'${it[DatosUsuario.partidasJugadas]}\'")
            }
        }

        return filasCSVUsuarios
    }
}