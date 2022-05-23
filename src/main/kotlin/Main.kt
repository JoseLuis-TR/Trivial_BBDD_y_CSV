import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

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
    var nuevoJuego = Juego()
    nuevoJuego.pedirNombreUsuario()

    // Se hace una query para saber cuantos usuarios existen en la tabla usuarios que contengan
    // el mismo nombre que el usuario actual
    var usuarioExiste = transaction {
        datos_usuario.slice(datos_usuario.usuario).select { datos_usuario.usuario eq nuevoJuego.nombreJugador}.count()  }

    if(usuarioExiste > 0)
    {
        // En caso de que haya algun usuario con el mismo nombre se hace una query para conocer de antemano
        // la contraseña del usuario y poder comprobar que la contraseña que coloca el nuevo usuario es correcta.
        // En caso de olvido, escribiendo un 0 el programa te imprime la contraseña del usuario
        var loginContra: String
        var contraUsuario = transaction {
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
        var nuevaContrasenya = ""
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
    var juegoActual = gestionUsuarios()
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
    var jugarOtraVez = ""
    println("-----------RESULTADO FINAL-----------")
    println("Jugador -> ${juegoActual.nombreJugador}")
    println("Puntuación -> ${juegoActual.puntuacion}/10")
    juegoActual.declararFecha()

    transaction {
        resultados_trivial.insert {
            it[nomJugador] = juegoActual.nombreJugador
            it[puntuacion] = juegoActual.puntuacion
            it[fecha] = juegoActual.fecha
        }
    }

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

    val formatoTablaPuntuaciones = "| %-11s | %-11d | %-27s |%n"
    System.out.format("\n+---------------------------------------------------------+%n");
    println("                     TABLA PUNTUACIONES")
    System.out.format("+-------------+-------------+-----------------------------+%n");
    System.out.format("| USUARIO     | PUNTUACION  | FECHA                       |%n");
    System.out.format("+-------------+-------------+-----------------------------+%n");
    transaction {
        val filas = resultados_trivial.selectAll().orderBy(resultados_trivial.puntuacion to SortOrder.DESC)
        filas.forEach {
            System.out.format(formatoTablaPuntuaciones, it[resultados_trivial.nomJugador], it[resultados_trivial.puntuacion], it[resultados_trivial.fecha])
        }
    }
    System.out.format("+---------------------------------------------------------+%n");

    val formatoTablaUsuarios = "| %-4d | %-13s | %-13s | %-13d |%n"
    System.out.format("\n+------------------------------------------------------+%n");
    println("               USUARIOS REGISTRADOS")
    System.out.format("+------+---------------+---------------+---------------+%n");
    System.out.format("| ID   | USUARIO       | CONTRASEÑA    | PART.JUGADAS  |%n");
    System.out.format("+------+---------------+---------------+---------------+%n");
    transaction {
        val filasUsuarios = datos_usuario.selectAll().orderBy(datos_usuario.id)
        filasUsuarios.forEach {
            System.out.format(formatoTablaUsuarios, it[datos_usuario.id], it[datos_usuario.usuario], it[datos_usuario.contrasenya], it[datos_usuario.partidasJugadas])
        }
    }
    System.out.format("+------+---------------+---------------+---------------+%n");
}

fun main(args: Array<String>) {
    trivial(comienzoJuego())
}