package de.diavololoop.chloroplast.mediathek;

import java.io.File;
import java.io.PrintStream;
import java.sql.*;
import java.util.Arrays;
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
        Database data = new Database();
        try {
            data.initDatabase();
            data.addInterpret("chloroplast");
            data.printDatabase(System.out);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Database get() {

        if (database == null) {
            database = new Database();
        }


        return database;
    }


    private Connection connection;
    private Statement statement;

    private PreparedStatement stmtInterpretAdd;
    private PreparedStatement stmtAlbumAdd;
    private PreparedStatement stmtTitleAdd;
    private PreparedStatement stmtSearchAdd;

    private PreparedStatement stmtSearchInterpretFast;
    private PreparedStatement stmtSearchAlbumFast;
    private PreparedStatement stmtSearchTitleFast;

    private Database () {

        File databaseDir = new File("database");
        if (!databaseDir.isDirectory()) {
            databaseDir.mkdirs();
        }

        File database = new File("database/database.db");

        database.delete();

        try{

            boolean isEmpty = !database.isFile();

            connection = DriverManager.getConnection("jdbc:sqlite:" + database.getAbsolutePath());
            statement = connection.createStatement();

            if (isEmpty) {
                initDatabase();
            }

            stmtInterpretAdd = connection.prepareStatement("INSERT INTO interpret (name)            VALUES (?);",    new String[] {"id"});
            stmtAlbumAdd     = connection.prepareStatement("INSERT INTO album     (name, interpret) VALUES (?, ?);", new String[] {"id"});
            stmtTitleAdd     = connection.prepareStatement("INSERT INTO title     (name, album)     VALUES (?, ?);", new String[] {"id"});
            stmtSearchAdd    = connection.prepareStatement("INSERT INTO search    (codes, type, id) VALUES (?, ?, ?);");

            stmtSearchInterpretFast = connection.prepareStatement("SELECT i.id, i.name FROM search s INNER JOIN interpret i ON i.id = s.id WHERE type = 2 AND codes LIKE ? GROUP BY i.id, i.name");
            stmtSearchAlbumFast     = connection.prepareStatement("SELECT a.id, a.name, i.id, i.name " +
                    "FROM search s " +
                    "INNER JOIN album a     ON s.id = a.id " +
                    "INNER JOIN interpret i ON i.id = a.interpret " +
                    "WHERE type = 1 AND codes LIKE ? " +
                    "GROUP BY a.id, a.name, i.id, i.name");

            stmtSearchTitleFast = connection.prepareStatement("SELECT t.name, t.id, a.name, a.id, i.name, i.id " +
                    "FROM search s " +
                    "INNER JOIN title t     ON s.id = t.id " +
                    "INNER JOIN album a     ON a.id = t.album " +
                    "INNER JOIN interpret i ON i.id = a.interpret " +
                    "WHERE type = 0 AND codes LIKE ? " +
                    "GROUP BY t.name, t.id, a.name, a.id, i.name, i.id");

            connection.setAutoCommit(false);

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        printDatabase(System.out);
    }

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
    public void searchFast(String search, boolean title, boolean album, boolean interpret, List<Interpret> resInterpret, List<Album> resAlbum, List<Title> resTitle) {

        try {
            if (interpret) {
                stmtSearchInterpretFast.setString(1, "%" + search + "%");
                ResultSet result = stmtSearchInterpretFast.executeQuery();

                while (result.next()) {
                    resInterpret.add(new Interpret(result.getString(2), result.getInt(1)));
                }
            }

            if (album) {
                stmtSearchAlbumFast.setString(1, "%" + search + "%");
                ResultSet result = stmtSearchAlbumFast.executeQuery();

                while (result.next()) {
                    resAlbum.add(new Album(result.getString(2), new Interpret(result.getString(4), result.getInt(3)), result.getInt(1)));
                }
            }

            if (title) {
                stmtSearchTitleFast.setString(1, "%" + search + "%");

                ResultSet result = stmtSearchTitleFast.executeQuery();

                while (result.next()) {
                    Interpret i = new Interpret(result.getString(5), result.getInt(6));
                    Album a = new Album(result.getString(3), i, result.getInt(4));
                    Title t = new Title(result.getString(1), a, result.getInt(2));
                    resTitle.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void search(String search, boolean title, boolean album, boolean interpret, List<Interpret> resInterpret, List<Album> resAlbum, List<Title> resTitle) {

        try {

            if (interpret) {

                StringBuilder query = new StringBuilder();

                query.append(
                        "SELECT i.id, i.name, k.keyword, s.codes " +
                        "FROM search s " +
                        "INNER JOIN ( ");

                List<String> keys = Phonetik.makeSearchKeys(search);
                keys.forEach(k -> query.append(String.format("SELECT '%%%s%%' AS keyword UNION ALL ", k)));
                Arrays.stream(search.split(" ")).forEach(k -> query.append(String.format("SELECT '%%%s%%' AS keyword UNION ALL ", k)));

                query.append(String.format("SELECT '%%%s%%' AS keyword", search.toLowerCase().replaceAll("'", "''")));

                query.append(
                        ") k ON s.codes LIKE k.keyword " +
                        "INNER JOIN interpret i ON i.id = s.id " +
                        "WHERE type = 2 " +
                        "");//GROUP BY i.id, i.name

                System.out.println(query.toString());

                ResultSet result = statement.executeQuery(query.toString());

                while (result.next()) {
                    resInterpret.add(new Interpret(result.getString(2), result.getInt(1)));


                    System.out.printf("%20s|%20s|%20s\r\n", result.getString(2), result.getString(3), result.getString(4));
                }
            }

            if (album) {
                stmtSearchAlbumFast.setString(1, "%" + search + "%");
                ResultSet result = stmtSearchAlbumFast.executeQuery();

                while (result.next()) {
                    resAlbum.add(new Album(result.getString(2), new Interpret(result.getString(4), result.getInt(3)), result.getInt(1)));
                }
            }

            if (title) {
                stmtSearchTitleFast.setString(1, "%" + search + "%");

                ResultSet result = stmtSearchTitleFast.executeQuery();

                while (result.next()) {
                    Interpret i = new Interpret(result.getString(5), result.getInt(6));
                    Album a = new Album(result.getString(3), i, result.getInt(4));
                    Title t = new Title(result.getString(1), a, result.getInt(2));
                    resTitle.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

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
                "id         INTEGER      NOT NULL," +
                "codes      VARCHAR(255) NOT NULL," +
                "type       INTEGER      NOT NULL);" +
                "CREATE INDEX ind_search_codes ON search(codes);");
    }

    public Optional<Interpret> addInterpret(String interpret){

        try {

            stmtInterpretAdd.setString(1, interpret);
            stmtInterpretAdd.executeUpdate();

            ResultSet res = stmtAlbumAdd.getGeneratedKeys();
            res.next();
            int insertID = res.getInt(1);

            stmtSearchAdd.setString(1, interpret.toLowerCase());
            stmtSearchAdd.setInt(2, 2);
            stmtSearchAdd.setInt(3, insertID);
            stmtSearchAdd.executeUpdate();

            List<String> keys = Phonetik.makeSearchKeys(interpret);

            for (String key: keys) {
                stmtSearchAdd.setString(1, key);
                stmtSearchAdd.setInt(2, 2);
                stmtSearchAdd.setInt(3, insertID);
                stmtSearchAdd.executeUpdate();
            }

            connection.commit();

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

            stmtAlbumAdd.setString(1, album);
            stmtAlbumAdd.setInt(2, interpret.id);
            stmtAlbumAdd.executeUpdate();

            ResultSet res = stmtAlbumAdd.getGeneratedKeys();
            res.next();
            int insertID = res.getInt(1);

            stmtSearchAdd.setString(1, album.toLowerCase());
            stmtSearchAdd.setInt(2, 1);
            stmtSearchAdd.setInt(3, insertID);
            stmtSearchAdd.executeUpdate();

            List<String> keys = Phonetik.makeSearchKeys(album);

            for (String key: keys) {
                stmtSearchAdd.setString(1, key);
                stmtSearchAdd.setInt(2, 1);
                stmtSearchAdd.setInt(3, insertID);
                stmtSearchAdd.executeUpdate();
            }

            connection.commit();

            return Optional.of(new Album(album, interpret, insertID));

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<Title> addTitle(String title, Album album) {

        if (album.id == -1) {
            //TODO retrieve or add interpret if not known
        }

        try {

            stmtTitleAdd.setString(1, title);
            stmtTitleAdd.setInt(2, album.id);
            stmtTitleAdd.executeUpdate();

            ResultSet res = stmtAlbumAdd.getGeneratedKeys();
            res.next();
            int insertID = res.getInt(1);

            stmtSearchAdd.setString(1, title.toLowerCase());
            stmtSearchAdd.setInt(2, 0);
            stmtSearchAdd.setInt(3, insertID);
            stmtSearchAdd.executeUpdate();

            List<String> keys = Phonetik.makeSearchKeys(title);

            for (String key: keys) {
                stmtSearchAdd.setString(1, key);
                stmtSearchAdd.setInt(2, 0);
                stmtSearchAdd.setInt(3, insertID);
                stmtSearchAdd.executeUpdate();
            }

            connection.commit();

            return Optional.of(new Title(title, album, insertID));

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void printDatabase(PrintStream out) {

        try {

            System.out.println("######################## DATABASE START ########################");

            ResultSet result = statement.executeQuery("SELECT id, name FROM interpret");
            out.println("interpret:");
            out.println("----id----|---name---");
            while (result.next()) {
                out.printf("%10d|%10s\r\n", result.getInt(1), result.getString(2));
            }
            out.println();


            result = statement.executeQuery("SELECT a.id, a.name, a.interpret, i.name FROM album a INNER JOIN interpret i ON a.interpret = i.id ");
            out.println("album:");
            out.println("----id----|-----name-----|---iid----|interpret");
            while (result.next()) {
                out.printf("%10d|%14s|%10d|%10s\r\n", result.getInt(1), result.getString(2), result.getInt(3), result.getString(4));
            }
            out.println();


            result = statement.executeQuery("SELECT t.id, t.name, t.album, a.name, i.name FROM title t INNER JOIN album a ON t.album = a.id INNER JOIN interpret i ON a.interpret = i.id");
            out.println("album:");
            out.println("----id----|-----name-----|---aid----|--interpret--|--album-");
            while (result.next()) {
                out.printf("%10d|%14s|%10d|%14s|%10s\r\n", result.getInt(1), result.getString(2), result.getInt(3), result.getString(4), result.getString(5));
            }
            out.println();


            result = statement.executeQuery("SELECT id, codes, type FROM search ORDER BY type");
            out.println("album:");
            out.println("----id----|----codes---|---type---");
            while (result.next()) {
                out.printf("%10d|%12s|%10d\r\n", result.getInt(1), result.getString(2), result.getInt(3));
            }
            out.println();

            System.out.println("######################## DATABASE END ########################");

        } catch (SQLException e) {
            e.printStackTrace(out);
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

    public class Title {

        public final String name;
        public final int id;
        public final Album album;

        public Title(String name, Album album) {
            this.name = name;
            this.album = album;
            id = -1;
        }

        public Title(String name, Album album, int id) {
            this.name = name;
            this.album = album;
            this.id = id;
        }

    }

}
