import com.floern.castingcsv.castingCSV
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
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
 * Tabla que guarda los resultados de los usuarios que juegan al trivial
 */
object resultados_trivial : Table() {
    val nomJugador = text("Nombre_Jugador")
    val puntuacion = integer("Puntuación")
    val fecha = text("Fecha")
}

/**
 * Tabla que guarda los datos de los usuarios registrados en el trivial
 */
object datos_usuario : Table() {
    val id = integer("id").autoIncrement()
    val usuario = text("Nombre_Usuario")
    val contrasenya = text("Contraseña")
    val partidasJugadas = integer("Partidas_Jugadas")
    override val primaryKey = PrimaryKey(id, name ="PK_USUARIO_CONTRA")
}

/**
 * ## QUE HACE:
 *   Es la función que se encarga de gestionar el usuario que entra a jugar para saber si se debe crear una nueva entrada
 *   a la tabla de usuarios o debe pedirle contraseña al usuario ya creado
 */
fun gestionUsuarios(): Juego {
    // Se crea un objeto de tipo Juego y se llama a la función para pedir nombre al usuario
    val nuevoJuego = Juego()
    nuevoJuego.pedirNombreUsuario()

    // Se hace una query para saber cuantos usuarios existen en la tabla usuarios que contengan
    // el mismo nombre que el usuario actual
    val usuarioExiste = transaction {
        datos_usuario.slice(datos_usuario.usuario).select { datos_usuario.usuario eq nuevoJuego.nombreJugador}.count()  }

    if(usuarioExiste > 0)
    {
        // En caso de que haya algun usuario con el mismo nombre se hace una query para conocer de antemano
        // la contraseña del usuario y poder comprobar que la contraseña que coloca el nuevo usuario es correcta.
        // En caso de olvido, escribiendo un 0 el programa te imprime la contraseña del usuario
        var loginContra: String
        val contraUsuario = transaction {
            datos_usuario.select { datos_usuario.usuario eq nuevoJuego.nombreJugador}.first()[datos_usuario.contrasenya]
        }
        while(true)
        {
            println("El usuario que has indicado [${nuevoJuego.nombreJugador}] ya existe, escriba la contraseña: ")
            println("Si no recuerda su contraseña escriba un 0")
            print(">>> ")
            loginContra = readLine().toString()
            if(loginContra == "0"){
                println("\nLa contraseña del usuario ${nuevoJuego.nombreJugador} es $contraUsuario")
                println("-------------------------------------\n")
                continue
            } else if(loginContra != contraUsuario) {
                println("\nHas escrito la contraseña de forma equivocada, intentalo de nuevo")
                println("-------------------------------------\n")
                continue
            } else {
                println("\nLogin correcto. Bienvenido de nuevo ${nuevoJuego.nombreJugador}\n")
                println("-------------------------------------\n")
                break
            }
        }
    } else {
        // En caso de que el usuario no exista en la base de datos se le pedira a este que indique una contraseña
        // para su usuario
        var nuevaContrasenya: String
        while(true){
            println("Aún no estas registrado en la base de datos del trivial, escriba una contraseña para guardar sus datos")
            print(">>> ")
            nuevaContrasenya = readLine().toString()
            if(nuevaContrasenya.length <= 1) {
                println("La contraseña debe tener como mínimo 2 caracteres.")
                println("-------------------------------------\n")
                continue
            } else {
                break
            }
        }

        // Se añade el nuevo usuario con su contraseña a la tabla de usuarios
        transaction {
            datos_usuario.insert {
                it[usuario] = nuevoJuego.nombreJugador
                it[contrasenya] = nuevaContrasenya
                it[partidasJugadas] = 0
            }
        }
        println("\n¡Usuario creado correctamente!")
        println("-------------------------------------\n")
    }
    return nuevoJuego
}

/**
 * ## QUE HACE:
 *   Esta función nos sirve para conectarnos a la base de datos, creas las tablas en caso de no estarlo
 *   y llama a las funciones necesarias para crear un objeto juego con el nombre de usuario y las preguntas
 *   escogidas para el trivial
 */
