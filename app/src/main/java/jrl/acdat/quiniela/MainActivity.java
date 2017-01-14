package jrl.acdat.quiniela;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    // Servidor de quinielista.es
    public static final String RUTAQUINIELISTA = "http://www.quinielista.es/xml/temporada.asp";

    // Servidor de casa
    public static final String RUTASERVIDOR = "http://192.168.1.5/curso1617/quiniela/";

    public static final String RESULTADOS = "resultados";
    public static final String APUESTAS = "apuestas.txt";
    public static final String PREMIOS = "premios";
    public static final String EXTENSIONXML = ".xml";
    public static final String EXTENSIONJSON = ".json";

    public static final String UTF8 = "utf-8";

    RadioButton rdbXml, rdbJson;
    TextView txvResultados, txvApuestas, txvAciertosYPremios;
    EditText edtResultados, edtApuestas, edtAciertosYPremios;
    Button btnCalcular;

    boolean esJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rdbXml = (RadioButton)findViewById(R.id.rdbXml);
        rdbXml.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    edtResultados.setText(RUTASERVIDOR + RESULTADOS + EXTENSIONXML);
                    edtAciertosYPremios.setText(RUTASERVIDOR + PREMIOS + EXTENSIONXML);
                    esJson = false;
                }
            }
        });
        rdbJson = (RadioButton)findViewById(R.id.rdbJson);
        rdbJson.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    edtResultados.setText(RUTASERVIDOR + RESULTADOS + EXTENSIONJSON);
                    edtAciertosYPremios.setText(RUTASERVIDOR + PREMIOS + EXTENSIONJSON);
                    esJson = true;
                }
            }
        });
        txvResultados = (TextView)findViewById(R.id.txvResultados);
        edtResultados = (EditText)findViewById(R.id.edtResultados);
        txvApuestas = (TextView)findViewById(R.id.txvApuestas);
        edtApuestas = (EditText)findViewById(R.id.edtApuestas);
        txvAciertosYPremios = (TextView)findViewById(R.id.txvAciertosYPremios);
        edtAciertosYPremios = (EditText)findViewById(R.id.edtAciertosYPremios);
        btnCalcular = (Button)findViewById(R.id.btnCalcular);
        btnCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                descargarResultados();
                descargarApuestas();
            }
        });

        edtResultados.setText(RUTASERVIDOR + RESULTADOS + EXTENSIONJSON);
        edtApuestas.setText(RUTASERVIDOR + APUESTAS);
        edtAciertosYPremios.setText(RUTASERVIDOR + PREMIOS + EXTENSIONJSON);
        esJson = true;
    }

    private Resultado leer(File fichero, String codigo) {
        FileInputStream fis = null;
        InputStreamReader isw = null;
        BufferedReader in = null;
        StringBuilder miCadena = new StringBuilder();
        Resultado resultado = new Resultado();
        int n;
        resultado.setCodigo(true);
        try {
            fis = new FileInputStream(fichero);
            isw = new InputStreamReader(fis, codigo);
            in = new BufferedReader(isw);
            while ((n = in.read()) != -1)
                miCadena.append((char) n);
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
            resultado.setCodigo(false);
            resultado.setMensaje(e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                    resultado.setContenido(miCadena.toString());
                }
            } catch (IOException e) {
                Log.e("Error al cerrar", e.getMessage());
                resultado.setCodigo(false);
                resultado.setMensaje(e.getMessage());
            }
        }
        return resultado;
    }

    private void descargarResultados() {
        final ProgressDialog progreso = new ProgressDialog(this);
        String url = String.valueOf(edtResultados.getText());
        if(!url.equals("")) {
            RestClient.get(url, new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progreso.setMessage("Conectando . . .");
                    progreso.setCancelable(false);
                    progreso.show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    progreso.dismiss();
                    try {
                        JSONObject responseJSON = new JSONObject(responseString);
                        ArrayList<Quiniela> quinielas = Analisis.obtenerResultados(responseJSON);
                        for(int i = 0; i < quinielas.size(); i++) {
                            Toast.makeText(MainActivity.this, quinielas.get(i).toString(), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    progreso.dismiss();
                    String mensaje = "El fichero \"" + RESULTADOS + (esJson ? EXTENSIONJSON : EXTENSIONXML) + "\" no se ha descargado";
                    Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            String mensaje = "La ruta del fichero \"" + RESULTADOS + (esJson ? EXTENSIONJSON : EXTENSIONXML) + "\" esta vacia";
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        }
    }

    private void descargarResultadosJSON() {
        final ProgressDialog progreso = new ProgressDialog(this);
        String url = String.valueOf(edtResultados.getText());
        if(!url.equals("")) {
            RestClient.get(url, new JsonHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progreso.setMessage("Conectando . . .");
                    progreso.setCancelable(false);
                    progreso.show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    progreso.dismiss();

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    progreso.dismiss();
                    String mensaje = "El fichero \"" + RESULTADOS + (esJson ? EXTENSIONJSON : EXTENSIONXML) + "\" no se ha descargado";
                    Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            String mensaje = "La ruta del fichero \"" + RESULTADOS + (esJson ? EXTENSIONJSON : EXTENSIONXML) + "\" esta vacia";
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        }
    }

            private void descargarApuestas() {
        final ProgressDialog progreso = new ProgressDialog(this);
        String url = String.valueOf(edtApuestas.getText());
        if(!url.equals("")) {
            RestClient.get(url, new FileAsyncHttpResponseHandler(this) {

                @Override
                public void onStart() {
                    super.onStart();
                    progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progreso.setMessage("Conectando . . .");
                    progreso.setCancelable(false);
                    progreso.show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, File file) {
                    progreso.dismiss();
                    //Toast.makeText(MainActivity.this, "El fichero " + file.getPath() +
                    // " se ha descargado con exito", Toast.LENGTH_SHORT).show();

                    Resultado resultado = leer(file, UTF8);
                    String[] apuestas = resultado.getContenido().split("\n");

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                    progreso.dismiss();
                    String mensaje = "El fichero \"" + APUESTAS + "\" no se ha descargado";
                    Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            String mensaje = "La ruta del fichero \"" + APUESTAS + "\" esta vacia";
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        }
    }
}
