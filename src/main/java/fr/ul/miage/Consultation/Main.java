package fr.ul.miage.Consultation;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Aggregates.lookup;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Aggregates.project;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.text;

public class Main {
    public static String log;
    public static void main (String[] args){
        String n = System.getProperty("line.separator");
        System.out.println("Bienvenue dans le logiciel de consultation de document" + n
                            + "Entrez votre login pour vous connectez :");
        Scanner scanner = new Scanner(System.in);
        String login = scanner.nextLine();
        Boolean ok = connect(login);
        while (!ok){
            System.out.println("Login incorrect ! Recommencez :");
            login = scanner.nextLine();
            ok = connect(login);
        }
        System.out.println("Connection réussie !");
        log = login;
        ecranPrincipal();
    }

    public static Boolean connect(String login){
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("projet");
        MongoCollection<Document> collectionUtilisateur = database.getCollection("utilisateur");
        Long present = collectionUtilisateur.countDocuments(eq("login", login));
        return (present != 0);
    }

    public static MongoCollection<Document> getCollectionOeuvre(){
        // Creation de la connexion au serveur MongoDB
        MongoClient mongoClient = MongoClients.create();
        // Sélection de la base de données
        MongoDatabase database = mongoClient.getDatabase("projet");
        // Selection de la collection
        MongoCollection<Document> collection = database.getCollection("oeuvre");
        return collection;
    }

    public static void ecranPrincipal(){
        MongoCollection<Document> collectionOeuvre = getCollectionOeuvre();
        Integer rep = menu();
        while (rep != 0){
            switch (rep){
                case 1:
                    rechercheTitre(collectionOeuvre);
                    break;
                case 2:
                    rechercheMotsCles(collectionOeuvre);
                    break;
                case 3:
                    rechercheThematique(collectionOeuvre);
                    break;
                case 4:
                    listeMieuxNotees(collectionOeuvre);
                    break;
                case 5:
                    listeCommenteeRecemment(collectionOeuvre);
                    break;
            }
            rep = menu();
        }
    }

    public static Integer menu(){
        String n = System.getProperty("line.separator");
        Scanner scan = new Scanner(System.in);
        System.out.println("Bienvenue " + log + n
                + "Que souhaitez vous faire ? (Entrez le numéro de l'action voulue)" + n
                + "1. Une recherche par titre" + n
                + "2. Une recherche par mots-clés" + n
                + "3. Une recherche par thématique" + n
                + "4. Afficher une liste des publications les mieux 10 (10 oeuvres)" + n
                + "5. Afficher une liste des publications commentées récemment (10 oeuvres)" + n
                + "0. Se déconnecter");
        Integer choix = scan.nextInt();
        return choix;
    }

    public static void rechercheTitre(MongoCollection<Document> collection) {
        System.out.println("\n  Recherche par titre");
        System.out.println("Entrez le titre de l'oeuvre recherché");
        String titre = recupSaisie();
        ArrayList<Bson> requestTitre = new ArrayList<>();
        requestTitre = restrictionAcces(requestTitre);
        requestTitre.add(match(eq("titre", titre)));
        collection.aggregate(requestTitre)
                .forEach(doc ->  System.out.println(doc.toJson()));
    }

    public static void rechercheMotsCles(MongoCollection<Document> collection) {
        System.out.println("\n  Recherche par mots clès");
        System.out.println("Entrez les mots clès recherchés");
        String motsCles = recupSaisie();
        ArrayList<Bson> requestTitre = new ArrayList<>();
        requestTitre.add(match(text(motsCles)));
        requestTitre = restrictionAcces(requestTitre);
        collection.aggregate(requestTitre)
                .forEach(doc ->  System.out.println(doc.toJson()));
    }

    public static void rechercheThematique(MongoCollection<Document> collection) {
        System.out.println("\n  Recherche par thématique");
        System.out.println("Entrez la thématique recherchée");
        String thematique = recupSaisie();
        ArrayList<Bson> requestTitre = new ArrayList<>();
        requestTitre = restrictionAcces(requestTitre);
        requestTitre.add(match(eq("thematique", thematique)));
        collection.aggregate(requestTitre)
                .forEach(doc ->  System.out.println(doc.toJson()));
    }

    public static void listeMieuxNotees(MongoCollection<Document> collection) {
        //PAS ENCORE FAIT !!!!!
        System.out.println("\n  Liste des 10 meilleurs notes");
        ArrayList<Bson> request1 = new ArrayList<>();
        request1.add(Aggregates.group("$type", Accumulators.sum("count", 1)));
        request1.add(Aggregates.limit(5));
        collection.aggregate(request1)
                .forEach(doc ->  System.out.println(doc.toJson()));


    }

    public static void listeCommenteeRecemment(MongoCollection<Document> collection) {
        //PAS ENCORE FAIT !!!!!
        System.out.println("\n  Liste des 10 les plus récemment commentés");
        ArrayList<Bson> request1 = new ArrayList<>();
        request1.add(Aggregates.group("$type", Accumulators.sum("count", 1)));
        request1.add(Aggregates.limit(5));
        collection.aggregate(request1)
                .forEach(doc ->  System.out.println(doc.toJson()));


    }

    public static void addCommentaire (MongoCollection<Document> collection, String commentaire, String titre, String datePublicationOeuvre, Integer note){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date datePublicationO = new Date();
        try {
            datePublicationO = new SimpleDateFormat("yyyy-MM-dd").parse(datePublicationOeuvre);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Document doc = new Document();
        doc.put("titre", titre);
        doc.put("datePublicationOeuvre", formatter.format(datePublicationO));
        Document d = new Document();
        d.put("auteur", log);
        d.put("oeuvre", doc);
        d.put("contenu", commentaire);
        d.put("note", note);
        collection.insertOne(d);
        updateNoteMoyenneOeuvre(collection, titre, datePublicationOeuvre);
    }

    public static void updateNoteMoyenneOeuvre(MongoCollection<Document> collectionCommentaire, String titre, String datePublicationOeuvre){
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("projet");
        MongoCollection<Document> collectionOeuvre = database.getCollection("oeuvre");
        Document moyenne = collectionCommentaire.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.eq("oeuvre.titre", titre)),
                        Aggregates.match(Filters.eq("oeuvre.datePublicationOeuvre", datePublicationOeuvre)),
                        Aggregates.group("$oeuvre.titre", Accumulators.avg("noteMoyenne", "$note"))
                )
        ).first();
        Document d = new Document();
        d.put("NoteMoyenne", moyenne.get("noteMoyenne"));
        collectionOeuvre.updateOne(and(eq("titre", titre), eq("datePublication", datePublicationOeuvre)), new Document("$set", d));
    }

    public static ArrayList<Bson> restrictionAcces (ArrayList<Bson> liste){
        //restriction d'accès en fonction de l'université de la formation et du rôle
        liste.add(lookup("utilisateur", "universites", "universite", "Utilisateur"));
        liste.add(unwind("$Utilisateur"));
        liste.add(match(eq("Utilisateur.login", log)));
        liste.add(match(in("$expr", Arrays.asList("$Utilisateur.formation", "$formations"))));
        liste.add(match(in("$expr", Arrays.asList("$Utilisateur.role", "$roles"))));
        liste.add(project(Projections.exclude("Utilisateur", "_id")));
        return liste;
    }

    public static String recupSaisie(){
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
