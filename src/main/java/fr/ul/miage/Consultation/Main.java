package fr.ul.miage.Consultation;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

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

    /**
     * Vérifie le login entré pour ce connecter au programme
     * @param login login d'un utilisateur
     * @return True si le login est bon (présent dans la base), False sinon
     */
    public static Boolean connect(String login){
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("projet");
        MongoCollection<Document> collectionUtilisateur = database.getCollection("utilisateur");
        Long present = collectionUtilisateur.countDocuments(eq("login", login));
        return (present != 0);
    }

    /**
     * Créer la connection avec la collection oeuvre de la base de données
     * @return la collection oeuvre
     */
    public static MongoCollection<Document> getCollectionOeuvre(){
        // Creation de la connexion au serveur MongoDB
        MongoClient mongoClient = MongoClients.create();
        // Sélection de la base de données
        MongoDatabase database = mongoClient.getDatabase("projet");
        // Selection de la collection
        MongoCollection<Document> collection = database.getCollection("oeuvre");
        return collection;
    }

    /**
     * Affiche le menu et gère la réponse fournise par celui-ci
     */
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

    /**
     * Affichage du menu de choix de recherche
     * @return la réponse du choix de recherche
     */
    public static Integer menu(){
        String n = System.getProperty("line.separator");
        Scanner scan = new Scanner(System.in);
        System.out.println("Bienvenue " + log + n
                + "Que souhaitez vous faire ? (Entrez le numéro de l'action voulue)" + n
                + "1. Une recherche par titre" + n
                + "2. Une recherche par mots-clés" + n
                + "3. Une recherche par thématique" + n
                + "4. Afficher une liste des publications les mieux notées (10 oeuvres)" + n
                + "5. Afficher une liste des publications commentées récemment (10 oeuvres)" + n
                + "0. Se déconnecter");
        Integer choix = scan.nextInt();
        return choix;
    }

    /**
     * Requête de recherche des oeuvres par titre
     * @param collection  collection oeuvre
     */
    public static void rechercheTitre(MongoCollection<Document> collection) {
        ArrayList<Oeuvre> oeuvres = new ArrayList<Oeuvre>();
        System.out.println("\n  Recherche par titre");
        System.out.println("Entrez le titre de l'oeuvre recherché");
        String titre = recupSaisie();
        ArrayList<Bson> requestTitre = new ArrayList<>();
        requestTitre = restrictionAcces(requestTitre);
        requestTitre.add(match(eq("titre", titre)));
        collection.aggregate(requestTitre).forEach(doc ->  oeuvres.add(new Oeuvre(doc.get("titre").toString(), (ArrayList<String>) doc.get("auteurs"), doc.get("nbPages").toString(), doc.get("datePublication").toString(), doc.get("thematique").toString(), doc.get("universites").toString(), doc.get("formations").toString(), doc.get("contenu").toString(), doc.get("noteMoyenne"))));
        afficherOeuvres(collection, oeuvres);
    }

    /**
     * Requête de recherche des oeuvres par mots clès
     * @param collection  collection oeuvre
     */
    public static void rechercheMotsCles(MongoCollection<Document> collection) {
        ArrayList<Oeuvre> oeuvres = new ArrayList<Oeuvre>();
        System.out.println("\n  Recherche par mots clès");
        System.out.println("Entrez les mots clès recherchés");
        String motsCles = recupSaisie();
        ArrayList<Bson> requestTitre = new ArrayList<>();
        requestTitre.add(match(text(motsCles)));
        requestTitre = restrictionAcces(requestTitre);
        collection.aggregate(requestTitre).forEach(doc ->  oeuvres.add(new Oeuvre(doc.get("titre").toString(), (ArrayList<String>) doc.get("auteurs"), doc.get("nbPages").toString(), doc.get("datePublication").toString(), doc.get("thematique").toString(), doc.get("universites").toString(), doc.get("formations").toString(), doc.get("contenu").toString(), doc.get("noteMoyenne"))));
        afficherOeuvres(collection, oeuvres);
    }

    /**
     * Requête de recherche des oeuvres par thématique
     * @param collection  collection oeuvre
     */
    public static void rechercheThematique(MongoCollection<Document> collection) {
        ArrayList<Oeuvre> oeuvres = new ArrayList<Oeuvre>();
        System.out.println("\n  Recherche par thématique");
        System.out.println("Entrez la thématique recherchée");
        String thematique = recupSaisie();
        ArrayList<Bson> requestTitre = new ArrayList<>();
        requestTitre = restrictionAcces(requestTitre);
        requestTitre.add(match(eq("thematique", thematique)));
        collection.aggregate(requestTitre).forEach(doc ->  oeuvres.add(new Oeuvre(doc.get("titre").toString(), (ArrayList<String>) doc.get("auteurs"), doc.get("nbPages").toString(), doc.get("datePublication").toString(), doc.get("thematique").toString(), doc.get("universites").toString(), doc.get("formations").toString(), doc.get("contenu").toString(), doc.get("noteMoyenne"))));
        afficherOeuvres(collection, oeuvres);
    }

    /**
     * Requête de recherche des 10 oeuvres les mieux notées
     * @param collection  collection oeuvre
     */
    public static void listeMieuxNotees(MongoCollection<Document> collection) {
        ArrayList<Oeuvre> oeuvres = new ArrayList<Oeuvre>();
        System.out.println("\n  Liste des 10 meilleurs notes");
        ArrayList<Bson> requestTitre = new ArrayList<>();
        requestTitre = restrictionAcces(requestTitre);
        requestTitre.add(sort(Sorts.orderBy(Sorts.descending("noteMoyenne"), Sorts.ascending("titre"))));
        requestTitre.add(limit(10));
        collection.aggregate(requestTitre).forEach(doc ->  oeuvres.add(new Oeuvre(doc.get("titre").toString(), (ArrayList<String>) doc.get("auteurs"), doc.get("nbPages").toString(), doc.get("datePublication").toString(), doc.get("thematique").toString(), doc.get("universites").toString(), doc.get("formations").toString(), doc.get("contenu").toString(), doc.get("noteMoyenne"))));
        afficherOeuvres(collection, oeuvres);
    }

    /**
     * Requête de recherche des 10 oeuvres les plus récemment commentées
     * @param collection collection oeuvre
     */
    public static void listeCommenteeRecemment(MongoCollection<Document> collection) {
        ArrayList<Oeuvre> oeuvres = new ArrayList<Oeuvre>();
        System.out.println("\n  Liste des 10 les plus récemment commentés");
        ArrayList<Bson> requestTitre = new ArrayList<>();
        requestTitre = restrictionAcces(requestTitre);
        requestTitre.add(lookup("commentaire", "titre", "oeuvre.titre", "Commentaire"));
        requestTitre.add(match(eq("$expr", Arrays.asList("$Commentaire.oeuvre.datePublicationOeuvre", "dateOeuvre"))));
        requestTitre.add(sort(Sorts.orderBy(Sorts.descending("Commentaire.datePublicationComm"), Sorts.ascending("titre"))));
        requestTitre.add(limit(10));
        collection.aggregate(requestTitre).forEach(doc ->  oeuvres.add(new Oeuvre(doc.get("titre").toString(), (ArrayList<String>) doc.get("auteurs"), doc.get("nbPages").toString(), doc.get("datePublication").toString(), doc.get("thematique").toString(), doc.get("universites").toString(), doc.get("formations").toString(), doc.get("contenu").toString(), doc.get("noteMoyenne"))));
        afficherOeuvres(collection, oeuvres);
    }

    /**
     * Permet d'afficher la liste des oeuvres retournées par la requête de recherhe
     * @param collection collection d'oeuvres de la base de données
     * @param oeuvres oeuvres retourner par la requête
     */
    public static void afficherOeuvres(MongoCollection<Document> collection, ArrayList<Oeuvre> oeuvres){
        String n = System.getProperty("line.separator");
        Integer i = 1;
        for(Oeuvre o : oeuvres){
            System.out.println("Oeuvre numéro " + i + " :" + n + o.toStringRecap() + n + n);
            i++;
        }
        System.out.println("Quelle oeuvre souhaitez vous voir ? (Entrez son numéro)");
        Scanner scan = new Scanner(System.in);
        Integer rep = scan.nextInt();
        afficherOeuvre(collection, oeuvres.get(rep - 1), oeuvres.get(rep - 1).titre, oeuvres.get(rep - 1).datePublication);
    }

    /**
     * Permet d'afficher les information et le contenu d'une oeuvre sélectionnée
     * @param collectionOeuvre collection des oeuvres de la base de données
     * @param oeuvre oeuvre sélectionnée
     * @param titre titre de l'oeuvre
     * @param datePublication date de publication de l'oeuvre
     */
    public static void afficherOeuvre(MongoCollection<Document>collectionOeuvre, Oeuvre oeuvre, String titre, String datePublication){
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("projet");
        MongoCollection<Document> collectionCommentaire = database.getCollection("commentaire");
        //récupération des commentaires liés à l'oeuvre
        ArrayList<Commentaire> commentaires = new ArrayList<Commentaire>();
        FindIterable<Document> docs = collectionCommentaire.find(and(eq("oeuvre.titre", titre), eq("oeuvre.datePublicationOeuvre", datePublication)));
        docs.forEach(doc -> commentaires.add(new Commentaire(doc.get("auteur").toString(), doc.get("contenu").toString(), doc.get("note").toString())));
        //affichage données de l'oeuvre
        System.out.println(oeuvre);
        //affichage commentaires liés à l'oeuvre
        if (commentaires.size() == 0){
            System.out.println("Il n'y a aucun commentaire pour cette oeuvre.");
        }
        else{
            System.out.println("Voici les commentaires liés à l'oeuvre : ");
            for(Commentaire c : commentaires){
                System.out.println(c);
            }
        }

        System.out.println("Souhaitez vous déponsez un commentaire et une note ? (Oui ou Non)");
        Scanner scan = new Scanner(System.in);
        String rep = scan.nextLine();
        if (rep.equals("Oui")){
            ecrireCommentaire(collectionOeuvre, collectionCommentaire, titre, datePublication);
        }
    }

    /**
     * Demande les infos nécessaires pour la création d'un commentaire
     * @param collectionOeuvre collection des oeuvres de la base de données
     * @param collectionCommentaire collection des commentaires de la base de données
     * @param titre titre de l'oeuvre commentée
     * @param datePublicationOeuvre date de publication de l'oeuvre
     */
    public static void ecrireCommentaire (MongoCollection<Document> collectionOeuvre, MongoCollection<Document> collectionCommentaire, String titre, String datePublicationOeuvre){
        System.out.println("Ecrivez votre commentaire : (sur une seule ligne)");
        Scanner scan = new Scanner(System.in);
        String commentaire = scan.nextLine();
        System.out.println("Ecrivez votre note : (un nombre entier entre 0 et 10)");
        Integer note = scan.nextInt();
        addCommentaire(collectionOeuvre, collectionCommentaire, commentaire, titre, datePublicationOeuvre, note);
    }

    /**
     * Ajoute un commentaire à une oeuvre
     * @param collectionOeuvre collection des oeuvres de la base de données
     * @param collectionCommentaire collection des commentaires de la base de données
     * @param commentaire contenu du commentaire
     * @param titre titre de l'oeuvre commentée
     * @param datePublicationOeuvre date de publication de l'oeuvre
     * @param note note donnée à l'oeuvre
     */
    public static void addCommentaire (MongoCollection<Document> collectionOeuvre, MongoCollection<Document> collectionCommentaire, String commentaire, String titre, String datePublicationOeuvre, Integer note){
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
        d.put("datePublicationComm", formatter.format(new Date()));
        d.put("oeuvre", doc);
        d.put("contenu", commentaire);
        d.put("note", note);
        collectionCommentaire.insertOne(d);
        updateNoteMoyenneOeuvre(collectionOeuvre,collectionCommentaire, titre, datePublicationOeuvre);
    }

    /**
     * Permet de mettre à jour la note moyenne d'un oeuvre à partir de ses commentaires
     * @param collectionOeuvre collection des oeuvres de la base de données
     * @param collectionCommentaire collection des commentaires de la base de données
     * @param titre titre de l'oeuvre
     * @param datePublicationOeuvre date de publication de l'oeuvre
     */
    public static void updateNoteMoyenneOeuvre(MongoCollection<Document> collectionOeuvre, MongoCollection<Document> collectionCommentaire, String titre, String datePublicationOeuvre){
        Document moyenne = collectionCommentaire.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.eq("oeuvre.titre", titre)),
                        Aggregates.match(Filters.eq("oeuvre.datePublicationOeuvre", datePublicationOeuvre)),
                        Aggregates.group("$oeuvre.titre", Accumulators.avg("noteMoyenne", "$note"))
                )
        ).first();
        Document d = new Document();
        d.put("noteMoyenne", moyenne.get("noteMoyenne"));
        collectionOeuvre.updateOne(and(eq("titre", titre), eq("datePublication", datePublicationOeuvre)), new Document("$set", d));
    }

    /**
     * Partie de la requête qui gère les restrictions d'accès liée à l'université, la formation et le rôle
     * @param liste liste qui contient les différente étapes de la requête vers la base de données
     * @return la liste
     */
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

    /**
     * Permet de saisir du test
     * @return la saisie utilisateur
     */
    public static String recupSaisie(){
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
