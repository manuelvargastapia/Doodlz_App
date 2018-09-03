package cl.manuel.doodlz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.print.PrintHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Procesamiento de eventos de toque del usuario y dibujo de líneas
 */

public class DoodleView extends View{

    /*VARIABLES DE CLASE*/
    private static final float UMBRAL_TOQUE = 10; //Determinar si el usuario movió lo suficiente el dedo
    private Bitmap bitmap; //Área de dibujo
    private Canvas bitmapCanvas; //Se usa para dibujar en el bitmap
    private final Paint pantalla; //Se usa para situar el bitmap en el pantalla
    private final Paint linea; //Usado para dibujar líneas en el bitmap
    private final Map<Integer, Path> pathMap = new HashMap<>(); //Mapa de paths actuales
    private final Map<Integer, Point> pointMapAnterior = new HashMap<>(); //Mapa de puntos actuales


    /*CONSTRUCTOR*/
    public DoodleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs); //Pasar contexto a constructor de View

        pantalla = new Paint(); //Objeto Paint que despliega bitmap en pantalla

        //Establecer la configuración de despliegue inicial para líneas pintadas
        linea = new Paint();
        linea.setAntiAlias(true); //Border suaves
        linea.setColor(Color.BLACK); //Color defecto
        linea.setStyle(Paint.Style.STROKE); //Línea sólida
        linea.setStrokeWidth(5); //Grosor defecto
        linea.setStrokeCap(Paint.Cap.ROUND); //Final de línea redondeado
    }


    /*CREACIÓN DE BITMAP Y CANVAS DE ACUERDO AL TAMAÑO DE VIEW*/
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        /*Creación de bitmap vía método estático de Bitmap. Se pasan como argumentos el ancho y
        * alto de esta View (DoodleView), y un código que especifica cómo se almacenarán los
        * pixeles del bitmap. En este caso, cada pixel será almacenado en 4 bytes (alfa, rojo,
        * verde y azul).*/
        bitmap = Bitmap.createBitmap(getWidth(),
                                     getHeight(),
                                     Bitmap.Config.ARGB_8888);

        bitmapCanvas = new Canvas(bitmap); //Creación de objeto Canvas

        bitmap.eraseColor(Color.WHITE); /*Borrar bitmap con blanco (pintarlo de blanco, ya que su
                                          color por defecto es negro*/
    }


    /*MÉTODOS LLAMADOS DESDE FRGMAINACTIVITY*/

    //Limpiar dibujo
    public void limpiar(){
        pathMap.clear(); //Remueve todos los paths
        pointMapAnterior.clear(); //Remueve todos los puntos anteriores
        bitmap.eraseColor(Color.WHITE); //Limpiar bitmap
        invalidate(); //Refrescar pantalla (reconstruir DoodleView)
    }

    //Definir color de línea
    public void cambiarColor(int color){
        linea.setColor(color);
    }

    //Obtener color de línea
    public int obtenerColor(){
        return linea.getColor();
    }

    //Definir grosor de línea
    public void cambiarGrosor(int grosor){
        linea.setStrokeWidth(grosor);
    }

    //Obtener grosor de línea
    public int obtenerGrosor(){
        return (int)linea.getStrokeWidth();
    }


    /*REALIZACIÓN DEL DIBUJO CUANDO DOODLEVIEW ES REFRESCADA*/
    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawBitmap(bitmap, 0, 0, pantalla); //Dibujar fondo

        for(Integer clave : pathMap.keySet()){ //Por cada path siendo dibujado...
            canvas.drawPath(pathMap.get(clave), linea); //... dibujar una línea
        }
    }


    /*MANEJO DE EVENTOS DE TOQUE*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked(); //Obtener tipo de evento en formato int
        int actionIndex = event.getActionIndex(); /*Index que almacena info del puntero (dedo),
                                                    como su ID, que puede obtenerse con
                                                    getPointerID(index)*/

        //Determinar si el toque comenzó, terminó o hay desplazamiento
        if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN){

            //El usuario tocó la pantalla con un nuevo dedo

            inicioToque(event.getX(actionIndex),
                        event.getY(actionIndex),
                        event.getPointerId(actionIndex));

        }else if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){

            //El usuario removió un dedo de la pantalla

            finToque(event.getPointerId(actionIndex));

        }else {

            movimientoToque(event);
        }

        invalidate(); //Redibujar DoodleView

        return true;
    }


    /*EVENTOS DE TOQUE: DIBUJAR DE ACUERDO A TOQUES DEL USUARIO*/

    //El usuario toca la pantalla
    private void inicioToque(float x, float y, int ID){

        Path path; //Almacenamiento del path del ID de toque
        Point point; //Almacenamiento del último punto en el path

        //Si ya hay una path asociado al ID
        if(pathMap.containsKey(ID)){
            path = pathMap.get(ID); //obtener el path
            path.reset(); //Resetear el path, ya que ha comenzado un nuevo toque
            point = pointMapAnterior.get(ID); //obtener último punto del path

        }else { //Si no existe un path previo
            path = new Path();
            pathMap.put(ID, path); //Agregar path al pathMap
            point = new Point(); //Crear un nuevo punto
            pointMapAnterior.put(ID, point); //Agregar el punto al mapa
        }

        path.moveTo(x, y);
        point.x = (int)x;
        point.y = (int)y;
    }

    //El usuario arrastra su dedo por la pantalla
    private void movimientoToque(MotionEvent event){

        //Por cada uno de los punteros en objeto MotionEvent
        for(int i = 0; i < event.getPointerCount(); i++){

            //Obtener ID e index del puntero
            int ID = event.getPointerId(i);
            int index = event.findPointerIndex(ID);

            //Si hay un path asociado al puntero
            if(pathMap.containsKey(ID)){

                //Obtener las nuevas coordenadas para el puntero
                float nuevaX = event.getX(index);
                float nuevaY = event.getY(index);

                //Obtener path y puntos previos del puntero
                Path path = pathMap.get(ID);
                Point punto = pointMapAnterior.get(ID);

                //Calcular cuán lejor el usuario se movió desde últia actualización
                float deltaX = Math.abs(nuevaX - punto.x);
                float deltaY = Math.abs(nuevaY - punto.y);

                //Si la distancia es suficientemente significativa
                if(deltaX >= UMBRAL_TOQUE || deltaY >= UMBRAL_TOQUE){

                    //Mover path a nueva locación
                    path.quadTo(punto.x,
                                punto.y,
                                (nuevaX + punto.x) / 2,
                                (nuevaY + punto.y) / 2);
                }

                //Almacenar nuevas coordenadas
                punto.x = (int)nuevaX;
                punto.y = (int)nuevaY;
            }
        }
    }

    //El usuario levanta el dedo de la pantalla
    private void finToque(int ID){
        Path path = pathMap.get(ID); //Obtener correspondiente path
        bitmapCanvas.drawPath(path, linea); //Dibuja en bitmapCanvas
        path.reset(); //Resetear path
    }


    /*GUARDAR IMÁGENES*/
    public void guardarImagen(){

        //Usar "Doodlz" seguido de la hora como nombre de la imagen
        final String nombre = "Doodlz" + System.currentTimeMillis() + ".jpg";

        //Insertar imagen en dispositivo
        String direccion = MediaStore.Images.Media.insertImage(
                                    getContext().getContentResolver(),
                                    bitmap,
                                    nombre,
                                    "Dibujo hecho en Diidlz");

        //Mensaje de éxito si la dirección existe
        if(direccion != null){
            Toast mensaje = Toast.makeText(getContext(),
                                           R.string.mensaje_guardado,
                                           Toast.LENGTH_SHORT);
            mensaje.setGravity(Gravity.CENTER,
                               mensaje.getXOffset() / 2,
                               mensaje.getYOffset() / 2);
            mensaje.show();

        }else {//Desplegar mensaje que indica que hubo un error al guardar

            Toast mensaje = Toast.makeText(getContext(),
                                           R.string.mensaje_error_guardar,
                                           Toast.LENGTH_SHORT);
            mensaje.setGravity(Gravity.CENTER,
                               mensaje.getXOffset() / 2,
                               mensaje.getYOffset() / 2);
            mensaje.show();
        }
    }


    /*IMPRIMIR IMÁGENES*/
    public void imprimirImagen(){

        //Usando clase PrintHelper para imprimir imagen (Android Support Library)
        if(PrintHelper.systemSupportsPrint()){ //Si el sistema soporta impresión (>=Android 4.4)

            PrintHelper printHelper = new PrintHelper(getContext());
            printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT); //Escalar imagen al papel
            printHelper.printBitmap("Dibujo de Doodlz", bitmap); //Imprimir

        }else { //Desplegar mensaje indicando que el sistema no puede imprimir

            Toast mensaje = Toast.makeText(getContext(),
                                           R.string.mensaje_error_imprimir,
                                           Toast.LENGTH_SHORT);

            mensaje.setGravity(Gravity.CENTER,
                               mensaje.getXOffset() / 2,
                               mensaje.getYOffset() / 2);
            mensaje.show();
        }
    }
}
