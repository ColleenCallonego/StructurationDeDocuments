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
import java.util.Scanner;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

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
        collection.aggregate(Arrays.asList(
                Aggregates.lookup("utilisateur", "universites", "universite", "Utilisateur"),
                Aggregates.unwind("$Utilisateur"),
                Aggregates.match(eq("Utilisateur.login", log)),
                Aggregates.match(Filters.expr(" { $eq: [ \"$formations\" , \"$Utilisateur.formations.formation\"] } ")),
                Aggregates.match(Filters.expr("[\"$roles\", \"$Utilisateur.role\"]")),
                Aggregates.project(Projections.exclude("Utilisateur", "_id"))
        )).forEach(doc -> System.out.println(doc.toJson()));



        /*ArrayList<Bson> requestTitre = new ArrayList<>();
        requestTitre.add(Aggregates.lookup("utilisateur", "universites", "universite", "Utilisateur"));
        requestTitre.add(Aggregates.unwind("$Utilisateur"));
        requestTitre.add(Aggregates.match(Filters.eq("Utilisateur.login", log)));
        requestTitre.add(Aggregates.match(Filters.expr(" { $eq: [ \"$formations\" , \"$Utilisateur.formations.formation\"] } ")));
        requestTitre.add(Aggregates.match(Filters.expr(" [ \"$roles\" , \"$Utilisateur.role\"] ")));
        requestTitre.add(Aggregates.project(Projections.exclude("Utilisateur", "_id")));*/

        /*requestTitre.add(Aggregates.group("$type", Accumulators.sum("count", 1)));
        requestTitre.add(Aggregates.limit(5));*/



        /*collection.aggregate(requestTitre)
                .forEach(doc ->  System.out.println(doc.toJson()));*/


    }

    public static void rechercheMotsCles(MongoCollection<Document> collection) {
        //PAS ENCORE FAIT !!!!!
        System.out.println("\n  Recherche par mots clès");
        ArrayList<Bson> request1 = new ArrayList<>();
        request1.add(Aggregates.group("$type", Accumulators.sum("count", 1)));
        request1.add(Aggregates.limit(5));
        collection.aggregate(request1)
                .forEach(doc ->  System.out.println(doc.toJson()));


    }

    public static void rechercheThematique(MongoCollection<Document> collection) {
        //PAS ENCORE FAIT !!!!!
        System.out.println("\n  Recherche par thématique");
        ArrayList<Bson> request1 = new ArrayList<>();
        request1.add(Aggregates.group("$type", Accumulators.sum("count", 1)));
        request1.add(Aggregates.limit(5));
        collection.aggregate(request1)
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
}
