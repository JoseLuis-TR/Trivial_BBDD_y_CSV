import clasesPrograma.*

/**
 * ## QUE HACE:
 *   Esta función nos sirve para conectarnos a la base de datos, creas las tablas en caso de no estarlo
 *   y llama a las funciones necesarias para crear un objeto juego con el nombre de usuario y las preguntas
 *   escogidas para el trivial
 */
fun comienzoJuego() {
    // Se crea un objeto base de datos y se realiza la conexión a esta
    val baseDeDatos = TablasBBDD()
    baseDeDatos.conexionBaseDeDatos()

    println("-------------------------------------")
    println("Bienvenido/a al juego del trivial\n" +
            "-------------------------------------")
    // Se llama a la función de gestión de usuarios que se encargar de revisar si el usuario que quiere usar el jugador
    // ya existe (se le pedira contraseña) o no existe (debera añadir una contraseña)
    val juegoActual = gestionUsuarios(baseDeDatos)

    // Se lee el archivo de preguntas del trivial y se escogen 10 de manera aleatoria
    juegoActual.lecturaPreguntas()
    juegoActual.escogerDiezPreguntas()
    println("-------------------------------------")
    println("Instrucciones")
    println("Para responder escribe el número que corresponda a la respuesta correcta")
    println("-------------------------------------\n")

    // Se comienza el juego
    trivial(juegoActual,baseDeDatos)
}

/**
 * ## QUE HACE:
 *   Es la función que se encarga de gestionar el usuario que entra a jugar para saber si se debe crear una nueva entrada
 *   a la tabla de usuarios o debe pedirle contraseña al usuario ya creado llamando a las funciones necesarias.
 */
fun gestionUsuarios(baseDeDatos:TablasBBDD): Juego {
    // Se crea un objeto de tipo Clases.Juego y se llama a la función para pedir nombre al usuario
    val nuevoJuego = Juego()
    nuevoJuego.pedirNombreUsuario()

    // Se realiza una comprobación llamando al objeto base de datos, que hara una select de los usuarios y lo contará en caso de que el
    // usuario ya exista
    val usuarioExiste = baseDeDatos.comprobarUsuarioExiste(nuevoJuego.nombreJugador)

    if(usuarioExiste > 0) {
        // Si existe el usuario, se pide login
        realizarLogin(nuevoJuego, baseDeDatos)
    } else {
        // Si no existe, se procede a crear en la tabla de usuarios
        anyadirNuevoUsuario(nuevoJuego, baseDeDatos)
    }
    return nuevoJuego
}

/**
 * ## QUE HACE:
 *   Esta función es llamada cuando la función gestionUsuario() descubre que el usuario que va a jugar no esta registrado
 *   por lo que necesita pedirle al usuario una contraseña para guardarlo en la base de datos
 */
fun anyadirNuevoUsuario(nuevoJuego: Juego, baseDeDatos: TablasBBDD)
{
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

    // Cuando se conozca la contraseña que el usuario nuevo quiere utilizar, se hará una inserción en la tabla de usuaurios
    // donde se añadira el usuario y la contraseña elegida
    baseDeDatos.insertNuevoUsuario(nuevoJuego.nombreJugador,nuevaContrasenya)
    println("\n¡Usuario creado correctamente!")
    println("-------------------------------------\n")
}

/**
 * ## QUE HACE:
 *   Esta función es llamada cuando la función gestionUsuario() descubre que el usuario que va a jugar ya tiene una cuenta
 *   y necesita que este inicie sesión
 */
