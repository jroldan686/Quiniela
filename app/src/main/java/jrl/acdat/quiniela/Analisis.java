package jrl.acdat.quiniela;

import android.content.Context;
/*
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
*/
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Analisis {

    public static ArrayList<Quiniela> obtenerResultados(JSONObject object) throws JSONException {
        JSONObject quinielistaJSON = object.getJSONObject("quinielista");
        JSONArray quinielasJSON = quinielistaJSON.getJSONArray("quiniela");
        Quiniela quiniela;
        Partido partido;
        ArrayList<Quiniela> quinielas = new ArrayList<Quiniela>();
        for(int i = 0; i < quinielasJSON.length(); i++) {
            quiniela = new Quiniela();
            JSONArray partidosJSON = quinielasJSON.getJSONArray(i);
            ArrayList<Partido> partidos = new ArrayList<Partido>();
            for(int j = 0; j < partidosJSON.length(); j++) {
                partido = new Partido();
                partido.setNum(partidosJSON.getJSONObject(j).getInt("num"));
                partido.setEquipo1(partidosJSON.getJSONObject(j).getString("equipo1"));
                partido.setEquipo2(partidosJSON.getJSONObject(j).getString("equipo2"));
                partido.setPt1(partidosJSON.getJSONObject(j).getInt("pt1"));
                partido.setPtX(partidosJSON.getJSONObject(j).getInt("ptX"));
                partido.setPt2(partidosJSON.getJSONObject(j).getInt("pt2"));
                partido.setSig(partidosJSON.getJSONObject(j).getString("sig"));
                partidos.add(partido);
            }
            quiniela.setPartit(partidos);
            quiniela.setJornada(quinielasJSON.getJSONObject(i).getInt("jornada"));
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
