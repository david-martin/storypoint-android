package me.dmkube.storypointme;

/**
 * Created by dmartin on 14/02/18.
 */

class Pointer {

    private String name;
    private String score;

    Pointer(String name) {
        this.name = name;
    }

    public Pointer(String name, String score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    @Override
    public String toString() {
        String scoreAsString = "-";
        if (score != null) {
            scoreAsString = score;
        }

        // TODO: obfuscation of score?
        return String.format("%s : %s", name, scoreAsString);
    }
}