fun realizarLogin(nuevoJuego: Juego, baseDeDatos: TablasBBDD)
{
    // En caso de que haya algun usuario con el mismo nombre se hace una query para conocer de antemano
    // la contraseña del usuario y poder comprobar que la contraseña que coloca el nuevo usuario es correcta.
    // En caso de olvido, escribiendo un 0 el programa te imprime la contraseña del usuario
    var loginContra: String

    // Se hace una select de la contraseña del usuario que quiere iniciar sesión para poder verificar el login o mostrarsela
    // al usuario en caso de olvido
    val contraUsuario = baseDeDatos.recuperarContrasenyaUsuario(nuevoJuego.nombreJugador)
    while(true)
    {
        println("El usuario que has indicado [${nuevoJuego.nombreJugador}] ya existe, escriba la contraseña: ")
        println("Si no recuerda su contraseña escriba un 0")
        print(">>> ")
        loginContra = readLine().toString()
        if(loginContra == "0"){
            // Si el usuario escribe 0 se le recordará su contraseña
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
}

/**
 * ## QUE HACE:
 *   Es la función que se encarga de recorrer la lsita de diez preguntas y mostrarlas al usuario y esperar la respuesta
 *   de este para llamar a la función dentro del objeto del juego que se encarga de revisar si es correcta o no, dependiendo
 *   de lo que reciba muesta al usuario si ha respondido bien o no
 */
fun trivial(juegoActual: Juego, baseDeDatos: TablasBBDD)
{
    var respuestaJug:String

    // Se recorre la lista de diccionarios con las 10 preguntas
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

            // Se muestra la pregunta y sus opciones y se espera la respuesta del usuario
            respuestaJug = readLine().toString()
            if(respuestaJug.uppercase() !in listOf("A","B","C","D"))
            {
                // Se controla que el usuario no escriba una opción no permitida
                println("No has respondido con una de las opciones disponibles, intentalo de nuevo.\n")
                continue
            }else
            {
                // Si el usuario contesta con una de las opciones posibles se hace una revisión de la respuesta dada
                if(juegoActual.revisarRespuesta(parte,respuestaJug)) {
                    // En caso de responder correctamente, se añade un punto a la puntuación total
                    println("¡Correcto!" +
                            "\nPuntuación -> ${juegoActual.puntuacion}/10")
                    println("-------------------------------------\n")
                    break
                }
                else{
                    // En caso de responder de forma incorrecta simplemente se muestra la respuesta correcta y se sigue con la siguiente pregunta
                    println("¡Incorrecto!\n" +
                            "La respuesta correcta era la [${parte["respuesta"]}] -> ${parte["${parte["respuesta"]}"]}\n" +
                            "Puntuación -> ${juegoActual.puntuacion}/10")
                    println("-------------------------------------\n")
                    break
                }

            }
        }
    }

    // Cuando se acaban las preguntas se llama a la función que se encargará de finalizar el juego
    finDeJuego(juegoActual, baseDeDatos)
}

/**
 * ## QUE HACE:
 *   Marca el final de una partida, muestra la información de la puntuación del usuario, la inserta en la tabla de puntuaciones y
 *   actualiza el número de partidas jugadas del usuario. Además pregunta al usuario si quiere volver a jugar. Si responde que sí vuelve a comenzar el juego
 *   desde el principio preguntando de nuevo usuario y contraseña. En caso de que no se muestra en pantalla la información de las dos tablas
 *   y construye dos documentos csv con la información de estas
 */
fun finDeJuego(juegoActual: Juego, baseDeDatos: TablasBBDD)
{
    // Se le muestra al usuario el resultado final y se llama al objeto juegoActual para obtener la fecha de finalización
    // que necesitaremos para hacer el insert en la tabla resultado
    println("-----------RESULTADO FINAL-----------")
    println("Jugador -> ${juegoActual.nombreJugador}")
    println("Puntuación -> ${juegoActual.puntuacion}/10")
    juegoActual.declararFecha()

    // Insertamos la información sobre la última partida en la tabla de resultados
    baseDeDatos.insertNuevaPartidaJugada(juegoActual.nombreJugador,juegoActual.puntuacion,juegoActual.fecha)

    // Actualizamos a 1 el número de partidas jugadas por el usuario
    baseDeDatos.updateTablaUsuario(juegoActual.nombreJugador)

    // Se le pregunta al usuario si quiere jugar de nuevo al trivial o quiere dejar de jugar
    var jugarOtraVez: String
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
                // En caso de querer repetir se llama de nuevo a la función que comienza el juego
                comienzoJuego()
            else
                // En caso de querer terminar se sigue en la función de fin de juego
                break
        }
    }

    // Se muestra por pantalla la información que hay hasta ahora en la tabla de puntuaciones
    System.out.format("\n+---------------------------------------------------------+%n")
    println("                     TABLA PUNTUACIONES")
    System.out.format("+-------------+-------------+-----------------------------+%n")
    System.out.format("| USUARIO     | PUNTUACION  | FECHA                       |%n")
    System.out.format("+-------------+-------------+-----------------------------+%n")
    // Se llama a la función printTablas para que muestre la información correspondiente a la tabla puntuaciones
    baseDeDatos.printTablaPuntuaciones()
    System.out.format("+---------------------------------------------------------+%n")

    // Se muestra por pantalla la información que hay hasta ahora en la tabla de usuarios
    System.out.format("\n+------------------------------------------------------+%n")
    println("               USUARIOS REGISTRADOS")
    System.out.format("+------+---------------+---------------+---------------+%n")
    System.out.format("| ID   | USUARIO       | CONTRASEÑA    | PART.JUGADAS  |%n")
    System.out.format("+------+---------------+---------------+---------------+%n")
    // Se llama a la función printTablas para que muestre la información correspondiente a la tabla usuarios
    baseDeDatos.printTablaUsuarios()
    System.out.format("+------+---------------+---------------+---------------+%n")

    // Se crea un objeto CSVWriter que se encarga de realizar el proceso de escritura de los archivos CSV con la información
    // correspondiente de las tablas
    csvWriter(baseDeDatos,"usuarios.csv","resultados.csv")
}

fun main() {
    comienzoJuego()
}