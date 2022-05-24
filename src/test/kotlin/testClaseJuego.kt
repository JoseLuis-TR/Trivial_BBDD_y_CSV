import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class testClaseJuego {

    val nuevoJuego = Juego()

    // Comprobamos que al crear el objeto juego lee correctamente el archivo de preguntas y que escoge
    // 10 preguntas para el juego
    @Test
    internal fun testEscogerDiezPreguntas()
    {
        nuevoJuego.lecturaPreguntas()
        nuevoJuego.escogerDiezPreguntas()
        assertEquals(10,nuevoJuego.diezPreguntas.size)
    }

    // Comprobamos que la función reconoce cuando una respuesta es correcta y cuando no
    @Test
    internal fun testRevisarRespuesta()
    {
        val pregunta = mapOf("respuesta" to "A")
        assertEquals(true,nuevoJuego.revisarRespuesta(pregunta,"A"))
        assertEquals(false,nuevoJuego.revisarRespuesta(pregunta,"B"))
    }
}