fun comienzoJuego(): Juego {
    Database.connect("jdbc:sqlite:src/main/kotlin/database.db")
    transaction {
        SchemaUtils.create(resultados_trivial)
        SchemaUtils.create(datos_usuario)
    }
    println("-------------------------------------")
    println("Bienvenido/a al juego del trivial\n" +
            "-------------------------------------")
    val juegoActual = gestionUsuarios()
    juegoActual.lecturaPreguntas()
    juegoActual.escogerDiezPreguntas()
    println("-------------------------------------")
    println("Instrucciones")
    println("Para responder escribe el número que corresponda a la respuesta correcta")
    println("-------------------------------------\n")

    return juegoActual
}

/**
 * ## QUE HACE:
 *   Es la función que se encarga de recorrer la lsita de diez preguntas y mostrarlas al usuario y esperar la respuesta
 *   de este para llamar a la función dentro del objeto del juego que se encarga de revisar si es correcta o no, dependiendo
 *   de lo que reciba muesta al usuario si ha respondido bien o no
 */
fun trivial(juegoActual:Juego)
{
    var respuestaJug:String
    for(parte in juegoActual.diezPreguntas)
    {
        while(true)
        {
            println("Pregunta: ¿${parte["pregunta"]}")
            println("[A] -> ${parte["A"]}")
            println("[B] -> ${parte["B"]}")
            println("[C] -> ${parte["C"]}")
            println("[D] -> ${parte["D"]}")
            print(">>>")
            respuestaJug = readLine().toString()
            if(respuestaJug.uppercase() !in listOf("A","B","C","D"))
            {
                println("No has respondido con una de las opciones disponibles, intentalo de nuevo.\n")
                continue
            }else
            {
                if(juegoActual.revisarRespuesta(parte,respuestaJug)) {
                    println("¡Correcto!\nPuntuación -> ${juegoActual.puntuacion}/10")
                    println("-------------------------------------\n")
                    break
                }
                else{
                    println("¡Incorrecto!\n" +
                            "La respuesta correcta era la [${parte["respuesta"]}] -> ${parte["${parte["respuesta"]}"]}\n" +
                            "Puntuación -> ${juegoActual.puntuacion}/10")
                    println("-------------------------------------\n")
                    break
                }

            }
        }
    }
    finDeJuego(juegoActual)
}

/**
 * ## QUE HACE:
 *   Marca el final de una partida, muestra la información de la puntuación del usuario, la inserta en la tabla de puntuaciones y
 *   actualiza el número de partidas jugadas del usuario. Además pregunta al usuario si quiere volver a jugar. Si responde que sí vuelve a comenzar el juego
 *   desde el principio preguntando de nuevo usuario y contraseña. En caso de que no se muestra en pantalla la información de las dos tablas
 *   y construye dos documentos csv con la información de estas
 */
fun finDeJuego(juegoActual:Juego)
{
    var jugarOtraVez: String
    println("-----------RESULTADO FINAL-----------")
    println("Jugador -> ${juegoActual.nombreJugador}")
    println("Puntuación -> ${juegoActual.puntuacion}/10")
    juegoActual.declararFecha()

    // Insertamos la información sobre la última partida en la tabla de resultados
    transaction {
        resultados_trivial.insert {
            it[nomJugador] = juegoActual.nombreJugador
            it[puntuacion] = juegoActual.puntuacion
            it[fecha] = juegoActual.fecha
        }
    }

    // Actualizamos a 1 el número de partidas jugadas por el usuario
    transaction {
        datos_usuario.update ({ datos_usuario.usuario eq juegoActual.nombreJugador }) {
            with(SqlExpressionBuilder) {
                it.update(partidasJugadas,partidasJugadas + 1)
            }
        }
    }

    while(true)
    {
        println("¿Quieres volver a jugar? (S/N)")
        print(">>>")
        jugarOtraVez = readLine().toString()
        if(jugarOtraVez.uppercase() !in arrayOf("S","N"))
        {
            println("Responde S o N")
            continue
        }else {
            if(jugarOtraVez.uppercase() == "S")
                trivial(comienzoJuego())
            else
                break
        }
    }

    // Las variables de formato nos sirven para darle cierto formato a las tablas cuando se imprimen
    val formatoTablaPuntuaciones = "| %-11s | %-11d | %-27s |%n"
    System.out.format("\n+---------------------------------------------------------+%n")
    println("                     TABLA PUNTUACIONES")
    System.out.format("+-------------+-------------+-----------------------------+%n")
    System.out.format("| USUARIO     | PUNTUACION  | FECHA                       |%n")
    System.out.format("+-------------+-------------+-----------------------------+%n")
    transaction {
        val filas = resultados_trivial.selectAll().orderBy(resultados_trivial.puntuacion to SortOrder.DESC)
        filas.forEach {
            System.out.format(formatoTablaPuntuaciones, it[resultados_trivial.nomJugador], it[resultados_trivial.puntuacion], it[resultados_trivial.fecha])
        }
    }
    System.out.format("+---------------------------------------------------------+%n")

    val formatoTablaUsuarios = "| %-4d | %-13s | %-13s | %-13d |%n"
    System.out.format("\n+------------------------------------------------------+%n")
    println("               USUARIOS REGISTRADOS")
    System.out.format("+------+---------------+---------------+---------------+%n")
    System.out.format("| ID   | USUARIO       | CONTRASEÑA    | PART.JUGADAS  |%n")
    System.out.format("+------+---------------+---------------+---------------+%n")
    transaction {
        val filasUsuarios = datos_usuario.selectAll().orderBy(datos_usuario.id)
        filasUsuarios.forEach {
            System.out.format(formatoTablaUsuarios, it[datos_usuario.id], it[datos_usuario.usuario], it[datos_usuario.contrasenya], it[datos_usuario.partidasJugadas])
        }
    }
    System.out.format("+------+---------------+---------------+---------------+%n")

    escrituraCSV()
}

