package fr.ul.miage.Consultation;

import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Main {
    public static String log;
    public static void main (String[] args){
        /*String n = System.getProperty("line.separator");
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
        ecranPrincipal();*/
        log = "McdonnellC";
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("projet");
        MongoCollection<Document> collectionUtilisateur = database.getCollection("commentaire");
        String s = "60a26f49f0808914f9244524";
        ObjectId o = new ObjectId(s);
        addCommentaire(collectionUtilisateur, "C'est très bien", "Les oubliés d'encre", "2011-03-15" , 8);
    }

    public static Boolean connect(String login){
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("projet");
        MongoCollection<Document> collectionUtilisateur = database.getCollection("utilisateur");
        Long present = collectionUtilisateur.countDocuments(eq("login", login));
        return (present != 0);
    }

    public static void ecranPrincipal(){
        Integer rep = menu();
        while (rep != 0){
            switch (rep){
                case 1:
                    rechercheTitre();
                    break;
                case 2:
                    rechercheMotsCles();
                    break;
                case 3:
                    rechercheThematique();
                    break;
                case 4:
                    listeMieuxNotees();
                    break;
                case 5:
                    listeCommenteeRecemment();
                    break;
            }
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

    public static void rechercheTitre() {

    }

    public static void rechercheMotsCles() {

    }

    public static void rechercheThematique() {

    }

    public static void listeMieuxNotees() {

    }

    public static void listeCommenteeRecemment() {

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
}
