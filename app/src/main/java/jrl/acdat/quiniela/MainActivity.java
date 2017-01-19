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

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
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
    ArrayList<Quiniela> quinielas = null;
    String[] apuestas = null;
    Memoria memoria;

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
        memoria = new Memoria(this);
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
                        if(esJson) {
                            JSONObject responseJSON = new JSONObject(responseString);
                            quinielas = Analisis.obtenerResultados(responseJSON);
                        } else {
                            quinielas = Analisis.obtenerResultados(responseString);
                        }
                        /*
                        for(int i = 0; i < quinielas.size(); i++) {
                            for(int j = 0; j < quinielas.get(i).getPartit().size(); j++) {
                                Toast.makeText(MainActivity.this, quinielas.get(i).getJornada() + " : " +
                                        quinielas.get(i).getPartit().get(j).getEquipo1(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        */
                        while(quinielas == null || apuestas == null);   // Espera a que los datos estén disponibles
                        escrutarApuestas(quinielas, apuestas);
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
                    apuestas = resultado.getContenido().split("\n");
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
/*
    private void subirPremios(String premios) {
        RequestParams params = new RequestParams();
        params.put("premios", premios);
        RestClient.post(RUTASERVIDOR, params, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(MainActivity.this, "El fichero \"" + PREMIOS + (esJson ? EXTENSIONJSON : EXTENSIONXML) +
                        "\" no se ha subido al Servidor", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Toast.makeText(MainActivity.this, "El fichero \"" + PREMIOS + (esJson ? EXTENSIONJSON : EXTENSIONXML) +
                        "\" se ha subido con exito al Servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
*/
    private void escrutarApuestas(ArrayList<Quiniela> quinielas, String[] apuestas) {
        int ultimaJornada = quinielas.size() - 1;
        double recaudacion = quinielas.get(ultimaJornada).getRecaudacion();
        String apuesta, numero, resultado, plenoAl15, contenido = "";
        Premiada premiada;
        int partidosAcertados = 0;
        ArrayList<Premiada> premiadas = new ArrayList<Premiada>();
        int acertantesEspecial = 0;
        int acertantesPrimera = 0;
        int acertantesSegunda = 0;
        int acertantesTercera = 0;
        int acertantesCuarta = 0;
        int acertantesQuinta = 0;

        for(int i = 0; i < apuestas.length; i++) {
            apuesta = apuestas[i];
            premiada = new Premiada();
            for (int j = 0; j < quinielas.get(ultimaJornada).getPartit().size(); j++) {
                partidosAcertados = 0;
                numero = quinielas.get(ultimaJornada).getPartit().get(j).getNum();
                resultado = quinielas.get(ultimaJornada).getPartit().get(j).getSig();
                if (!numero.equals("15") && resultado.equals(String.valueOf(apuesta.charAt(j)))) {
                    partidosAcertados++;
                }
                if (numero.equals("15")) {
                    plenoAl15 = String.valueOf(apuesta.charAt(j)) + String.valueOf(apuesta.charAt(j + 1));
                    if(resultado.equals(plenoAl15)) {
                        partidosAcertados++;
                    }
                    if(partidosAcertados > 9) {
                        premiada.setApuesta(apuesta);
                        switch (partidosAcertados) {
                            case 10: premiada.setCategoria(Categoria.QUINTA); acertantesQuinta++; break;
                            case 11: premiada.setCategoria(Categoria.CUARTA); acertantesCuarta++; break;
                            case 12: premiada.setCategoria(Categoria.TERCERA); acertantesTercera++; break;
                            case 13: premiada.setCategoria(Categoria.SEGUNDA); acertantesSegunda++; break;
                            case 14: premiada.setCategoria(Categoria.PRIMERA); acertantesPrimera++; break;
                            case 15: premiada.setCategoria(Categoria.ESPECIAL); acertantesEspecial++; break;
                        }
                        premiadas.add(premiada);
                    }
                }
            }
        }

        for(int i = 0; i < premiadas.size(); i++) {
            if(premiadas.get(i).getCategoria() == Categoria.QUINTA) {
                premiadas.get(i).setPremio((recaudacion * 0.09) / acertantesQuinta);
            }
            if(premiadas.get(i).getCategoria() == Categoria.CUARTA) {
                premiadas.get(i).setPremio((recaudacion * 0.075) / acertantesCuarta);
            }
            if(premiadas.get(i).getCategoria() == Categoria.TERCERA) {
                premiadas.get(i).setPremio((recaudacion * 0.075) / acertantesTercera);
            }
            if(premiadas.get(i).getCategoria() == Categoria.SEGUNDA) {
                premiadas.get(i).setPremio((recaudacion * 0.075) / acertantesSegunda);
            }
            if(premiadas.get(i).getCategoria() == Categoria.PRIMERA) {
                premiadas.get(i).setPremio((recaudacion * 0.16) / acertantesPrimera);
            }
            if(premiadas.get(i).getCategoria() == Categoria.ESPECIAL) {
                premiadas.get(i).setPremio((recaudacion * 0.075) / acertantesEspecial);
            }
        }

        for(int i = 0; i < premiadas.size(); i++) {
            contenido += premiadas.get(i).getApuesta() + " -> " + premiadas.get(i).getPremio() + " Euros" + "\n";
        }
        String fichero = PREMIOS + (esJson ? EXTENSIONJSON : EXTENSIONXML);
        if(memoria.escribirInterna(fichero, contenido, true, UTF8)) {
            // subirPremios(premios);
        } else {
            String mensaje = "El fichero \"" + fichero + "\" no se ha creado";
            Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
        }

        /*
        for(int i = 0; i < quinielas.size(); i++) {
            apuesta = apuestas[i];
            for(int j = 0; j < quinielas.get(i).getPartit().size(); j++) {
                numero = quinielas.get(i).getPartit().get(j).getNum();
                resultado = quinielas.get(i).getPartit().get(j).getSig();
                if(!numero.equals("15") && resultado.equals(String.valueOf(apuesta.charAt(j)))) {

                }
        */
                /*
                Toast.makeText(MainActivity.this, quinielas.get(i).getJornada() + " : " +
                        quinielas.get(i).getPartit().get(j).getEquipo1(), Toast.LENGTH_SHORT).show();
                */
    }
}
