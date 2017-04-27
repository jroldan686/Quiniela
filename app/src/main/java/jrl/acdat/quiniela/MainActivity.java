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

import org.json.JSONException;
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

    // Servidor de alumno.club
    public static final String RUTASERVIDORRESULTADOS = "http://alumno.club/acceso/quiniela/";
    public static final String RUTASERVIDORAPUESTAS = "http://192.168.2.11/acceso/quiniela/";
    public static final String RUTASERVIDORACIERTOSYPREMIOS = RUTASERVIDORAPUESTAS;

    // Servidor de clase
    //public static final String RUTASERVIDORRESULTADOS = "http://192.168.2.11/acceso/quiniela/";
    //public static final String RUTASERVIDORAPUESTAS = RUTASERVIDORRESULTADOS;
    //public static final String RUTASERVIDORACIERTOSYPREMIOS = RUTASERVIDORRESULTADOS;

    // Servidor de casa
    //public static final String RUTASERVIDORRESULTADOS = "http://192.168.1.6/curso1617/quiniela/";
    //public static final String RUTASERVIDORAPUESTAS = RUTASERVIDORRESULTADOS;
    //public static final String RUTASERVIDORACIERTOSYPREMIOS = RUTASERVIDORRESULTADOS;


    public static final String RESULTADOS = "resultados";
    public static final String APUESTAS = "apuestas.txt";
    public static final String PREMIOS = "premios";
    public static final String EXTENSIONXML = "xml";
    public static final String EXTENSIONJSON = "json";
    public static final String FICHEROPHP = "premios.php";


    public static final String UTF8 = "utf-8";

    String[] basura = {"", "-", "_"};   // Array que contiene los string incluidos en las conversiones de ficheros xml a json
    int contador = 0;                   // Contador que gestiona el paso de un string a otro en el array anterior
    boolean esOk;                       // Booleano que verifica si se han probado todos los string del array anterior

    RadioButton rdbXml, rdbJson;
    TextView txvResultados, txvApuestas, txvAciertosYPremios;//, txvResultadosQuiniela, txvPremiosQuiniela;
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
                    edtResultados.setText(obtenerRutaSinExtension(
                            edtResultados.getText().toString()) + "." + EXTENSIONXML);
                    edtAciertosYPremios.setText(obtenerRutaSinExtension(
                            edtAciertosYPremios.getText().toString()) + "." + EXTENSIONXML);
                    esJson = false;
                }
            }
        });
        rdbJson = (RadioButton)findViewById(R.id.rdbJson);
        rdbJson.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    edtResultados.setText(obtenerRutaSinExtension(
                            edtResultados.getText().toString()) + "." + EXTENSIONJSON);
                    edtAciertosYPremios.setText(obtenerRutaSinExtension(
                            edtAciertosYPremios.getText().toString()) + "." + EXTENSIONJSON);
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

        rutaResultados = RUTASERVIDORRESULTADOS + RESULTADOS + "." + EXTENSIONJSON;
        rutaApuestas = RUTASERVIDORAPUESTAS + APUESTAS;
        rutaAciertosYPremios = RUTASERVIDORACIERTOSYPREMIOS + PREMIOS + "." + EXTENSIONJSON;
        edtResultados.setText(rutaResultados);
        edtApuestas.setText(rutaApuestas);
        edtAciertosYPremios.setText(rutaAciertosYPremios);
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
        String control = "";
        if(!ruta.equals("")) {
            if (URLUtil.isValidUrl(ruta)) {
                esValida = true;
            } else {
                if(editText == edtResultados) control = "de resultados";
                if(editText == edtApuestas) control = "de apuestas";
                if(editText == edtAciertosYPremios) control = "de aciertos y premios";
                mensaje = "La ruta \"" + control + "\" no es valida";
                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        } else {
            mensaje = "La ruta \"" + control + "\" esta vacia";
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
                if(!partes[ultima].contains("/")) {
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
                } else {
                    String[] subpartes = partes[ultima].split("/");
                    String nombreFichero = subpartes[subpartes.length - 1];
                    ruta += "." + (esJson ? EXTENSIONJSON : EXTENSIONXML);
                    editText.setText(ruta);
                    String mensaje = "La extension del fichero \"" + nombreFichero + "\" no existe. " +
                                     "Se sustituyo por \"." + (esJson ? EXTENSIONJSON : EXTENSIONXML) + "\"";
                    Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                }
            }
        }
        return ruta;
    }

    private String obtenerRutaSinExtension(String ruta) {
        String[] partes = ruta.split("\\.");
        ruta = partes[0];
        for (int i = 1; i < partes.length - 1; i++) {
            ruta += "." + partes[i];
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

                esOk = false;
                do {
                    try {
                        if(esJson) {
                            JSONObject responseJSON = new JSONObject(responseString);
                            quinielas = Analisis.obtenerResultados(responseJSON, basura[contador]);
                        } else {
                            quinielas = Analisis.obtenerResultados(responseString);
                        }
                        escrutarApuestas(quinielas, apuestas);
                        contador = 0;
                        esOk = true;
                    } catch (Exception ex) {
                        if(esJson) {
                            //Toast.makeText(MainActivity.this, "El fichero JSON ha fallado. Reintentando de nuevo...", Toast.LENGTH_LONG).show();
                            if(contador < basura.length - 1) contador++;
                            else {
                                Toast.makeText(MainActivity.this, "El fichero JSON ha fallado. Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                                esOk = true;
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "El fichero XML ha fallado. Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                            contador = 0;
                            esOk = true;
                        }
                    }
                } while(!esOk);
                progreso.dismiss();
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
        int ultimaJornada = quinielas.size();
        String temporada = quinielas.get(ultimaJornada - 1).getTemporada();
        String jornada = quinielas.get(ultimaJornada - 1).getJornada();
        String apuesta, numero, resultado, plenoAl15, contenido = "";
        Premiada premiada;
        int partidosAcertados;
        ArrayList<Premiada> premiadas = new ArrayList<Premiada>();
        int acertadosDe10 = 0;
        int acertadosDe11 = 0;
        int acertadosDe12 = 0;
        int acertadosDe13 = 0;
        int acertadosDe14 = 0;
        int acertadosDe15 = 0;
        int acertados = 0;
        float total = 0;
        Gson gson = new Gson();

        for(int i=0; i < apuestas.length; i++) {
            apuesta = apuestas[i];
            premiada = new Premiada();
            partidosAcertados = 0;
            for (int j = 0; j < quinielas.get(ultimaJornada - 1).getPartit().size(); j++) {
                numero = quinielas.get(ultimaJornada - 1).getPartit().get(j).getNum();
                resultado = quinielas.get(ultimaJornada - 1).getPartit().get(j).getSig();
                if (!numero.equals("15") && resultado.equals(String.valueOf(apuesta.charAt(j)))) {
                    partidosAcertados++;
                }
                if (partidosAcertados == 14) {
                    plenoAl15 = String.valueOf(apuesta.charAt(j)) + String.valueOf(apuesta.charAt(j + 1));
                    if (resultado.equals(plenoAl15)) {
                        partidosAcertados++;
                    }
                }
                if (numero.equals("15")) {
                    if (partidosAcertados > 9) {
                        premiada.setApuesta(apuesta);
                        acertados++;
                        switch (partidosAcertados) {
                            case 10:
                                premiada.setCategoria(Categoria.quinta);
                                premiada.setPremio((float) (quinielas.get(ultimaJornada - 1).getEl10()) / 100);
                                acertadosDe10++;
                                break;
                            case 11:
                                premiada.setCategoria(Categoria.cuarta);
                                premiada.setPremio((float) (quinielas.get(ultimaJornada - 1).getEl11()) / 100);
                                acertadosDe11++;
                                break;
                            case 12:
                                premiada.setCategoria(Categoria.tercera);
                                premiada.setPremio((float) (quinielas.get(ultimaJornada - 1).getEl12()) / 100);
                                acertadosDe12++;
                                break;
                            case 13:
                                premiada.setCategoria(Categoria.segunda);
                                premiada.setPremio((float) (quinielas.get(ultimaJornada - 1).getEl13()) / 100);
                                acertadosDe13++;
                                break;
                            case 14:
                                premiada.setCategoria(Categoria.primera);
                                premiada.setPremio((float) (quinielas.get(ultimaJornada - 1).getEl14()) / 100);
                                acertadosDe14++;
                                break;
                            case 15:
                                premiada.setCategoria(Categoria.especial);
                                premiada.setPremio((float) (quinielas.get(ultimaJornada - 1).getEl15()) / 100);
                                acertadosDe15++;
                                break;
                        }
                        premiadas.add(premiada);
                    }
                    break;
                }
            }
        }

        Premio premios = new Premio();
        premios.setAcertadosDe10(acertadosDe10);
        premios.setAcertadosDe11(acertadosDe11);
        premios.setAcertadosDe12(acertadosDe12);
        premios.setAcertadosDe13(acertadosDe13);
        premios.setAcertadosDe14(acertadosDe14);
        premios.setAcertadosDe15(acertadosDe15);
        premios.setPremiosDe10((float) ((quinielas.get(ultimaJornada - 1).getEl10()) / 100) * premios.getAcertadosDe10());
        premios.setPremiosDe11((float) ((quinielas.get(ultimaJornada - 1).getEl11()) / 100) * premios.getAcertadosDe11());
        premios.setPremiosDe12((float) ((quinielas.get(ultimaJornada - 1).getEl12()) / 100) * premios.getAcertadosDe12());
        premios.setPremiosDe13((float) ((quinielas.get(ultimaJornada - 1).getEl13()) / 100) * premios.getAcertadosDe13());
        premios.setPremiosDe14((float) ((quinielas.get(ultimaJornada - 1).getEl14()) / 100) * premios.getAcertadosDe14());
        premios.setPremiosDe15((float) ((quinielas.get(ultimaJornada - 1).getEl15()) / 100) * premios.getAcertadosDe15());

        Escrutinio escrutinio = new Escrutinio();
        if(!esJson) {
            contenido = "<quinielas>\n\t<premiadas>\n";
        }
        for(int i = 0; i < premiadas.size(); i++) {
            if(!esJson) {
                contenido += "\t\t<quiniela>\n" +
                        "\t\t\t<apuestas>" + premiadas.get(i).getApuesta() + "</apuestas>\n" +
                        "\t\t\t<categoria>" + premiadas.get(i).getCategoria() + "</categoria>\n" +
                        "\t\t\t<premios>" + premiadas.get(i).getPremio() + "</premios>\n" +
                        "\t\t</quiniela>\n";
            }
            total += premiadas.get(i).getPremio();
        }
        premios.setAcertados(acertados);
        premios.setTotal(total);
        if(esJson) {
            escrutinio.setPremiadas(premiadas);
            escrutinio.setTemporada(temporada);
            escrutinio.setJornada(jornada);
            escrutinio.setPremios(premios);
            contenido = gson.toJson(escrutinio);
        } else {
            contenido += "\t</premiadas>\n" +
                    "\t<temporada>" + temporada + "</temporada>\n" +
                    "\t<jornada>" + jornada + "</jornada>\n" +
                    "\t<premios>\n" +
                    "\t\t<acertadosDe10>" + premios.getAcertadosDe10() + "</acertadosDe10>\n" +
                    "\t\t<premiosDe10>" + premios.getPremiosDe10() + "</premiosDe10>\n" +
                    "\t\t<acertadosDe11>" + premios.getAcertadosDe11() + "</acertadosDe11>\n" +
                    "\t\t<premiosDe11>" + premios.getPremiosDe11() + "</premiosDe11>\n" +
                    "\t\t<acertadosDe12>" + premios.getAcertadosDe12() + "</acertadosDe12>\n" +
                    "\t\t<premiosDe12>" + premios.getPremiosDe12() + "</premiosDe12>\n" +
                    "\t\t<acertadosDe13>" + premios.getAcertadosDe13() + "</acertadosDe13>\n" +
                    "\t\t<premiosDe13>" + premios.getPremiosDe13() + "</premiosDe13>\n" +
                    "\t\t<acertadosDe14>" + premios.getAcertadosDe14() + "</acertadosDe14>\n" +
                    "\t\t<premiosDe14>" + premios.getPremiosDe14() + "</premiosDe14>\n" +
                    "\t\t<acertadosDe15>" + premios.getAcertadosDe15() + "</acertadosDe15>\n" +
                    "\t\t<premiosDe15>" + premios.getPremiosDe15() + "</premiosDe15>\n" +
                    "\t\t<acertados>" + premios.getAcertados() + "</acertados>\n" +
                    "\t\t<total>" + premios.getTotal() + "</total>\n" +
                    "\t</premios>\n" +
                    "</quinielas>";
        }
        if(memoria.escribirInterna(ficheroAciertosYPremios, contenido, false, UTF8)) {
            File fichero = new File(this.getFilesDir(), ficheroAciertosYPremios);
            subirPremios(fichero);
        } else {
            String mensaje = "El fichero \"" + ficheroAciertosYPremios + "\" no se ha creado";
            Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
        }
    }
}
