package cl.manuel.doodlz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.SeekBar;

/**
 * Permite al usuario establecer el color para dibujar
 */

public class FrgDialColor extends DialogFragment{

    private SeekBar sbAlfa;
    private SeekBar sbRojo;
    private SeekBar sbVerde;
    private SeekBar sbAzul;
    private View colorView;
    private int color;


    /*CREACIÓN DE ALERTDIALOG PARA GESTIONAR SELECCIÓN DEL COLOR*/
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity()); //Creación alerta
        View dialColor = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_color, null); //obtener GUI
        alerta.setView(dialColor); //Agregar GUI a alerta
        alerta.setTitle(R.string.title_color_dialog); //Mensaje de alerta

        //Obtener referencias a SeekBars
        sbAlfa = dialColor.findViewById(R.id.sbAlfa);
        sbRojo = dialColor.findViewById(R.id.sbRojo);
        sbVerde = dialColor.findViewById(R.id.sbVerde);
        sbAzul = dialColor.findViewById(R.id.sbAzul);
        colorView = dialColor.findViewById(R.id.colorView);

        //Registrar event listeners
        sbAlfa.setOnSeekBarChangeListener(listener);
        sbRojo.setOnSeekBarChangeListener(listener);
        sbVerde.setOnSeekBarChangeListener(listener);
        sbAzul.setOnSeekBarChangeListener(listener);

        //Usar color actual en SeekBars
        final DoodleView dv = getDoodleFragment().getDoodleView();
        color = dv.obtenerColor();
        sbAlfa.setProgress(Color.alpha(color));
        sbRojo.setProgress(Color.red(color));
        sbVerde.setProgress(Color.green(color));
        sbAzul.setProgress(Color.blue(color));

        //Agregar botón de establecer color
        alerta.setPositiveButton(R.string.btn_establecer_color,
                                 new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dv.cambiarColor(color);
                                    }
                                 });

        return alerta.create(); //Retornar diálogo
    }


    /*CLASE INTERNA ANÓNIMA PARA EL MANEJO DE EVENTOS DE SEEKBAR*/
    private final SeekBar.OnSeekBarChangeListener listener =
                        new SeekBar.OnSeekBarChangeListener() {

                            @Override //Desplegar color actualizado
                            public void onProgressChanged(SeekBar seekBar, int progreso, boolean usuario) {

                                if(usuario){ //Si es el usuario el que cambia el color (no el programa)
                                    color = Color.argb(sbAlfa.getProgress(),
                                                       sbRojo.getProgress(),
                                                       sbVerde.getProgress(),
                                                       sbAzul.getProgress());
                                }

                                colorView.setBackgroundColor(color);
                            }

                            @Override //Requerido por interfaz
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override //Requerido por interfaz
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        };


    /*OBTENER REFERENCIA A FRGMAINACTIVITY (usado en onCreateDialog())*/
    private FrgMainActivity getDoodleFragment(){
        return (FrgMainActivity) getFragmentManager().findFragmentById(R.id.doodleFragment);
    }


    /*CONTROL DEL CICLO DE VIDA*/

    /*Indicar a FrgMainActivity que el diálogo está desplegado cuando este fragmet es ligado
    a su Activity padre*/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        FrgMainActivity fragment = getDoodleFragment();

        if(fragment != null){
            fragment.setDialogoEnPantalla(true);
        }
    }

    /*Indica a FrgMainActivity que el diálogo ya no está depslegado, al desligarse de su padre*/
    @Override
    public void onDetach() {
        super.onDetach();

        FrgMainActivity fragment = getDoodleFragment();

        if (fragment != null) {
            fragment.setDialogoEnPantalla(false);
        }
    }
}
