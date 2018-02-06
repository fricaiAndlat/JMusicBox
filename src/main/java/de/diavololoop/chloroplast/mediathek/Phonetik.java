package de.diavololoop.chloroplast.mediathek;

import java.io.File;

import java.util.LinkedList;
import java.util.List;

public class Phonetik {

    public static void main(String[] args) {

        Database data = Database.get();

        data.searchFor("", false, false, false);
        data.searchFor("", false, false, true);
        data.searchFor("", false, true, false);
        data.searchFor("", false, true, true);
        data.searchFor("", true, false, false);
        data.searchFor("", true, false, true);
        data.searchFor("", true, true, false);
        data.searchFor("", true, true, true);

    }

    public static List<String> makeSearchKeys(String str) {

        List<String> result = new LinkedList<String>();

        List<String> numbers = Phonetik.extractNumbersInString(str);

        for (String key: numbers) {
            result.add(soundex(key));
            result.add(soundexGerman(key));
        }


        str = str.toLowerCase();
        str = str.replaceAll("ä", "a");
        str = str.replaceAll("ö", "o");
        str = str.replaceAll("ü", "u");
        str = str.replaceAll("ß", "s");
        str = str.replaceAll("[^a-z]", " ");
        str = str.replaceAll(" +", " ");
        str = str.trim();

        System.out.println("string: "+str);

        String[] elements = str.split(" ");
        for (String key: elements) {
            result.add(soundex(key));
            result.add(soundexGerman(key));
        }

        return result;

    }

    public static String soundex(String str) {
        str = str.toLowerCase();
        str = str.replaceAll("ä", "a");
        str = str.replaceAll("ö", "o");
        str = str.replaceAll("ü", "u");
        str = str.replaceAll("ß", "s");
        str = str.replaceAll("[^a-z]", "");

        StringBuilder builder = new StringBuilder();

        if (str.length() < 1) {
            return "0000";
        }

        builder.append(str.charAt(0));

        for (int i = 1; i < str.length() && builder.length() < 4; ++i) {

            char c = str.charAt(i);
            char last = i == 0 ? '#': builder.charAt(builder.length() - 1);

            switch(c) {
                case 'b':
                case 'f':
                case 'p':
                case 'v':
                    if (last != '1') {
                        builder.append('1');
                    }
                    break;
                case 'c':
                case 'g':
                case 'j':
                case 'k':
                case 'q':
                case 's':
                case 'x':
                case 'z':
                    if (last != '2') {
                        builder.append('2');
                    }
                    break;
                case 'd':
                case 't':
                    if (last != '3') {
                        builder.append('3');
                    }
                    break;
                case 'l':
                    if (last != '4') {
                        builder.append('4');
                    }
                    break;
                case 'm':
                case 'n':
                    if (last != '5') {
                        builder.append('5');
                    }
                    break;
                case 'r':
                    if (last != '6') {
                        builder.append('6');
                    }
                    break;
                default: {}
            }
        }

        for (int i = builder.length(); i < 4; ++i) {
            builder.append('0');
        }

        return builder.toString();
    }

