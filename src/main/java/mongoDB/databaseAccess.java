package mongoDB;


import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.filters.Filters;

// The code used to to access and retrieve information from the database

public class databaseAccess {

    public static Nitrite getDatabase(String databaseName) {
        Nitrite db = Nitrite.builder().filePath(databaseName).openOrCreate();
        return (db);
    }

    public static NitriteCollection getCollection(String collectionName, Nitrite database) {
//        DBCollection collection = database.getCollection(collectionName);

        NitriteCollection collection = database.getCollection(collectionName);
        return(collection);
    }

    public static Document getFirstResult(NitriteCollection collection, String[] query) {
        Document results = collection.find(Filters.eq(query[0], query[1])).firstOrDefault();
        return(results);
    }

    public static Cursor getResults(NitriteCollection collection, String[] query) {
        Cursor results = collection.find(Filters.eq(query[0], query[1]));

        return(results);
    }

    public static Cursor getAll(NitriteCollection collection) {
        Cursor results = collection.find();
        return(results);
    }



}
