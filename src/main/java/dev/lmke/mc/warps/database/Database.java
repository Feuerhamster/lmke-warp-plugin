package dev.lmke.mc.warps.database;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.ObjectRepository;

public class Database {

    private static Nitrite database;

    public static void openDatabase(String path) {
        database = Nitrite.builder()
                .filePath(path)
                .openOrCreate();
    }

    public static void closeDatabase() {
        database.close();
    }

    public static void commit() {
        database.commit();
    }

    public static <T> ObjectRepository<T> getRepo(Class<T> c) {
        return database.getRepository(c);
    }
}
