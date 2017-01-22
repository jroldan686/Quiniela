package jrl.acdat.quiniela;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    // Servidor de quinielista.es
    //public static final String RUTAQUINIELISTA = "http://www.quinielista.es/xml/temporada.asp";

    // Servidor de clase
    public static final String RUTASERVIDOR = "http://192.168.2.11/acceso/quiniela/";

    // Servidor de casa
    //public static final String RUTASERVIDOR = "http://192.168.1.6/curso1617/quiniela/";

    public static final String RESULTADOS = "resultados";
    public static final String APUESTAS = "apuestas.txt";
    public static final String PREMIOS = "premios";
    public static final String EXTENSIONXML = "xml";
    public static final String EXTENSIONJSON = "json";
    public static final String FICHEROPHP = "premios.php";

    public static final String UTF8 = "utf-8";

    RadioButton rdbXml, rdbJson;
    TextView txvResultados, txvApuestas, txvAciertosYPremios;
    EditText edtResultados, edtApuestas, edtAciertosYPremios;
    Button btnCalcular;

    boolean esJson;
    String rutaResultados, rutaApuestas, rutaAciertosYPremios;
    String ficheroResultados, ficheroApuestas, ficheroAciertosYPremios;
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
                    edtResultados.setText(RUTASERVIDOR + RESULTADOS + "." + EXTENSIONXML);
                    edtAciertosYPremios.setText(RUTASERVIDOR + PREMIOS + "." + EXTENSIONXML);
                    esJson = false;
                }
            }
        });
        rdbJson = (RadioButton)findViewById(R.id.rdbJson);
        rdbJson.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    edtResultados.setText(RUTASERVIDOR + RESULTADOS + "." + EXTENSIONJSON);
                    edtAciertosYPremios.setText(RUTASERVIDOR + PREMIOS + "." + EXTENSIONJSON);
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
                rutaResultados = edtResultados.getText().toString();
                rutaApuestas = edtApuestas.getText().toString();
                rutaAciertosYPremios = edtAciertosYPremios.getText().toString();
                if(comprobarRuta(rutaResultados, edtResultados) &&
                   comprobarRuta(rutaApuestas, edtApuestas) &&
                   comprobarRuta(rutaAciertosYPremios, edtAciertosYPremios)) {
                    ficheroResultados = obtenerNombreFichero(rutaResultados =
                                        modificarRuta(rutaResultados, edtResultados));
                    ficheroApuestas = obtenerNombreFichero(rutaApuestas);
                    ficheroAciertosYPremios = obtenerNombreFichero(rutaAciertosYPremios =
                                              modificarRuta(rutaAciertosYPremios, edtAciertosYPremios));
                    descargarApuestas();
                }
            }
        });

        edtResultados.setText(RUTASERVIDOR + RESULTADOS + "." + EXTENSIONJSON);
        edtApuestas.setText(RUTASERVIDOR + APUESTAS);
        edtAciertosYPremios.setText(RUTASERVIDOR + PREMIOS + "." + EXTENSIONJSON);
        esJson = true;
        memoria = new Memoria(this);
    }

    public String obtenerNombreFichero(String ruta) {
        String[] partes = ruta.split("/");
        int ultima = partes.length - 1;
        return partes[ultima];
    }

    public String obtenerRutaPHP(String ruta) {
        String[] partes = ruta.split("/");
        String rutaServidor = partes[0] + "/";
        for(int i = 1; i < partes.length - 1; i++)
            rutaServidor += partes[i] + "/";
        return rutaServidor + FICHEROPHP;
    }

    public boolean comprobarRuta(String ruta, EditText editText) {
        boolean esValida = false;
        String mensaje;
        if(!ruta.equals("")) {
            if (URLUtil.isValidUrl(ruta)) {
                esValida = true;
            } else {
                String control = "";
                if(editText == edtResultados) control = "de resultados";
                if(editText == edtApuestas) control = "de apuestas";
                if(editText == edtAciertosYPremios) control = "de aciertos y premios";
                mensaje = "La ruta \"" + control + "\" no es valida";
                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        } else {
            mensaje = "La ruta \"" + ruta + "\" esta vacia";
            Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
        }
        return esValida;
    }

    private String modificarRuta(String ruta, EditText editText) {
        String[] partes = ruta.split("\\.");
        int ultima = 0;
        if(partes.length > 0) {
            ultima = partes.length - 1;
            if (!(partes[ultima].equals(esJson ? EXTENSIONJSON : EXTENSIONXML))) {
                String rutaAntigua = ruta;
                ruta = partes[0];
                partes[ultima] = esJson ? EXTENSIONJSON : EXTENSIONXML;
                for (int i = 1; i < partes.length; i++)
                    ruta += "." + partes[i];
                String[] partesFicheroAntiguo = rutaAntigua.split("/");
                String antiguofichero = partesFicheroAntiguo[partesFicheroAntiguo.length - 1];
                String[] partesFicheroNuevo = partes[partes.length - 2].split("/");
                String nombrefichero = partesFicheroNuevo[partesFicheroNuevo.length - 1];
                String nuevofichero = (nombrefichero + "." + (esJson ? EXTENSIONJSON : EXTENSIONXML));
                editText.setText(ruta);
                String mensaje = "La extension del fichero \"" + antiguofichero + "\" fue modificado por \"" + nuevofichero + "\"";
                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        }
        return ruta;
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
        RestClient.get(rutaResultados, new TextHttpResponseHandler() {

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
                    if (esJson) {
                        JSONObject responseJSON = new JSONObject(responseString);
                        quinielas = Analisis.obtenerResultados(responseJSON);
                    } else {
                        quinielas = Analisis.obtenerResultados(responseString);
                    }
                    escrutarApuestas(quinielas, apuestas);
                } catch (Exception ex) {
                    Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                progreso.dismiss();
                String mensaje = "El fichero \"" + ficheroResultados + "\" no se ha descargado";
                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void descargarApuestas() {
        final ProgressDialog progreso = new ProgressDialog(this);
        RestClient.get(rutaApuestas, new FileAsyncHttpResponseHandler(this) {

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
                Resultado resultado = leer(file, UTF8);
                apuestas = resultado.getContenido().split("\n");
                descargarResultados();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                progreso.dismiss();
                String mensaje = "El fichero \"" + ficheroApuestas + "\" no se ha descargado";
                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void subirPremios(File file) {
        final ProgressDialog progreso = new ProgressDialog(MainActivity.this);
        final String nombrefichero = file.getName();
        RequestParams params = new RequestParams();
        try {
            params.put("param", file);
            RestClient.post(obtenerRutaPHP(rutaAciertosYPremios), params, new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progreso.setMessage("Conectando . . .");
                    progreso.setCancelable(false);
                    progreso.show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    progreso.dismiss();
                    Toast.makeText(MainActivity.this, "El fichero \"" + nombrefichero +
                            "\" no se ha subido al Servidor", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    progreso.dismiss();
                    Toast.makeText(MainActivity.this, "El fichero \"" + nombrefichero +
                            "\" se ha subido con exito al Servidor", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (FileNotFoundException ex) {
            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void escrutarApuestas(ArrayList<Quiniela> quinielas, String[] apuestas) {
        int ultimaJornada = quinielas.size() - 1;
        String temporada = quinielas.get(ultimaJornada).getTemporada();
        String jornada = quinielas.get(ultimaJornada).getJornada();
        String apuesta, numero, resultado, plenoAl15, contenido = "";
        Premiada premiada;
        int partidosAcertados = 0;
        ArrayList<Premiada> premiadas = new ArrayList<Premiada>();

        for(int i = 0; i < apuestas.length; i++) {
            apuesta = apuestas[i];
            premiada = new Premiada();
            partidosAcertados = 0;
            for (int j = 0; j < quinielas.get(ultimaJornada).getPartit().size(); j++) {
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
                        premiada.setTemporada(temporada);
                        premiada.setJornada(jornada);
                        premiada.setApuesta(apuesta);
                        switch (partidosAcertados) {
                            case 10: premiada.setCategoria(Categoria.QUINTA);
                                     premiada.setPremio(quinielas.get(ultimaJornada).getEl10() / 100);
                                     break;
                            case 11: premiada.setCategoria(Categoria.CUARTA);
                                     premiada.setPremio(quinielas.get(ultimaJornada).getEl11() / 100);
                                     break;
                            case 12: premiada.setCategoria(Categoria.TERCERA);
                                     premiada.setPremio(quinielas.get(ultimaJornada).getEl12() / 100);
                                     break;
                            case 13: premiada.setCategoria(Categoria.SEGUNDA);
                                     premiada.setPremio(quinielas.get(ultimaJornada).getEl13() / 100);
                                     break;
                            case 14: premiada.setCategoria(Categoria.PRIMERA);
                                     premiada.setPremio(quinielas.get(ultimaJornada).getEl14() / 100);
                                     break;
                            case 15: premiada.setCategoria(Categoria.ESPECIAL);
                                     premiada.setPremio(quinielas.get(ultimaJornada).getEl15() / 100);
                                     break;
                        }
                        premiadas.add(premiada);
                    }
                    break;
                }
            }
        }
        if(!esJson)
            contenido = "<quinielas>\n\t<premiadas>\n";
        for(int i = 0; i < premiadas.size(); i++) {
            if(esJson) {
                Gson gson = new Gson();
                contenido += gson.toJson(premiadas.get(i)) + "\n";
            } else {
                contenido += "\t\t<quiniela>\n" +
                             "\t\t\t<apuesta>" + premiadas.get(i).getApuesta() + "</apuesta>\n" +
                             "\t\t\t<categoria>" + premiadas.get(i).getCategoria() + "</categoria>\n" +
                             "\t\t\t<jornada>" + premiadas.get(i).getJornada() + "</jornada>\n" +
                             "\t\t\t<premio>" + premiadas.get(i).getPremio() + "</premio>\n" +
                             "\t\t\t<temporada>" + premiadas.get(i).getTemporada() + "</temporada>\n" +
                             "\t\t</quiniela>\n";
            }
        }
        if(!esJson)
            contenido += "\t</premiadas>\n</quinielas>";
        //String nombrefichero = PREMIOS + (esJson ? EXTENSIONJSON : EXTENSIONXML);
        if(memoria.escribirInterna(ficheroAciertosYPremios, contenido, false, UTF8)) {
            File fichero = new File(this.getFilesDir(), ficheroAciertosYPremios);
            subirPremios(fichero);
        } else {
            String mensaje = "El fichero \"" + ficheroAciertosYPremios + "\" no se ha creado";
            Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
        }
    }
}
