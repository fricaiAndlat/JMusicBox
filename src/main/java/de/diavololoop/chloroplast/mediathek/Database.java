package de.diavololoop.chloroplast.mediathek;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 *
 * DATABASE structure:
 *
 * Interpret:
 *      id: Integer
 *      name: String
 *
 * Album:
 *      id: Integer
 *      name: String
 *      interpret: int -> interpret.id
 *
 * Titel:
 *      id: Integer
 *      name: String
 *      album: int -> album.id
 *
 * search:
 *      id: int -> interpret.id | album.id | titel.id
 *      codes: String
 *      type: int
 *
 *
 */
public class Database {

    private final static String SEARCH_TITLE = "type=0";
    private final static String SEARCH_ALBUM = "type=1";
    private final static String SEARCH_INTERPRET = "type=2";
    private final static String SEARCH_TITLE_ALBUM = "type<2";
    private final static String SEARCH_TITLE_INTERPRET = "type<>1";
    private final static String SERACH_ALBUM_INTERPRET = "type>0";
    private final static String SEARCH_TITLE_ALBUM_INTERPRET = "1=1";

    private final static String[] SEARCH_STRINGS = {SEARCH_TITLE_ALBUM_INTERPRET, SEARCH_TITLE, SEARCH_ALBUM,
            SEARCH_TITLE_ALBUM, SEARCH_INTERPRET, SEARCH_TITLE_INTERPRET, SERACH_ALBUM_INTERPRET,
            SEARCH_TITLE_ALBUM_INTERPRET};

    private static Database database;


    public static void main(String[] args) {

    }

    public static Database get() {

        if (database == null) {
            database = new Database();
        }


        return database;
    }


    private Connection connection;
    private Statement statement;


    private Database () {

        File databaseDir = new File("database");
        if (!databaseDir.isDirectory()) {
            databaseDir.mkdirs();
        }

        File database = new File("database/database.db");

        try{
            connection = DriverManager.getConnection("jdbc:sqlite:" + database.getAbsolutePath());
            statement = connection.createStatement();

            if (!database.isFile()) {
                initDatabase();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }



    }

    public void searchFor(String search, boolean title, boolean album, boolean interpret) {

        int searchType = 0;
        searchType |= title     ? 1 : 0;
        searchType |= album     ? 2 : 0;
        searchType |= interpret ? 4 : 0;

        String searchString = SEARCH_STRINGS[searchType];

        System.out.println(searchString);
    }

    /**
     *
     * DATABASE structure:
     *
     * title=0, album=1, interpret=2
     *
     * Interpret:
     *      id: Integer
     *      name: String
     *
     * Album:
     *      id: Integer
     *      name: String
     *      interpret: int -> interpret.id
     *
     * Titel:
     *      id: Integer
     *      name: String
     *      album: int -> album.id
     *
     * search:
     *      id: int -> interpret.id | album.id | titel.id
     *      codes: String
     *      type: int
     *
     *
     */
    private void initDatabase() throws SQLException {

        statement.executeUpdate("CREATE TABLE interpret (" +
                "id   INTEGER      PRIMARY KEY, " +
                "name VARCHAR(255) UNIQUE);");

        statement.executeUpdate("CREATE TABLE album (" +
                "id         INTEGER      PRIMARY KEY, " +
                "name       VARCHAR(255) NOT NULL," +
                "interpret  INTEGER      NOT NULL," +
                "FOREIGN KEY(interpret) REFERENCES interpret(id));");

        statement.executeUpdate("CREATE TABLE title (" +
                "id         INTEGER      PRIMARY KEY, " +
                "name       VARCHAR(255) NOT NULL," +
                "album      INTEGER      NOT NULL," +
                "FOREIGN KEY(album) REFERENCES album(id));");

        statement.executeUpdate("CREATE TABLE search (" +
                "id         INTEGER      INDEX, " +
                "codes      VARCHAR(255) INDEX," +
                "type       INTEGER      NOT NULL);");

    }

    public Optional<Interpret> addInterpret(String interpret){

        try {

            int insertID = statement.executeUpdate("INSERT INTO interpret (name) VALUES ('" + interpret + "');");
            statement.executeUpdate("INSERT INTO search (codes, type, id) VALUES ('" + interpret + "', 2, "+insertID+")");

            List<String> keys = Phonetik.makeSearchKeys(interpret);

            for (String key: keys) {
                statement.executeUpdate("INSERT INTO search (codes, type, id) VALUES ('" + key + "', 2, "+insertID+")");
            }

            return Optional.of(new Interpret(interpret, insertID));

        } catch (SQLException e) {
            return Optional.empty();
        }

    }

    public Optional<Album> addAlbum(String album, Interpret interpret) {

        if (interpret.id == -1) {
            //TODO retrieve or add interpret if not known
        }

        try {

            int insertID = statement.executeUpdate("INSERT INTO album (name, interpret) VALUES ('" + album + "', " + interpret.id + ");");
            statement.executeUpdate("INSERT INTO search (codes, type, id) VALUES ('" + album + "', 1, "+insertID+")");

            List<String> keys = Phonetik.makeSearchKeys(album);

            for (String key: keys) {
                statement.executeUpdate("INSERT INTO search (codes, type, id) VALUES ('" + key + "', 1, "+insertID+")");
            }

            return Optional.of(new Album(album, interpret, insertID));

        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    public class Interpret {

        public final String name;
        public final int id;

        public Interpret(String name) {
            this.name = name;
            id = -1;
        }

        public Interpret(String name, int id) {
            this.name = name;
            this.id = id;
        }

    }

    public class Album {

        public final String name;
        public final int id;
        public final Interpret interpret;

        public Album(String name, Interpret interpret) {
            this.name = name;
            this.interpret = interpret;
            id = -1;
        }

        public Album(String name, Interpret interpret, int id) {
            this.name = name;
            this.interpret = interpret;
            this.id = id;
        }

    }

}
