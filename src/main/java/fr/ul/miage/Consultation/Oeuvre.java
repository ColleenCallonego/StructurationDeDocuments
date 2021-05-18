package fr.ul.miage.Consultation;

import fr.ul.miage.Importation.Auteur;

import java.util.ArrayList;

public class Oeuvre {
    public String titre;
    public ArrayList<String> auteurs;
    public String nbPages;
    public String datePublication;
    public String thematique;
    public String universites;
    public String formations;
    public String contenu;
    public Object noteMoyenne;

    public Oeuvre(String titre, ArrayList<String> auteurs, String nbPages, String datePublication, String thematique, String universites, String formations, String contenu, Object noteMoyenne) {
        this.titre = titre;
        this.auteurs = auteurs;
        this.nbPages = nbPages;
        this.datePublication = datePublication;
        this.thematique = thematique;
        this.universites = universites;
        this.formations = formations;
        this.contenu = contenu;
        this.noteMoyenne = noteMoyenne;
    }

    public String toStringRecap(){
        String n = System.getProperty("line.separator");
        return  "Titre : " + titre + n +
                "Date du publication : " + datePublication + n +
                "Auteurs : " + auteurs + n +
                "Note moyenne : " + noteMoyenne;
    }

    @Override
    public String toString() {
        String n = System.getProperty("line.separator");
        return  "Titre : " + titre + n +
                "Date du publication : " + datePublication + n +
                "Auteurs : " + auteurs + n +
                "Thématique :  " + thematique + n +
                "Nombre de pages : " + nbPages + n +
                "Note moyenne : " + noteMoyenne + n +
                "Universités qui y ont accès : " + universites + n +
                "Formations qui y ont accès : " + formations + n +
                "---------------------------------------------------------" + n +
                contenu;
    }
}
