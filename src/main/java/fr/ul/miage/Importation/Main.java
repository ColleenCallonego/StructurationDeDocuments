package fr.ul.miage.Importation;

import com.mongodb.client.*;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

import static com.mongodb.client.model.Filters.*;

public class Main {
    public static ArrayList<String> listAuteurs = new ArrayList<String>();
    public static ArrayList<String> listUniversites = new ArrayList<String>();
    public static ArrayList<String> listFormations = new ArrayList<String>();

    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("projet");
        MongoCollection<Document> collectionOeuvre = database.getCollection("oeuvre");
        MongoCollection<Document> collectionFormation = database.getCollection("formation");
        MongoCollection<Document> collectionUtilisateur = database.getCollection("utilisateur");
        MongoCollection<Document> collectionPlace = database.getCollection("place");
        getDocumentsToUpsert(collectionOeuvre, collectionFormation, collectionPlace, collectionUtilisateur);
        collectionOeuvre.createIndex(Indexes.text("contenu"));
    }

    public static ArrayList<Document> getDocumentsToUpsert (MongoCollection<Document> collectionOeuvre, MongoCollection<Document> collectionFormation, MongoCollection<Document> collectionPlace, MongoCollection<Document> collectionUtilisateur){
        ArrayList<Document> list = new ArrayList<Document>();
        try {
            File folder = new File("src/main/resources/import");
            for (File fileEntry : folder.listFiles()) {
                listAuteurs = new ArrayList<String>();
                listUniversites = new ArrayList<String>();
                listFormations = new ArrayList<String>();
                System.out.println(fileEntry.getName());
                // Le fichier d'entrée
                FileInputStream file = null;
                file = new FileInputStream(fileEntry.getPath());
                Scanner scanner = new Scanner(file);
                createOeuvre(collectionOeuvre, collectionFormation, collectionPlace, collectionUtilisateur, scanner);
            }
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        return list;
    }

    public static void createOeuvre(MongoCollection<Document> collectionOeuvre, MongoCollection<Document> collectionFormation, MongoCollection<Document> collectionPlace, MongoCollection<Document> collectionUtilisateur, Scanner scanner){
        Document d = new Document();
        String titre = "";
        String date = "";
        String pages = "";
        String theme = "";
        String auteurs = "";
        String universites = "";
        String formations = "";
        String roles = "";
        String contenu = "";
        String s = "";
        Integer pos;
        //renvoie true s'il y a une autre ligne à lire
        while (scanner.hasNextLine()) {
            s = scanner.nextLine();
            pos = s.indexOf(":");
            if (pos != -1){
                switch (s.substring(0, pos)){
                    case "Titre":
                        titre = s.substring(pos + 2);
                        break;
                    case "Auteurs":
                        auteurs = s.substring(pos + 2);
                        break;
                    case "Pages":
                        pages = s.substring(pos + 2);
                        break;
                    case "Publication":
                        date = s.substring(pos + 2);
                        break;
                    case "Theme":
                        theme = s.substring(pos + 2);
                        break;
                    case "Formations":
                        formations = s.substring(pos + 2);
                        break;
                    case "Universites":
                        universites = s.substring(pos + 2);
                        break;
                    case "Roles":
                        roles = s.substring(pos + 2);
                        break;
                    case "Contenu":
                        while(scanner.hasNextLine()){
                            contenu += scanner.nextLine();
                        }
                        break;
                }
            }
            //System.out.println(scanner.nextLine());
        }
        if (notExistOeuvre(collectionOeuvre, titre, date)){
            d.put("titre", titre);
            d.put("nbPages", pages);
            d.put("datePublication", date);
            d.put("thematique", theme);
            d.put("contenu", contenu);
            createFormation(collectionFormation, collectionPlace, transformString(universites), transformString(formations));
            for (Auteur auteur : transformStringAuteurs(auteurs)){
                createAuteur(collectionUtilisateur, auteur.nom, auteur.prenom, transformString(roles), transformString(universites), transformString(formations));
                listAuteurs.add(getLoginAuteur(collectionUtilisateur, auteur.nom, auteur.prenom));
            }
            d.put("auteurs", listAuteurs);
            d.put("universites", listUniversites);
            d.put("formations", listFormations);
            d.put("roles", transformString(roles));
            d.put("noteMoyenne", 0);
            collectionOeuvre.insertOne(d);
        }
    }

    public static Boolean notExistOeuvre(MongoCollection<Document> collection, String titre, String date){
        Long present = collection.countDocuments(and(eq("titre", titre), eq("datePublication", date)));
        return (present == 0);
    }

    public static TreeSet<String> transformString (String s){
        TreeSet<String> list = new TreeSet<String>();
        String temp = s;
        Integer pos = temp.indexOf(",");
        if (pos == -1){
            list.add(temp);
        }
        while (pos != -1){
            list.add(temp.substring(0, pos));
            temp = temp.substring(pos + 1);
            pos = temp.indexOf(",");
        }
        return list;
    }

    public static ArrayList<Auteur> transformStringAuteurs(String s){
        ArrayList<Auteur> list = new ArrayList<Auteur>();
        String temp = s;
        Integer pos1 = temp.indexOf(",");
        Integer pos2;
        if (pos1 == -1){
            pos2 = temp.indexOf(" ");
            list.add(new Auteur(temp.substring(0, pos2), temp.substring(pos2 + 1)));
        }
        while (pos1 != -1){
            pos2 = temp.indexOf(" ");
            list.add(new Auteur(temp.substring(0, pos2), temp.substring(pos2 + 1, pos1)));
            temp = temp.substring(pos1 + 2);
            pos1 = temp.indexOf(",");
        }
        return list;
    }

    //AJOUTER ALEATOIRE SUR NIVEAU DE FORMATION
    public static void createFormation (MongoCollection<Document> collectionFormation, MongoCollection<Document> collectionPlace, TreeSet<String> universites, TreeSet<String> formations){
        for (String formation : formations) {
            listFormations.add(formation);
            listUniversites.addAll(universites);
            TreeSet<String> l = notExistFormation(collectionFormation, formation);
            if (l.isEmpty()){
                Document d = new Document();
                d.put("nom", formation);
                d.put("niveau", " ");
                d.put("universites", universites);
                collectionFormation.insertOne(d);
            }
            else{
                Document d = new Document();
                l.addAll(universites);
                d.put("universites", l);
                collectionFormation.updateOne(eq("nom", formation), new Document("$set", d));
                for (String univ : universites){
                    createPlace(collectionPlace, univ, formation);
                }
            }
        }
    }

    public static TreeSet<String> notExistFormation (MongoCollection<Document> collection, String nom){
        TreeSet<String> list = new TreeSet<String>();
        Long present = collection.countDocuments(eq("nom", nom));
        if (present != 0){
            Document d = collection.find(eq("nom", nom)).first();
            ArrayList<String> l = (ArrayList<String>) d.get("universites");
            list = new TreeSet<String>(l);
        }
        return list;
    }

    public static String getLoginAuteur (MongoCollection<Document> collection, String nom, String prenom){
        Document d = collection.find(eq("nom", nom)).first();
        return (String)d.get("login");
    }

    //AJOUTER BON TRAITEMENT POUR UNIVERISTE, FORMATIONS & ROLE
    public static void createAuteur (MongoCollection<Document> collection, String nom, String prenom, TreeSet<String> roles, TreeSet<String> universite, TreeSet<String> formations){
        Document d = new Document();
        if (notExistAuteur(collection, nom, prenom)){
            Long nb = sameLogin(collection, nom, prenom);
            if (nb == 0){
                d.put("login", nom + prenom.charAt(0));
            }
            else{
                d.put("login", nom + prenom.charAt(0) + (nb));
            }
            d.put("nom", nom);
            d.put("prenom", prenom);
            d.put("universite", universite.first());
            Document doc;
            ArrayList<Document> list = new ArrayList<Document>();
            for(String formation : formations){
                doc = new Document();
                doc.put("formation", formation);
                doc.put("debut", " ");
                doc.put("fin", " ");
                list.add(doc);
            }
            d.put("formations", list);
            d.put("role", roles.first());
            collection.insertOne(d);
        }
        else{
            Document doc;
            ArrayList<Document> list = new ArrayList<Document>();
            for(String formation : formations){
                doc = new Document();
                doc.put("formation", formation);
                doc.put("debut", " ");
                doc.put("fin", " ");
                list.add(doc);
            }
            d.put("formations", list);
            d.put("role", roles.first());
            collection.updateOne(and(eq("nom", nom), eq("prenom", prenom)), new Document("$set", d), new UpdateOptions().upsert(true));
        }
    }

    public static Boolean notExistAuteur (MongoCollection<Document> collection, String nom, String prenom){
        Long present = collection.countDocuments(and(eq("nom", nom), eq("prenom", prenom)));
        return (present == 0);
    }

    public static Long sameLogin (MongoCollection<Document> collection, String nom, String prenom){
        return collection.countDocuments(regex("login", (nom + (prenom.charAt(0)))));
    }

    public static void createPlace(MongoCollection<Document> collection, String universite, String formation){
        Document d = new Document();
        d.put("universite", universite);
        d.put("formation", formation);
        d.put("nbPlaceAnnee", "100");
        collection.insertOne(d);
    }
}
