import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {

    private static String input = ""; // In the beginning rules holded here
    private static int lineCount = 0; // Context free language size variable
    private static String foundEpsilon = ""; // Variable for non-terminal contains epsilon value
    private static LinkedHashMap<String, List<String>> CNF_map = new LinkedHashMap<>(); // CNF rules stored
    private static HashMap<String, String> CFG_map = new HashMap<>(); // CFG rules stored

    public static void main(String[] args) throws FileNotFoundException {
        load("G2.txt"); // Loading the language file
        CFGtoCNF(); // Conversion function
        printCNF(); // Print function
    }

    public static void load(String f) throws FileNotFoundException {
        File file = new File(f);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String current = scanner.nextLine();
            if(current.equals("RULES")){
                current = scanner.nextLine();
                while(!current.equals("START")){
                    String lettr = Character.toString(current.charAt(0));
                    String newValue;
                    if (CFG_map.containsKey(lettr)) {
                        newValue = CFG_map.get(lettr) + "|" + current.substring(2);
                    } else {
                        newValue = "->" + current.substring(2);
                    }
                    CFG_map.put(lettr, newValue);
                    current = scanner.nextLine();
                }
                scanner.nextLine();
                for (String name : CFG_map.keySet()) {
                    input += name + CFG_map.get(name) + "\n";
                }
                input = input.substring(0, input.length() - 1);
                String[] splitted = input.split("\n");
                lineCount = splitted.length;
            }
        }
        scanner.close();
    }

    private static String[] splitter(String input) {

        String[] tmpArray = new String[lineCount];
        for (int i = 0; i < lineCount; i++) {
            tmpArray = input.split("\\n");
        }
        return tmpArray;
    }

    public static void CFGtoCNF() {
        // New start variable assigned
        String newStart = "S0";
        ArrayList<String> newTransition = new ArrayList<>();
        newTransition.add("S");
        CNF_map.put(newStart, newTransition);

        // Converter for result string to map
        String[] splitedEnterInput = splitter(input);

        for (String s : splitedEnterInput) {

            String[] tempString = s.split("->|\\|");
            String non_terminal = tempString[0].trim();

            String[] Transition = Arrays.copyOfRange(tempString, 1, tempString.length);
            List<String> TransitionList = new ArrayList<>();

            // trim the empty space
            for (int k = 0; k < Transition.length; k++) {
                Transition[k] = Transition[k].trim();
            }

            // import array into ArrayList
            for (String value : Transition) {
                TransitionList.add(value);
            }

            // insert element into map
            CNF_map.put(non_terminal, TransitionList);
        }

        // Removing epsilon transitions
        for (int i = 0; i < lineCount; i++) {
            removeEpsilon();
        }

        // Removing duplicate non-terminal's transitions

        for (Map.Entry<String, List<String>> stringListEntry : CNF_map.entrySet()) {
            Map.Entry entry = (Map.Entry) stringListEntry;
            ArrayList<String> TransitionRow = (ArrayList<String>) entry.getValue();

            for (int i = 0; i < TransitionRow.size(); i++) {
                if (TransitionRow.get(i).contains(entry.getKey().toString())) {
                    TransitionRow.remove(entry.getKey().toString());
                }
            }
        }

        // Removing single non-terminals
        for (int i = 0; i < lineCount; i++) {
            removeSingleVariable();
        }

        // Assigning new variable for two non-terminal and one terminal
        onlyTwoNonTerminalOrOneTerminal();

        // Removing three or more terminals
        for (int i = 0; i < lineCount; i++) {
            removeThreeOrMoreTerminals();
        }

    }

    private static void removeEpsilon() {

        Iterator itr = CNF_map.entrySet().iterator();
        Iterator itr2 = CNF_map.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            ArrayList<String> TransitionRow = (ArrayList<String>) entry.getValue();

            if (TransitionRow.contains("e")) {
                if (TransitionRow.size() > 1) {
                    TransitionRow.remove("e");
                    foundEpsilon = entry.getKey().toString();

                } else {

                    foundEpsilon = entry.getKey().toString();
                    CNF_map.remove(foundEpsilon);
                }
            }
        }

        while (itr2.hasNext()) {

            Map.Entry entry = (Map.Entry) itr2.next();
            ArrayList<String> TransitionList = (ArrayList<String>) entry.getValue();

            for (int i = 0; i < TransitionList.size(); i++) {
                String temp = TransitionList.get(i);
                for (int j = 0; j < temp.length(); j++) {
                    if (foundEpsilon.equals(Character.toString(temp.charAt(j)))) {

                        if (temp.length() == 2) {

                            temp = temp.replace(foundEpsilon, "");

                            if (!CNF_map.get(entry.getKey().toString()).contains(temp)) {
                                CNF_map.get(entry.getKey().toString()).add(temp);
                            }

                        } else if (temp.length() >= 3) {

                            String deletedTemp = new StringBuilder(temp).deleteCharAt(j).toString();

                            if (!CNF_map.get(entry.getKey().toString()).contains(deletedTemp)) {
                                CNF_map.get(entry.getKey().toString()).add(deletedTemp);
                            }
                        } else {

                            if (!CNF_map.get(entry.getKey().toString()).contains("e")) {
                                CNF_map.get(entry.getKey().toString()).add("e");
                            }
                        }
                    }
                }
            }
        }
    }

    private static void removeSingleVariable() {

        Iterator itr4 = CNF_map.entrySet().iterator();
        String key = null;

        while (itr4.hasNext()) {

            Map.Entry entry = (Map.Entry) itr4.next();
            Set set = CNF_map.keySet();
            ArrayList<String> keySet = new ArrayList<String>(set);
            ArrayList<String> TransitionList = (ArrayList<String>) entry.getValue();

            for (int i = 0; i < TransitionList.size(); i++) {
                String temp = TransitionList.get(i);

                for (int j = 0; j < temp.length(); j++) {

                    for (String s : keySet) {
                        if (s.equals(temp)) {

                            key = entry.getKey().toString();
                            List<String> TransitionValue = CNF_map.get(temp);
                            TransitionList.remove(temp);
                            for (String value : TransitionValue) {
                                if (!CNF_map.get(key).contains(value))
                                    CNF_map.get(key).add(value);
                            }
                        }
                    }
                }
            }
        }
    }

    private static Boolean checkDuplicateInTransitionList(Map<String, List<String>> map, String key) {

        Boolean notFound = true;

        Iterator itr = map.entrySet().iterator();
        outerloop:

        while (itr.hasNext()) {

            Map.Entry entry = (Map.Entry) itr.next();
            ArrayList<String> TransitionList = (ArrayList<String>) entry.getValue();

            for (int i = 0; i < TransitionList.size(); i++) {
                if (TransitionList.size() < 2) {

                    if (TransitionList.get(i).equals(key)) {
                        notFound = false;
                        break outerloop;
                    } else {
                        notFound = true;
                    }
                }
            }
        }

        return notFound;
    }

    private static void onlyTwoNonTerminalOrOneTerminal() {

        Iterator itr5 = CNF_map.entrySet().iterator();
        String key = null;
        int lettr = 71; // G
        Map<String, List<String>> tempList = new LinkedHashMap<>();

        while (itr5.hasNext()) {

            Map.Entry entry = (Map.Entry) itr5.next();
            Set set = CNF_map.keySet();

            ArrayList<String> keySet = new ArrayList<String>(set);
            ArrayList<String> TransitionList = (ArrayList<String>) entry.getValue();
            Boolean found1 = false;
            Boolean found2 = false;
            Boolean found = false;

            for (String temp : TransitionList) {
                for (int j = 0; j < temp.length(); j++) {

                    if (temp.length() == 3) {

                        String newTransition = temp.substring(1, 3);

                        found = checkDuplicateInTransitionList(tempList, newTransition)
                                && checkDuplicateInTransitionList(CNF_map, newTransition);

                        if (found) {

                            ArrayList<String> newVariable = new ArrayList<>();
                            newVariable.add(newTransition);
                            key = Character.toString((char) lettr);

                            tempList.put(key, newVariable);
                            lettr++;
                        }

                    } else if (temp.length() == 2) {

                        for (String s : keySet) {
                            if (!s.equals(Character.toString(temp.charAt(j)))) {
                                found = false;

                            } else {
                                found = true;
                                break;
                            }

                        }

                        if (!found) {
                            lettr = setLettr(lettr, tempList, temp, j);
                        }
                    } else if (temp.length() == 4) {

                        String newTransition1 = temp.substring(0, 2);
                        String newTransition2 = temp.substring(2, 4);

                        found1 = checkDuplicateInTransitionList(tempList, newTransition1)
                                && checkDuplicateInTransitionList(CNF_map, newTransition1);

                        found2 = checkDuplicateInTransitionList(tempList, newTransition2)
                                && checkDuplicateInTransitionList(CNF_map, newTransition2);

                        if (found1) {

                            ArrayList<String> newVariable = new ArrayList<>();
                            newVariable.add(newTransition1);
                            key = Character.toString((char) lettr);

                            tempList.put(key, newVariable);
                            lettr++;
                        }

                        if (found2) {
                            ArrayList<String> newVariable = new ArrayList<>();
                            newVariable.add(newTransition2);
                            key = Character.toString((char) lettr);

                            tempList.put(key, newVariable);
                            lettr++;
                        }
                    } else if (temp.length() == 1 && temp.toUpperCase().equals(temp)) {
                        lettr = setLettr(lettr, tempList, temp, j);
                    }
                }
            }
        }
        CNF_map.putAll(tempList);
    }

    private static int setLettr(int lettr, Map<String, List<String>> tempList, String temp, int j) {
        String key;
        String newTransition = Character.toString(temp.charAt(j));
        if (checkDuplicateInTransitionList(tempList, newTransition)
                && checkDuplicateInTransitionList(CNF_map, newTransition)) {

            ArrayList<String> newVariable = new ArrayList<>();
            newVariable.add(newTransition);
            key = Character.toString((char) lettr);

            tempList.put(key, newVariable);

            lettr++;

        }
        return lettr;
    }

    private static void removeThreeOrMoreTerminals() {

        Iterator itr = CNF_map.entrySet().iterator();
        ArrayList<String> keyList = new ArrayList<>();
        Iterator itr2 = CNF_map.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            ArrayList<String> TransitionRow = (ArrayList<String>) entry.getValue();

            if (TransitionRow.size() < 2) {
                keyList.add(entry.getKey().toString());
            }
        }

        while (itr2.hasNext()) {

            Map.Entry entry = (Map.Entry) itr2.next();
            ArrayList<String> TransitionList = (ArrayList<String>) entry.getValue();

            if (TransitionList.size() > 1) {
                for (int i = 0; i < TransitionList.size(); i++) {
                    String temp = TransitionList.get(i);

                    for (int j = 0; j < temp.length(); j++) {

                        if (temp.length() > 2) {
                            String stringToBeReplaced1 = temp.substring(j, temp.length());
                            String stringToBeReplaced2 = temp.substring(0, temp.length() - j);

                            for (String key : keyList) {

                                List<String> keyValues = CNF_map.get(key);
                                String[] values = keyValues.toArray(new String[keyValues.size()]);
                                String value = values[0];

                                if (stringToBeReplaced1.equals(value)) {

                                    CNF_map.get(entry.getKey().toString()).remove(temp);
                                    temp = temp.replace(stringToBeReplaced1, key);

                                    if (!CNF_map.get(entry.getKey().toString()).contains(temp)) {
                                        CNF_map.get(entry.getKey().toString()).add(i, temp);
                                    }
                                } else if (stringToBeReplaced2.equals(value)) {

                                    CNF_map.get(entry.getKey().toString()).remove(temp);
                                    temp = temp.replace(stringToBeReplaced2, key);

                                    if (!CNF_map.get(entry.getKey().toString()).contains(temp)) {
                                        CNF_map.get(entry.getKey().toString()).add(i, temp);
                                    }
                                }
                            }
                        } else if (temp.length() == 2) {

                            temp = setTemp(keyList, entry, i, temp);

                        } else if (temp.length() == 1 && temp.equals(temp.toUpperCase())) {

                            for (String key : keyList) {

                                List<String> keyValues = CNF_map.get(key);
                                String[] values = keyValues.toArray(new String[keyValues.size()]);
                                String value = values[0];

                                if (value.equals(temp)) {

                                    CNF_map.get(entry.getKey().toString()).remove(temp);
                                    temp = temp.replace(temp, key);

                                    if (!CNF_map.get(entry.getKey().toString()).contains(temp)) {
                                        CNF_map.get(entry.getKey().toString()).add(i, temp);
                                    }
                                }
                            }
                        }

                    }
                }
            } else if (TransitionList.size() == 1) {

                for (int i = 0; i < TransitionList.size(); i++) {
                    String temp = TransitionList.get(i);

                    if (temp.length() == 2) {

                        temp = setTemp(keyList, entry, i, temp);

                    }
                    else if (temp.length() > 2) {
                        for (int j = 0; j < temp.length(); j++) {

                            if (temp.length() > 2) {
                                String stringToBeReplaced1 = temp.substring(j, temp.length());
                                String stringToBeReplaced2 = temp.substring(0, temp.length() - j);

                                for (String key : keyList) {

                                    List<String> keyValues = CNF_map.get(key);
                                    String[] values = keyValues.toArray(new String[keyValues.size()]);
                                    String value = values[0];

                                    if (stringToBeReplaced1.equals(value) && !temp.equals(value)) {

                                        CNF_map.get(entry.getKey().toString()).remove(temp);
                                        temp = temp.replace(stringToBeReplaced1, key);

                                        if (!CNF_map.get(entry.getKey().toString()).contains(temp)) {
                                            CNF_map.get(entry.getKey().toString()).add(i, temp);
                                        }
                                    } else if (stringToBeReplaced2.equals(value) && !temp.equals(value)) {

                                        CNF_map.get(entry.getKey().toString()).remove(temp);
                                        temp = temp.replace(stringToBeReplaced2, key);

                                        if (!CNF_map.get(entry.getKey().toString()).contains(temp)) {
                                            CNF_map.get(entry.getKey().toString()).add(i, temp);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static String setTemp(ArrayList<String> keyList, Map.Entry entry, int i, String temp) {
        for (String key : keyList) {
            List<String> keyValues = CNF_map.get(key);
            String[] values = keyValues.toArray(new String[keyValues.size()]);
            String value = values[0];

            for (int pos = 0; pos < temp.length(); pos++) {
                String tempChar = Character.toString(temp.charAt(pos));

                if (value.equals(tempChar)) {

                    CNF_map.get(entry.getKey().toString()).remove(temp);
                    temp = temp.replace(tempChar, key);

                    if (!CNF_map.get(entry.getKey().toString()).contains(temp)) {
                        CNF_map.get(entry.getKey().toString()).add(i, temp);
                    }
                }
            }
        }
        return temp;
    }

    public static void printCNF() {

        System.out.println("NON-TERMINAL");
        for (String non_terminal : CNF_map.keySet()) {
            System.out.println(non_terminal);
        }
        System.out.println("TERMINAL");
        ArrayList<String> terminals = new ArrayList<>();
        for (String key : CNF_map.keySet()) {
            for (String terminal : CNF_map.get(key)) {
                if (terminal.length() == 1) {
                    if (Character.isLowerCase(terminal.charAt(0)) || Character.isDigit(terminal.charAt(0))) {
                        if(!terminals.contains(terminal)) {
                            terminals.add(terminal);
                            System.out.println(terminal);
                        }
                    }
                }
            }
        }
        System.out.println("RULES");
        for (String key : CNF_map.keySet()) {
            for (String value : CNF_map.get(key)) {
                System.out.println(key+":"+value);
            }
        }
        System.out.println("START\nS0");

    }
}
