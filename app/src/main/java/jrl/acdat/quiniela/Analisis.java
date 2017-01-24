package jrl.acdat.quiniela;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class Analisis {

    public static ArrayList<Quiniela> obtenerResultados(String xml) throws XmlPullParserException, IOException {
        boolean esQuiniela = false;
        boolean esPartit = false;
        Quiniela quiniela = null;
        String nombreAtributo = "";
        String valorAtributo = "";
        Partido partido = null;
        ArrayList<Quiniela> quinielas = new ArrayList<Quiniela>();
        ArrayList<Partido> partidos = new ArrayList<Partido>();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(xml));

        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (xpp.getName().equals("quiniela")) {
                        esQuiniela = true;
                        quiniela = new Quiniela();
                    }
                    if (xpp.getName().equals("partit")) {
                        esPartit = true;
                        partido = new Partido();
                    }
                    if (esQuiniela) {
                        for (int i = 0; i < xpp.getAttributeCount(); i++) {
                            nombreAtributo = xpp.getAttributeName(i);
                            valorAtributo = xpp.getAttributeValue(i);
                            if (nombreAtributo.equals("jornada")) {
                                quiniela.setJornada(valorAtributo);
                            }
                            if (nombreAtributo.equals("temporada")) {
                                quiniela.setTemporada(valorAtributo);
                            }
                            if (nombreAtributo.equals("recaudacion")) {
                                quiniela.setRecaudacion(Integer.valueOf(valorAtributo));
                            }
                            if (nombreAtributo.equals("el15")) {
                                quiniela.setEl15(Integer.valueOf(valorAtributo));
                            }
                            if (nombreAtributo.equals("el14")) {
                                quiniela.setEl14(Integer.valueOf(valorAtributo));
                            }
                            if (nombreAtributo.equals("el13")) {
                                quiniela.setEl13(Integer.valueOf(valorAtributo));
                            }
                            if (nombreAtributo.equals("el12")) {
                                quiniela.setEl12(Integer.valueOf(valorAtributo));
                            }
                            if (nombreAtributo.equals("el11")) {
                                quiniela.setEl11(Integer.valueOf(valorAtributo));
                            }
                            if (nombreAtributo.equals("el10")) {
                                quiniela.setEl10(Integer.valueOf(valorAtributo));
                            }
                            if (nombreAtributo.equals("apuesta")) {
                                quiniela.setApuesta(Integer.valueOf(valorAtributo));
                            }
                        }
                        if (esPartit) {
                            for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                nombreAtributo = xpp.getAttributeName(i);
                                valorAtributo = xpp.getAttributeValue(i);
                                if (nombreAtributo.equals("num")) {
                                    partido.setNum(valorAtributo);
                                }
                                if (nombreAtributo.equals("equipo1")) {
                                    partido.setEquipo1(valorAtributo);
                                }
                                if (nombreAtributo.equals("equipo2")) {
                                    partido.setEquipo2(valorAtributo);
                                }
                                if (nombreAtributo.equals("pt1")) {
                                    partido.setPt1(Integer.valueOf(valorAtributo));
                                }
                                if (nombreAtributo.equals("ptX")) {
                                    partido.setPtX(Integer.valueOf(valorAtributo));
                                }
                                if (nombreAtributo.equals("pt2")) {
                                    partido.setPt2(Integer.valueOf(valorAtributo));
                                }
                                if (nombreAtributo.equals("sig")) {
                                    partido.setSig(valorAtributo);
                                }
                            }
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (xpp.getName().equals("partit")) {
                        esPartit = false;
                        if (partido != null) {
                            // Si el partido no tiene resultado premiado
                            if(!partido.getSig().equals("")) {
                                partidos.add(partido);
                                if (quiniela != null)
                                    quiniela.setPartit(partidos);
                            } else
                                // Se termina el metodo, ya que esto significa que no se han escrutado las quinielas siguientes
                                return quinielas;
                        }
                    }
                    if (xpp.getName().equals("quiniela")) {
                        esQuiniela = false;
                        if(partido != null && !partido.getSig().equals(""))
                            quinielas.add(quiniela);
                    }
                    break;
            }
            eventType = xpp.next();
        }
        return quinielas;
    }

    public static ArrayList<Quiniela> obtenerResultados(JSONObject json) throws JSONException {
        JSONObject quinielistaJSON = json.getJSONObject("quinielista");
        JSONArray quinielasJSON = quinielistaJSON.getJSONArray("quiniela");
        Quiniela quiniela = null;
        Partido partido = null;
        ArrayList<Quiniela> quinielas = new ArrayList<Quiniela>();
        for(int i = 0; i < quinielasJSON.length(); i++) {
            quiniela = new Quiniela();
            JSONArray partidosJSON = quinielasJSON.getJSONObject(i).getJSONArray("partit");
            ArrayList<Partido> partidos = new ArrayList<Partido>();
            for(int j = 0; j < partidosJSON.length(); j++) {
                // 1. Si el partido no tiene resultado premiado
                if(!partidosJSON.getJSONObject(j).getString("sig").equals("")) {
                    partido = new Partido();
                    partido.setNum(partidosJSON.getJSONObject(j).getString("num"));
                    partido.setEquipo1(partidosJSON.getJSONObject(j).getString("equipo1"));
                    partido.setEquipo2(partidosJSON.getJSONObject(j).getString("equipo2"));
                    partido.setPt1(partidosJSON.getJSONObject(j).getInt("pt1"));
                    partido.setPtX(partidosJSON.getJSONObject(j).getInt("ptX"));
                    partido.setPt2(partidosJSON.getJSONObject(j).getInt("pt2"));
                    // 2. Si no existe la etiqueta "sig"
                    try {
                        partido.setSig(partidosJSON.getJSONObject(j).getString("sig"));
                    } catch (JSONException ex) {
                        // 2. Se termina el metodo, ya que esto significa que no se han escrutado las quinielas siguientes
                        return quinielas;
                    }
                    partidos.add(partido);
                } else
                    // 1. Se termina el metodo, ya que esto significa que no se han escrutado las quinielas siguientes
                    return quinielas;
            }
            quiniela.setPartit(partidos);
            quiniela.setJornada(quinielasJSON.getJSONObject(i).getString("jornada"));
            quiniela.setTemporada(quinielasJSON.getJSONObject(i).getString("temporada"));
            quiniela.setRecaudacion(quinielasJSON.getJSONObject(i).getInt("recaudacion"));
            quiniela.setEl15(quinielasJSON.getJSONObject(i).getInt("el15"));
            quiniela.setEl14(quinielasJSON.getJSONObject(i).getInt("el14"));
            quiniela.setEl13(quinielasJSON.getJSONObject(i).getInt("el13"));
            quiniela.setEl12(quinielasJSON.getJSONObject(i).getInt("el12"));
            quiniela.setEl11(quinielasJSON.getJSONObject(i).getInt("el11"));
            quiniela.setEl10(quinielasJSON.getJSONObject(i).getInt("el10"));
            quiniela.setApuesta(quinielasJSON.getJSONObject(i).getInt("apuesta"));
            quinielas.add(quiniela);
        }
        return quinielas;
    }
}
