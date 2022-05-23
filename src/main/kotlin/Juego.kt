
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
        var actual = LocalDateTime.now()
        var formato = DateTimeFormatter.ofPattern("dd-MMMM-yyyy // HH:mm:ss")
        fecha = actual.format(formato)
    }

    /**
     * ## QUE HACE:
     *   Se encarga de leer el documento con las preguntas del trivial, creando una lista de diccionarios donde cada diccionario
     *   contiene la pregunta, las posibles respuestas y la respuesta correcta.
     */
    fun lecturaPreguntas()
    {
        val archivoPreguntas = File("src/main/resources/preguntas.trivial").readLines()
        var contador = 0
        var diccionario = mutableMapOf<String,String>()
        for(linea in archivoPreguntas) {
            when(contador){
                0 -> diccionario.put("pregunta",linea)
                1 -> diccionario.put("respuesta",linea)
                2 -> diccionario.put("A",linea)
                3 -> diccionario.put("B",linea)
                4 -> diccionario.put("C",linea)
                5 -> diccionario.put("D",linea)
            }
            if(contador == 5)
            {
                listaPreguntasTotales.add(diccionario)
                contador = 0
                diccionario = mutableMapOf<String,String>()
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
        while(diezPreguntas.size < 10) {
            var preguntaEscogida = listaPreguntasTotales.random()
            if(preguntaEscogida in diezPreguntas)
                continue
            else
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
            puntuacion += 1
            return true
        }
        else
            return false
    }
}