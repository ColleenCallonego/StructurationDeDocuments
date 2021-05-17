package fr.ul.miage.Importation;

import java.util.ArrayList;

public class Auteur {
    public String nom;
    public String prenom;
    public String formation;
    public String universite;
    public ArrayList<Formation> formations;

    public Auteur(String prenom, String nom) {
        this.nom = nom;
        this.prenom = prenom;
    }

    public Auteur(String nom, String prenom, String formation, String universite, ArrayList<Formation> formations) {
        this.nom = nom;
        this.prenom = prenom;
        this.formation = formation;
        this.universite = universite;
        this.formations = formations;
    }
}
