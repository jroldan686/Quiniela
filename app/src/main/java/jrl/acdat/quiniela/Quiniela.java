package jrl.acdat.quiniela;

import org.json.JSONArray;

import java.util.ArrayList;

public class Quiniela {

    private ArrayList<Partido> partit;
    private String jornada;
    private String temporada;
    private int recaudacion;
    private int el15;
    private int el14;
    private int el13;
    private int el12;
    private int el11;
    private int el10;
    private int apuesta;

    public ArrayList<Partido> getPartit() {
        return partit;
    }

    public void setPartit(ArrayList<Partido> partit) {
        this.partit = partit;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    public String getTemporada() {
        return temporada;
    }

    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }

    public int getRecaudacion() {
        return recaudacion;
    }

    public void setRecaudacion(int recaudacion) {
        this.recaudacion = recaudacion;
    }

    public int getEl15() {
        return el15;
    }

    public void setEl15(int el15) {
        this.el15 = el15;
    }

    public int getEl14() {
        return el14;
    }

    public void setEl14(int el14) {
        this.el14 = el14;
    }

    public int getEl13() {
        return el13;
    }

    public void setEl13(int el13) {
        this.el13 = el13;
    }

    public int getEl12() {
        return el12;
    }

    public void setEl12(int el12) {
        this.el12 = el12;
    }

    public int getEl11() {
        return el11;
    }

    public void setEl11(int el11) {
        this.el11 = el11;
    }

    public int getEl10() {
        return el10;
    }

    public void setEl10(int el10) {
        this.el10 = el10;
    }

    public int getApuesta() {
        return apuesta;
    }

    public void setApuesta(int apuesta) {
        this.apuesta = apuesta;
    }
}
