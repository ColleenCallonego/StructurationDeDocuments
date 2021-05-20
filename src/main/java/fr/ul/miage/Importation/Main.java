package fr.ul.miage.Importation;

import com.mongodb.client.*;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import javax.print.Doc;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
        getDocumentsToInsert(collectionOeuvre, collectionFormation, collectionPlace, collectionUtilisateur);
        collectionOeuvre.createIndex(Indexes.text("contenu"));
    }

    /**
     * Insert les nouvelles oeuvres dans la base de données
     * @param collectionOeuvre collection qui contient toutes les nouvelles oeuvres a ajouter à la base
     * @param collectionFormation collection qui contient toutes les nouvelles formations a ajouter à la base
     * @param collectionPlace collection qui contient toutes les nouvelles places a ajouter à la base
     * @param collectionUtilisateur collection qui contient toutes les nouveaux utilisateurs a ajouter à la base
     */
    public static void getDocumentsToInsert (MongoCollection<Document> collectionOeuvre, MongoCollection<Document> collectionFormation, MongoCollection<Document> collectionPlace, MongoCollection<Document> collectionUtilisateur){
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
    }

    /**
     * Créer une nouvelle oeuvre
     * @param collectionOeuvre collection qui contient toutes les nouvelles oeuvres a ajouter à la base
     * @param collectionFormation collection qui contient toutes les nouvelles formations a ajouter à la base
     * @param collectionPlace collection qui contient toutes les nouvelles places a ajouter à la base
     * @param collectionUtilisateur collection qui contient toutes les nouveaux utilisateurs a ajouter à la base
     * @param scanner scanner qui permet de lire le contenu des fichier à importer
     */
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
                createAuteur(collectionUtilisateur, auteur.nom, auteur.prenom, transformString(roles), transformString(universites), transformString(formations), date);
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

    /**
     * Vérifie si le fichier contient une oeuvre déjà présente dans la base de données
     * @param collection collection qui contient les différentes oeuvres présente dans le dossier d'importation
     * @param titre titre de l'oeuvre analysée
     * @param date date de publication de l'oeuvre analysée
     * @return True si l'oeuvre n'est pas présente, False sinon
     */
    public static Boolean notExistOeuvre(MongoCollection<Document> collection, String titre, String date){
        Long present = collection.countDocuments(and(eq("titre", titre), eq("datePublication", date)));
        return (present == 0);
    }

    /**
     * Transforme un String en TreeSet<String>
     * @param s le string à transformer
     * @return le TreeSet obtenu
     */
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

    /**
     * Transforme un string en ArrayList<Auteur>
     * @param s le string à transformer contient toutes les infos des auteurs
     * @return la liste d'auteur
     */
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

    /**
     * Créer une nouvelle formation
     * @param collectionFormation collection qui contient toutes les nouvelles formations a ajouter à la base
     * @param collectionPlace collection qui contient toutes les nouvelles places a ajouter à la base
     * @param universites liste des universités qui posède cette formation
     * @param formations liste des noms de formations
     */
    public static void createFormation (MongoCollection<Document> collectionFormation, MongoCollection<Document> collectionPlace, TreeSet<String> universites, TreeSet<String> formations){
        for (String formation : formations) {
            listFormations.add(formation);
            listUniversites.addAll(universites);
            ArrayList<String> niveaux = new ArrayList<>();
            niveaux.add("BAC +2");
            niveaux.add("BAC +3");
            niveaux.add("BAC +5");
            Random rand = new Random();
            String niveau = niveaux.get(rand.nextInt(niveaux.size())); //choisi alléatoirement un élément de la liste niveaux
            TreeSet<String> l = notExistFormation(collectionFormation, formation);
            if (l.isEmpty()){//si il n'y a aucune université qui propose cette formation alors on la créer
                Document d = new Document();
                d.put("nom", formation);
                d.put("niveau", niveau);
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

    /**
     * Vérifie si le fichier contient une formation déjà présente dans la base de données
     * @param collection collection qui contient les différentes formation présente dans les fichiers du dossier d'importation
     * @param nom nom de la formation
     * @return une liste des universités qui propose cette formation
     */
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

    /**
     * Construit le login d'un auteur
     * @param collection collection qui contient toutes les nouveaux utilisateurs a ajouter à la base
     * @param nom nom de l'auteur
     * @param prenom prénom de l'auteur
     * @return le login d'un auteur
     */
    public static String getLoginAuteur (MongoCollection<Document> collection, String nom, String prenom){
        Document d = collection.find(eq("nom", nom)).first();
        return (String)d.get("login");
    }

    /**
     * Créer un nouvel auteur
     * @param collection collection qui contient toutes les nouveaux utilisateurs a ajouter à la base
     * @param nom nom du nouvel auteur
     * @param prenom prénom du nouvel auteur
     * @param roles rôles du nouvel auteur
     * @param universites universités du nouvel auteur
     * @param formations formations du nouvel auteur
     * @param datePubli date de publication de l'oeuvre du nouvel auteur (pour mettre à jour les dates de début et de fin de formation)
     */
    public static void createAuteur (MongoCollection<Document> collection, String nom, String prenom, TreeSet<String> roles, TreeSet<String> universites, TreeSet<String> formations, String datePubli){
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
            d.put("universite", universites.first());
            d.put("formation", formations.first());
            Document doc;
            ArrayList<Document> list = new ArrayList<Document>();
            for(String formation : formations){
                doc = new Document();
                doc.put("formation", formation);
                doc.put("debut", datePubli);
                doc.put("fin", datePubli);
                list.add(doc);
            }
            d.put("histoFormations", list);
            d.put("role", roles.first());
            collection.insertOne(d);
        }
        else{
            Auteur a = getInfoAuteur(collection, nom, prenom);
            Document doc;
            ArrayList<Document> histoFormations = new ArrayList<Document>();
            for(String formation : formations){
                Boolean trouve = false;
                for(Formation f : a.formations){
                    if(formation.equals(f.formation)){
                        trouve = true;
                        Date date = new Date();
                        try {
                            date = new SimpleDateFormat("yyyy-MM-dd").parse(datePubli);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        f.setFormation(date);
                    }
                }
                if(!trouve){
                    Date date = new Date();
                    try {
                        date = new SimpleDateFormat("yyyy-MM-dd").parse(datePubli);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    a.formations.add(new Formation(formation, date, date));
                }
            }
            Date date = new Date();
            try {
                date = new SimpleDateFormat("yyyy-MM-dd").parse(datePubli);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date date2 = date;
            Boolean changeUniversite = false;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            for(Formation form : a.formations){
                if (date.before(date2)){
                    changeUniversite = false;
                }
                else if(form.fin.before(date)){
                    changeUniversite = true;
                    date2 = form.fin;
                }
                doc = new Document();
                doc.put("formation", form.formation);
                doc.put("debut", formatter.format(form.debut));
                doc.put("fin", formatter.format(form.fin));
                histoFormations.add(doc);
            }
            if(changeUniversite){
                d.put("universite", universites.first());
                d.put("formation", formations.first());
            }
            d.put("histoFormations", histoFormations);
            d.put("role", roles.first());
            collection.updateOne(and(eq("nom", nom), eq("prenom", prenom)), new Document("$set", d));
        }
    }

    /**
     * Vérifie si u auteur existe déjà dans la base de données
     * @param collection collection qui contient toutes les nouveaux auteurs a ajouter à la base
     * @param nom nom de l'auteur
     * @param prenom prénom de l'auteur
     * @return True si l'auteur n'existe pas encore, False sinon
     */
    public static Boolean notExistAuteur (MongoCollection<Document> collection, String nom, String prenom){
        Long present = collection.countDocuments(and(eq("nom", nom), eq("prenom", prenom)));
        return (present == 0);
    }

    /**
     * Récupère les information liées à l'auteur
     * @param collection collection qui contient toutes les nouveaux utilisateurs a ajouter à la base
     * @param nom nom de l'auteur
     * @param prenom prénom de l'auteur
     * @return un Auteur
     */
    public static Auteur getInfoAuteur (MongoCollection<Document> collection, String nom, String prenom){
        Auteur a;
        ArrayList<Formation> listFormations = new ArrayList<Formation>();
        Document d = collection.find(and(eq("nom", nom), eq("prenom", prenom))).first();
        ArrayList<Document> documents = (ArrayList<Document>) d.get("histoFormations");
        for(Document doc : documents){
            Date debut = new Date();
            Date fin = new Date();
            try {
                debut = new SimpleDateFormat("yyyy-MM-dd").parse((String)doc.get("debut"));
                fin = new SimpleDateFormat("yyyy-MM-dd").parse((String)doc.get("fin"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            listFormations.add(new Formation((String)doc.get("formation"), debut, fin));
        }
        a = new Auteur(nom, prenom, (String)d.get("universite"), (String)d.get("formation"), listFormations);
        return a;
    }

    /**
     * Gère les login doublons
     * @param collection collection qui contient toutes les nouveaux utilisateurs a ajouter à la base
     * @param nom nom de l'utilisateur
     * @param prenom prénom de l'utilisateur
     * @return le nombre d'occurrence du login dans la base de données
     */
    public static Long sameLogin (MongoCollection<Document> collection, String nom, String prenom){
        return collection.countDocuments(regex("login", (nom + (prenom.charAt(0)))));
    }

    /**
     * Créer le nombre de place d'une formation
     * @param collection collection qui contient toutes les nouvelles places a ajouter à la base
     * @param universite nom de l'université
     * @param formation nom de la formation
     */
    public static void createPlace(MongoCollection<Document> collection, String universite, String formation){
        Random rand = new Random();
        int min = 50;
        int max = 500;
        String nbPlaceAnnee = String.valueOf(rand.nextInt(max-min)+min); //choisi un nombre de place aléatoire entre min et max et transforme le int en Strinng
        Document d = new Document();
        d.put("universite", universite);
        d.put("formation", formation);
        d.put("nbPlaceAnnee", nbPlaceAnnee);
        collection.insertOne(d);
    }
}
