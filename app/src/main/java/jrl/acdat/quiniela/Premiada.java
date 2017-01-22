package jrl.acdat.quiniela;

enum Categoria {
    ESPECIAL,       // 14 aciertos + el pleno al 15
    PRIMERA,        // 14 aciertos
    SEGUNDA,        // 13 aciertos
    TERCERA,        // 12 aciertos
    CUARTA,         // 11 aciertos
    QUINTA          // 10 aciertos
}

public class Premiada {

    private String temporada;
    private String jornada;
    private String apuesta;
    private Categoria categoria;
    private float premio;

    public String getTemporada() {
        return temporada;
    }

    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    public String getApuesta() {
        return apuesta;
    }

    public void setApuesta(String apuesta) {
        this.apuesta = apuesta;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public float getPremio() {
        return premio;
    }

    public void setPremio(float premio) {
        this.premio = premio;
    }
}
