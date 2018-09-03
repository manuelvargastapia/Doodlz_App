package cl.manuel.doodlz;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * Manejo de eventos de acelerómetro vía despliegue de clase DoodleView y FrgDialogBorrarImagen
 *
 * Manejo de opciones de menú
 */

public class FrgMainActivity extends Fragment {

    /*VARIABLES DE CLASE*/

    private DoodleView dv;
    private float aceleracion;
    private float aceleracionActual;
    private float ultimaAceleracion;
    private boolean dialogoEnPantalla = false; //Evitar solapamiento de diálogos mostrados
    private static final int UMBRAL_ACELERACION = 100000; //Determina si el movimiento es para borrar
    private static final int CODIGO_PERMISO_GUARDAR_IMAGEN = 1; //Identificar petición de guardado


    /*CONSTRUCTOR (vacío)*/
    public FrgMainActivity() {
    }


    /*CONTROL DEL CICLO DE VIDA DE FRAGMENT*/

    //Inicialización de variables al crearse el fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //Inflar GUI
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        //Este fragment tiene items de menú para mostrar
        setHasOptionsMenu(true);

        //Referenciar a DoodleView
        dv = getView().findViewById(R.id.doodleView);

        //Inicializar valores de aceleración
        aceleracion = 0.00f;
        aceleracionActual = SensorManager.GRAVITY_EARTH;
        ultimaAceleracion = SensorManager.GRAVITY_EARTH;

        return v;
    }

    //Comenzar a captar eventos de acelerómetro solo cuando este fragment está visible
    @Override
    public void onResume() {
        super.onResume();

        permitirAcelerometro();
    }

    //Dejar de captar datos del acelerómetro cuando este fragment está inactivo
    @Override
    public void onPause() {
        super.onPause();

        noPermitirAcelerometro();
    }

    //Permite la captación de datos del acelerómetro
    private void permitirAcelerometro(){

        //Obtener objeto SensorManager
        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        //Resgistro de sensor a ser escuchado
        sm.registerListener(sensorEventListener,
                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Detener la captación de datos del acelerómetro
    private void noPermitirAcelerometro(){

        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        sm.unregisterListener(sensorEventListener,
                              sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }



    /*PROCESAMIENTO DE EVENTOS DE ACELERÓMETRO*/

    //Clase anónima encargada de manejo de los eventos
    private final SensorEventListener sensorEventListener = new SensorEventListener() {

        //Determinar si el usuario agitó el dispositivo
        @Override
        public void onSensorChanged(SensorEvent evento) {

            if(!dialogoEnPantalla){ //Verificar que no haya otros diálogos en pantalla

                //Captura de coordenadas del dispositivo vía acelerómetro (mts/seg^2)
                float x = evento.values[0]; //izq-der
                float y = evento.values[1]; //arriba-abajo
                float z = evento.values[2]; //adelante-atrás

                //Guardar último valor de aceleración
                ultimaAceleracion = aceleracionActual;

                //Calcular aceleración actual
                aceleracionActual = x*x + y*y + z*z;

                //Calcular el cambio en la aceleración
                aceleracion = aceleracionActual * (aceleracionActual - ultimaAceleracion);

                if(aceleracion > UMBRAL_ACELERACION){

                    confirmarEliminacion(); //Método que invoca FrgDialBorrarImagen.java
                }
            }
        }

        //Método vacío, debido a que es necesario por la interfaz
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    //Confirmar si la imagen debería ser borrada
    private void confirmarEliminacion(){

        FrgDialBorrarImagen fragment = new FrgDialBorrarImagen();
        fragment.show(getFragmentManager(), "erase dialog");
    }


    /*CONTROL DE LAS OPCIONES DE MENÚ*/

    //Desplegar ítems de menú del fragment
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        //Inflar GUI con menú
        inflater.inflate(R.menu.doodle_fragment_menu, menu);
    }

    //Manejar opciones del menú del usuario
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Switch entre opciones de acuerdo a ids de los ítems
        switch (item.getItemId()){
            case R.id.color:
                FrgDialColor color = new FrgDialColor();
                color.show(getFragmentManager(), "color dialog");
                return true;
            case R.id.grosor_linea:
                FrgDialGrosorLinea grosor = new FrgDialGrosorLinea();
                grosor.show(getFragmentManager(), "line width dialog");
                return true;
            case R.id.borrar:
                confirmarEliminacion();
                return true;
            case R.id.guardar:
                guardarImagen();
                return true;
            case R.id.imprimir:
                dv.imprimirImagen();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Método para guardar imágenenes, invocado por botón del menú
    private void guardarImagen(){

        /*Petición de permisos necesarios para usar el almacenamiento externo, salvo que
        esos permisos ya estén concedidos*/

        //Verificar si la app NO tiene los permisos
        if(getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){

            //Si la app NO cuenta con los permisos necesarios...

            //Mostrar explicación, de ser necesario, de por qué se requieren permisos
            if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){

                //Generar mensaje de explicación de permisos
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setMessage(R.string.permiso_explicacion);

                //Agregar botón "ok" a la alerta
                alerta.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    //Clase anónima que gestiona la petición de permisos
                    @Override
                    public void onClick(DialogInterface dialogo, int i) {

                        //Pedir permisos
                        /*El método recibe un String (permiso) y un int (código). El método despliega
                        * un diálogo donde el usuario debe acceder o denegar los permisos. El sistema
                        * invoca onRequestPermissionResult (callback) para procesar la respuesta.*/
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                           CODIGO_PERMISO_GUARDAR_IMAGEN);
                    }
                });

                //Mostrar diálogo recién creado
                alerta.create().show();

            }else{
                //Si no es necesario explicar, pedir permisos
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        CODIGO_PERMISO_GUARDAR_IMAGEN);
            }

        }else {
            //Si la app ya cuenta con permiso para usar el almacenamiento externo...
            dv.guardarImagen();
        }
    }

    //Método que procesa la elección de permisos del usuario, invocado por guardarImagen()
    //El método es llamado ya sea cuando el usuario permite o deniega permisos
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){

            //Determinar acciones a realizar de acuerdo a la función solicitada
            case CODIGO_PERMISO_GUARDAR_IMAGEN:

                //Si el valor del resultado de la elección es igual a "permiso concedido"...
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    dv.guardarImagen();
                }

                return;
        }
    }


    /*MÉTODOS QUE SERÁN LLAMADOS POR SUBCLASE DIALOGFRAGMENT:
    * getDoodleView servirá para obtener una referencia al DV y DialogFragment pueda establecer
    * color, grosor de línea o limpiar la imagen.
    * setDialogoEnPantalla será llamado para indicar cuándo hay un diálogo en la pantalla*/

    //Obtener DoodleView
    public DoodleView getDoodleView() {
        return dv;
    }

    //Determinar valor de dialogoEnPantalla
    public void setDialogoEnPantalla(boolean visible) {
        dialogoEnPantalla = visible;
    }
}
