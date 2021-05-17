package fr.ul.miage.Importation;

import java.util.Date;

public class Formation {
    public String formation;
    public Date debut;
    public Date fin;

    public Formation(String formation, Date debut, Date fin) {
        this.formation = formation;
        this.debut = debut;
        this.fin = fin;
    }

    public void setFormation(Date d){
        if (this.fin.before(d)){
            fin = d;
        }
        else if (this.debut.after(d)){
            debut = d;
        }
    }
}