/**
 * ## QUE HACE:
 *   Es una función que es llamada cuando el jugador decide no seguir jugando y lo que hace es crear dos documentos CSV
 *   con los datos obtenidos de las dos tabla de la base de datos que manejamos en este trivial.
 */
fun escrituraCSV()
{
    // Obtenemos el Path de ambos archivos CSV que tendrán la información de nuestras tablas
    val pathUsuarios = Paths.get("src/main/resources/usuarios.csv")
    val pathResultados = Paths.get("src/main/resources/resultados.csv")

    // Si existen estos archivos, son eliminado para evitar que se repita la misma información en los archivos
    Files.deleteIfExists(pathUsuarios)
    Files.deleteIfExists(pathResultados)

    // Se crean de nuevo los dos archivos csv con sendos nombres
    val csvUsuariosFile = File("src/main/resources/usuarios.csv")
    val csvResultadosFile = File("src/main/resources/resultados.csv")
    csvUsuariosFile.createNewFile()
    csvResultadosFile.createNewFile()

    // Usamos la libreria castingCSV para la creación de estos archivos. Recogemos la info de las data class que hemos creado
    // al principio del documento para poder crear una lista de listas en las que añadiremos las distintas filas de la
    // que constarán nuestros CSV
    val transUsuarios = castingCSV().fromCSV<Usuarios>(csvUsuariosFile)
    val transResultados = castingCSV().fromCSV<Resultados>(csvResultadosFile)
    val filasCSVUsuarios = transUsuarios.toMutableList()
    val filasCSVResultados = transResultados.toMutableList()

    // Realizamos una select para recoger toda la información de la tabla de resultados y la recorremos fila a fila para
    // poder añadirla a la lista de listas con la información de resultados que necesitaremos para crear el csv correspondiente
    transaction {
        val selectResultados = resultados_trivial.selectAll().orderBy(resultados_trivial.puntuacion to SortOrder.DESC)
        selectResultados.forEach {
            filasCSVResultados += Resultados(
                "\'${it[resultados_trivial.nomJugador]}\'","\'${it[resultados_trivial.puntuacion]}\'",
                "\'${it[resultados_trivial.fecha]}\'"
            )
        }
    }

    // Realizamos otra select para recoger toda la información de la tabla de usuarios y la recorremos fila a fila para
    // poder añadirla a la lista de listas con la información de usuarios
    transaction {
        val selectUsuarios = datos_usuario.selectAll().orderBy(datos_usuario.id)
        selectUsuarios.forEach {
            filasCSVUsuarios += Usuarios("\'${it[datos_usuario.id]}\'",
                "\'${it[datos_usuario.usuario]}\'", "\'${it[datos_usuario.contrasenya]}\'", "\'${it[datos_usuario.partidasJugadas]}\'")
        }
    }

    // Se crean los ficheros con sus nombres correspondientes recibiendo la información que hemos recogido en las selects anteriores
    castingCSV().toCSV(filasCSVUsuarios,csvUsuariosFile.outputStream())
    castingCSV().toCSV(filasCSVResultados,csvResultadosFile.outputStream())
}

fun main() {
    trivial(comienzoJuego())
}