    public static String soundexGerman(String str) {
        str = str.toLowerCase();
        str = str.replaceAll("ä", "a");
        str = str.replaceAll("ö", "o");
        str = str.replaceAll("ü", "u");
        str = str.replaceAll("[^a-z]", "");


        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < str.length(); ++i) {

            char c = str.charAt(i);
            char next = i+1 == str.length() ? '#' : str.charAt(i + 1);
            char previous = i == 0 ? '#' : str.charAt(i - 1);
            char last = builder.length() == 0 ? '#' : builder.charAt(builder.length() - 1);

            switch(c) {
                case 'a':
                case 'e':
                case 'i':
                case 'j':
                case 'o':
                case 'u':
                case 'y':
                    if (i == 0) {
                        builder.append('0');
                    }
                    break;
                case 'h':
                    break;
                case 'b':
                    if (last != '1') {
                        builder.append('1');
                    }
                    break;
                case 'p':
                    if (next != 'h') {
                        if (last != '1'){
                            builder.append('1');
                        }
                    } else {
                        if (last != '3') {
                            builder.append('3');
                        }
                    }
                    break;
                case 'd':
                case 't':
                    if (next != 'c' && next != 's' && next != 'z') {
                        if (last != '2') {
                            builder.append('2');
                        }
                    } else {
                        if (last != '8') {
                            builder.append('8');
                        }                    }
                    break;
                case 'f':
                case 'v':
                case 'w':
                    if (last != '3') {
                        builder.append('3');
                    }
                    break;
                case 'g':
                case 'k':
                case 'q':
                    if (last != '4') {
                        builder.append('4');
                    }
                    break;
                case 'c':
                    if ((i == 0 &&
                            (next == 'a' || next == 'h' || next == 'k' || next == 'l' || next == 'O'
                                    || next == 'Q' || next == 'r' || next == 'u' || next == 'x'))
                        ||
                            (previous != 's' && previous != 'z' && (next == 'a' || next == 'h' || next == 'k'
                                    || next == 'O' || next == 'Q' || next == 'u' || next == 'x'))) {

                        if (last != '4') {
                            builder.append('4');
                        }

                    } else {
                        if (last != '8') {
                            builder.append('8');
                        }
                    }
                    break;
                case 'x':
                    if (previous != 'c' && previous != 'k' && previous != 'q') {
                        if (last != '4') {
                            builder.append('4');
                        }
                    }
                    if (last != '8') {
                        builder.append('8');
                    }
                    break;
                case 'l':
                    if (last != '5') {
                        builder.append('5');
                    }
                    break;
                case 'm':
                case 'n':
                    if (last != '6') {
                        builder.append('6');
                    }
                    break;
                case 'r':
                    if (last != '7') {
                        builder.append('7');
                    }
                    break;
                case 's':
                case 'z':
                case 'ß':
                    if (last != '8') {
                        builder.append('8');
                    }
                    break;
                default: {}
            }


        }

