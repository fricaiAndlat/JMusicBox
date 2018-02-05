package de.diavololoop.chloroplast.mediathek;

public class Database {

    private final static String SEARCH_TITEL = "";
    private final static String SEARCH_ALBUM = "";
    private final static String SEARCH_INTERPRET = "";
    private final static String SEARCH_TITEL_ALBUM = "";
    private final static String SEARCH_TITEL_INTERPRET = "";
    private final static String SERACH_ALBUM_INTERPRET = "";
    private final static String SEARCH_TITEL_ALBUM_INTERPRET = "";

    private final static String[] SEARCH_STRINGS = {SEARCH_TITEL_ALBUM_INTERPRET, SEARCH_TITEL, SEARCH_ALBUM,
            SEARCH_TITEL_ALBUM, SEARCH_INTERPRET, SEARCH_TITEL_INTERPRET, SERACH_ALBUM_INTERPRET,
            SEARCH_TITEL_ALBUM_INTERPRET};

    private static Database database;


    public static void main(String[] args) {

    }

    public Database get() {

        if (database == null) {
            database = new Database();
        }

        return database;
    }

    private Database () {

    }

    public void searchFor(String search, boolean titel, boolean album, boolean interpret) {

        int searchType = 0;
        searchType |= titel     ? 1 : 0;
        searchType |= album     ? 2 : 0;
        searchType |= interpret ? 4 : 0;

        String searchString = SEARCH_STRINGS[searchType];
    }

}
