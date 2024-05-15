package ismaapp.tortosa.glucoseregister.entities;

import androidx.annotation.NonNull;

public class GlucoseLevels {
    //Valores standards
    private int id;
    private int levelMax;
    private int levelMin;

    public GlucoseLevels (int levelMax, int levelMin) {
        this.levelMax = levelMax;
        this.levelMin = levelMin;
    }

    public GlucoseLevels () {

    }

    public int getLevelMax() {
        return levelMax;
    }

    public int getLevelMin() {
        return levelMin;
    }
    public int getId() {return id;}

    public void setLevelMax(int levelMax) {
        this.levelMax = levelMax;
    }

    public void setLevelMin(int levelMin) {
        this.levelMin = levelMin;
    }

    public void setId(int id) { this.id = id; }

    //Método para validar y asegurar que levelMin siempre sea menor que levelMax.
    public void setLevels(int newLevelMax, int newLevelMin) {
        if (newLevelMin < newLevelMax) {
            this.levelMax = newLevelMax;
            this.levelMin = newLevelMin;
        } else {
            throw new IllegalArgumentException("El nivel mínimo debe ser menor que el nivel máximo.");
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "GlucoseLevels{" +
                "id=" + id +
                ", levelMax=" + levelMax +
                ", levelMin=" + levelMin +
                '}';
    }
}