        return builder.toString();

    }

    public static List<String> extractNumbersInString(String str) {

        LinkedList<String> numbers = new LinkedList<String>();

        int currentNumber = 0;
        boolean numberFound = false;


        for (int i = 0; i < str.length(); ++i){

            if (isNumber(str.charAt(i))) {

                numberFound = true;
                currentNumber *= 10;

                switch (str.charAt(i)) {

                    case '1': currentNumber += 1; break;
                    case '2': currentNumber += 2; break;
                    case '3': currentNumber += 3; break;
                    case '4': currentNumber += 4; break;
                    case '5': currentNumber += 5; break;
                    case '6': currentNumber += 6; break;
                    case '7': currentNumber += 7; break;
                    case '8': currentNumber += 8; break;
                    case '9': currentNumber += 9; break;
                    default: {}
                }

            } else {

                if (numberFound) {

                    numbers.add(encodeNumber(currentNumber));
                    numbers.add(encodeNumberGerman(currentNumber));

                    currentNumber = 0;
                    numberFound = false;

                }
            }

        }

        if (numberFound) {

            numbers.add(encodeNumber(currentNumber));
            numbers.add(encodeNumberGerman(currentNumber));

        }

        return numbers;

    }

    private final static String[] E_NUMBER = {null, "one", "two", "three", "four", "five", "six", "seven", "eight",
                "nine", "ten", "eleven", "twelve"};
    private final static String[] E_NUMBER_TEN = {"", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy",
                "eighty", "ninety"};

    private static String encodeNumber(int number) {

        if (number < 0) {
            throw new IllegalArgumentException("can only encode numbers >= 0");
        } else if (number == 0) {
            return "zero";
        } else if (number > 999999999) {
            throw new IllegalArgumentException("can only encode numbers < 1000000000");
        }

        String result = "";

        if (number >= 1000000) {
            int milliarden = number / 1000000;

            if (milliarden != 0) {
                result += encodeNumberHundret(milliarden) + "million";
            }
        }

        if (number >= 1000) {
            int thousand = (number / 1000) % 1000;

            if (thousand != 0) {
                result += encodeNumberHundret(thousand) + "thousand";
            }
        }

        result += encodeNumberHundret(number % 1000);

        return result;
    }

    private static String encodeNumberHundret(int number) {

        String result = "";
        int hundred = number / 100;

        if (hundred != 0) {
            result += E_NUMBER[hundred] + "hundred";
        }

        result += encodeNumberTen(number % 100);

        return result;
    }

    private static String encodeNumberTen(int number) {
        String result = "";

        if (number == 0) {
            return "";
        }

        if (number <= 12) {
            return E_NUMBER[number];
        }

        if (number < 20 && number > 12) {
            return E_NUMBER[number%10] + "teen";
        }

        result += E_NUMBER_TEN[number/10];
        result += E_NUMBER[number%10];
        return result;
    }


    private final static String G_ONE = "ein";
    private final static String G_TWO = "zwei";
    private final static String G_THREE = "drei";
    private final static String G_FOUR = "vier";
    private final static String G_FIVE = "fünf";
    private final static String G_SIX = "sechs";
    private final static String G_SEVEN = "sieben";
    private final static String G_EIGHT = "acht";
    private final static String G_NINE = "neun";
    private final static String G_TEN = "zehn";
    private final static String G_ELEVEN = "elf";
    private final static String G_TWELVE = "zwölf";
    private final static String[] G_NUMBER = {"", G_ONE, G_TWO, G_THREE, G_FOUR, G_FIVE, G_SIX, G_SEVEN, G_EIGHT,
            G_NINE, G_TEN, G_ELEVEN, G_TWELVE};

    private final static String G_TWENTY = "zwanzig";
    private final static String G_THIRTY = "dreißig";
    private final static String G_FOURTY = "vierzig";
    private final static String G_FIFTY = "fünfzig";
    private final static String G_SIXTY = "sechzig";
    private final static String G_SEVENTY = "siebzig";
    private final static String G_EIGHTY = "achtzig";
    private final static String G_NINETY = "neunzig";
    private final static String[] G_NUMBER_TEN = {null, G_TEN, G_TWENTY, G_THIRTY, G_FOURTY, G_FIFTY, G_SIXTY,
            G_SEVENTY, G_EIGHTY, G_NINETY};

    private static String encodeNumberGerman(int number) {

        if (number < 0) {
            throw new IllegalArgumentException("can only encode numbers >= 0");
        } else if (number == 0) {
            return "null";
        } else if (number == 1) {
            return "eins";
        } else if (number > 999999999) {
            throw new IllegalArgumentException("can only encode numbers < 1000000000");
        }

        String result = "";

        if (number >= 1000000) {
            int milliarden = number / 1000000;

            if (milliarden != 0) {
                result += encodeNumberGermanHundret(milliarden) + "millionen";
            }
        }

        if (number >= 1000) {
            int thousand = (number / 1000) % 1000;

            if (thousand != 0) {
                result += encodeNumberGermanHundret(thousand) + "tausend";
            }
        }

        result += encodeNumberGermanHundret(number % 1000);

        return result;
    }

    private static String encodeNumberGermanHundret(int number) {

        String result = "";
        int hundret = number / 100;

        if (hundret != 0) {
            result += G_NUMBER[hundret] + "hundert";
        }

        result += encodeNumberGemanTen(number % 100);

        return result;
    }

    private static String encodeNumberGemanTen(int number) {

        String result = "";

        if (number == 0) {
            return "";
        } else if (number == 16) {
            return "sechzehn";
        } else if (number == 17) {
            return "siebzehn";
        }

        if (number <= 12) {
            return G_NUMBER[number];
        }

        result += G_NUMBER[number%10];

        if (number%10 != 0 && number > 20) {
            result += "und";
        }

        result += G_NUMBER_TEN[number/10];
        return result;

    }

    private static boolean isNumber(char c) {
        return (c >= '0' && c <= '9');
    }

}
