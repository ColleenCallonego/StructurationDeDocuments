package fr.ul.miage.Consultation;

public class Commentaire {
    public String login;
    public String contenu;
    public String note;

    public Commentaire(String login, String contenu, String note) {
        this.login = login;
        this.contenu = contenu;
        this.note = note;
    }

    @Override
    public String toString() {
        String n = System.getProperty("line.separator");
        return  "---------------------------------------------------------" + n +
                "Commentaire : " + n +
                "De : " + login + n +
                "Note : " + note + n +
                "Commentaire : "  + contenu;
    }
}
