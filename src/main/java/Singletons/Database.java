package Singletons;

import org.dizitart.no2.Nitrite;

public class Database {

    public static Nitrite db;


    public static Nitrite getDb() {
        return db;
    }

    public static void setDb(Nitrite db) {
        Database.db = db;
    }
}
