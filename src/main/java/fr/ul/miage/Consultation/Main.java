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
    public static void main (String[] args){
        System.out.println("Bienvenue dans le logiciel de consultation de document");
        System.out.println("Entrez votre login pour vous connectez :");
        Scanner scanner = new Scanner(System.in);
        String login = scanner.nextLine();
        Boolean ok = connect(login);
        while (!ok){
            System.out.println("Login incorrect ! Recommencez :");
            login = scanner.nextLine();
            ok = connect(login);
        }
        System.out.println("Connection réussie !");
    }

    public static Boolean connect(String login){
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("projet");
        MongoCollection<Document> collectionUtilisateur = database.getCollection("utilisateur");
        Long present = collectionUtilisateur.countDocuments(eq("login", login));
        return (present != 0);
    }
}
