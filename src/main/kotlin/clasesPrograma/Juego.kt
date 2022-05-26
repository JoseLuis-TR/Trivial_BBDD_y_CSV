package clasesPrograma
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Clase que nos sirve para controlar ciertos aspectos del juego como elegir nombre de usuario, recoger la fecha, crear la
 * lista de preguntas para cada juego y calcular la puntuación
 */
class Juego {
    var nombreJugador = ""
    var fecha = ""
    var listaPreguntasTotales = arrayListOf<Map<String,String>>()
    var diezPreguntas = arrayListOf<Map<String,String>>()
    var puntuacion = 0

    /**
     * ## QUE HACE:
     *  Es llamada para hacer el proceso de pedir el nombre al usuario y lo asigna a la clase
     */
    fun pedirNombreUsuario(){
        while(true)
        {
            println("Escriba el nombre del jugador")
            println("(No puede superar los 10 caracteres)")
            print(">>> ")
            nombreJugador = readLine().toString().uppercase()
            if(nombreJugador.length > 10)
            {
                // Nos aseguramos que el nombre de usuario no supere los 10 caracteres
                println("Recuerda: no puedes superar los 10 caracteres\n" +
                        "-------------------------------------\n")
                continue
            } else {
                println("-------------------------------------\n")
                break
            }
        }
    }

    /**
     * ## QUE HACE:
     *  Es llamada para obtener la fecha actual
     */
    fun declararFecha(){
        // Recoge la fecha actual y se usa un formato especifico para guardarla
        val actual = LocalDateTime.now()
        val formato = DateTimeFormatter.ofPattern("dd-MMMM-yyyy // HH:mm:ss")
        fecha = actual.format(formato)
    }

    /**
     * ## QUE HACE:
     *   Se encarga de leer el documento con las preguntas del trivial, creando una lista de diccionarios donde cada diccionario
     *   contiene la pregunta, las posibles respuestas y la respuesta correcta.
     */
    fun lecturaPreguntas()
    {
        // Se lee el documento con las preguntas del trivial
        val archivoPreguntas = File("src/main/resources/preguntas.trivial").readLines()
        // El contador nos ayuda a saber en que linea nos encontramos de cada pregunta
        var contador = 0
        var diccionario = mutableMapOf<String,String>()
        for(linea in archivoPreguntas) {
            // Gracias al contador sabemos que información contiene cada linea
            when(contador){
                0 -> diccionario["pregunta"] = linea
                1 -> diccionario["respuesta"] = linea
                2 -> diccionario["A"] = linea
                3 -> diccionario["B"] = linea
                4 -> diccionario["C"] = linea
                5 -> diccionario["D"] = linea
            }
            if(contador == 5)
            {
                // Cuando el contador llega a 5 sabremos que hemos leido una pregunta con sus opciones y la respuesta correcta
                // asi que añadimos el diccionario de la pregunta dentro de la lista de preguntas totales y reiniciamos el contador
                listaPreguntasTotales.add(diccionario)
                contador = 0
                diccionario = mutableMapOf()
            }
            else
            {
                contador += 1
            }
        }
    }

    /**
     * ## QUE HACE:
     *   Con la lista de diccionarios que se crea en la función anterior hace una selección de 10 preguntas evitando
     *   repetirlas
     */
    fun escogerDiezPreguntas()
    {
        // Mientras que no se tengan 10 preguntas se sigue en el loop
        while(diezPreguntas.size < 10) {
            val preguntaEscogida = listaPreguntasTotales.random()
            // Se selecciona una pregunta al azar de la lista con todas las preguntas
            if(preguntaEscogida in diezPreguntas)
                // Si la pregunta esta repetida sigue el loop sin hacer nada
                continue
            else
                // Si la pregunta no esta repetida la añade a la variable que contiene las 10 preguntas escogidas
                diezPreguntas.add(preguntaEscogida)
        }
    }

    /**
     * ## QUE HACE:
     *   Se encarga de revisar el diccionario con la pregunta que haya sido realizada al usuario y compara la respuesta
     *   correcta con la respuesta que haya mandado el usuario para indicar si es correcta o incorrecta
     */
    fun revisarRespuesta(pregunta:Map<String,String>,respuestaJug:String): Boolean
    {
        if(respuestaJug.uppercase() == pregunta["respuesta"])
        {
            // Si la respuesta es correcta añade 1 punto a la puntuación total de la partida
            puntuacion += 1
            return true
        }
        else
            // En caso de respuesta incorrecta simplemente devuelve false
            return false
    }
}