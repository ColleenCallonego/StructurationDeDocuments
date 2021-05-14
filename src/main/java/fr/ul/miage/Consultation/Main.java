package fr.ul.miage.Consultation;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

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
}
