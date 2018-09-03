package cl.manuel.doodlz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

/**
 * Permite al usuario modificar el grosor de la línea
 */

public class FrgDialGrosorLinea extends DialogFragment{

    /*SALVO ALGUNAS DIFERENCIAS, EL CÓDIGO DE ESTA CLASE ES EL MISMO QUE EL DE FRGDIALCOLOR*/

    private ImageView imagenGrosor;


    /*CREACIÓN Y RETORNO DE ALERTDIALOG*/
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //Configuración de diálogo, mensaje y GUI
        AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
        View lineWidthDialogView = getActivity().getLayoutInflater().
                inflate(R.layout.fragment_grosor_linea, null);
        alerta.setView(lineWidthDialogView);
        alerta.setTitle(R.string.title_line_width_dialog);
        imagenGrosor = lineWidthDialogView.findViewById(R.id.imvGrosorLinea);

        //Configuración de SeekBar
        final DoodleView dv = getDoodleFragment().getDoodleView();
        final SeekBar sbGrosorLinea = lineWidthDialogView.findViewById(R.id.sbGrosorLinea);
        sbGrosorLinea.setOnSeekBarChangeListener(listener);
        sbGrosorLinea.setProgress(dv.obtenerGrosor());

        //Agregar botón de aprobación
        alerta.setPositiveButton(R.string.btn_establecer_ancho_linea,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dv.cambiarGrosor(sbGrosorLinea.getProgress());
                            }
                        });

        return alerta.create();
    }

    //Referencia a FrgMAinActivity
    private FrgMainActivity getDoodleFragment() {
        return (FrgMainActivity)getFragmentManager().findFragmentById(R.id.doodleFragment);
    }

    //Indicar a FrgMainAcitivity que diálogo está desplegado
    @Override
    public void onAttach(Context context) {
            super.onAttach(context);
            FrgMainActivity fragment = getDoodleFragment();

            if (fragment != null){
                fragment.setDialogoEnPantalla(true);
            }
    }

    //Indicar a FrgMainAcitivity que diálogo ya no está desplegado
    @Override
    public void onDetach() {
            super.onDetach();
            FrgMainActivity fragment = getDoodleFragment();

            if (fragment != null){
                fragment.setDialogoEnPantalla(false);
            }
    }


    /*MANEJO DE EVENTOS DE SEEKBAR*/
    private final SeekBar.OnSeekBarChangeListener listener =
            new SeekBar.OnSeekBarChangeListener() {

                final Bitmap bitmap = Bitmap.createBitmap(400,
                        100,
                        Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    //Configurar objeto Paint para valor actual de SeekBar
                    Paint p = new Paint();
                    p.setColor(getDoodleFragment().getDoodleView().obtenerColor());
                    p.setStrokeCap(Paint.Cap.ROUND);
                    p.setStrokeWidth(progress);

                    //Borrar bitmap y redibujar línea
                    bitmap.eraseColor(getResources().getColor(android.R.color.transparent,
                            getContext().getTheme()));
                    canvas.drawLine(30, 50, 370, 50, p);
                    imagenGrosor.setImageBitmap(bitmap);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            };
}
