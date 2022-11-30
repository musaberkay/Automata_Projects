import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static ArrayList<String> alphabet = new ArrayList<String>();
    public static ArrayList<String> states = new ArrayList<String>();
    public static String startState = null;
    public static ArrayList<String> endStates = new ArrayList<String>();
    public static HashMap<String, HashMap<String, String>> transitionsNFA = new HashMap<>();
    public static HashMap<String, HashMap<String, String>> transitionsDFA = new HashMap<>();
    public static String current = null;

    public static void main(String[] args) throws FileNotFoundException {
        load();
        converter(startState);
        printDFA();
    }

    public static void load() throws FileNotFoundException {
        File file = new File("NFA1.txt");
        Scanner scanner = new Scanner(file);
        scanner.nextLine();

        while(scanner.hasNextLine()){
            current = scanner.nextLine();
            if(current.equals("STATES")){
                break;
            }
            alphabet.add(current);
        }
        while(scanner.hasNextLine()){
            current = scanner.nextLine();
            if(current.equals("START")){
                break;
            }
            states.add(current);
        }
        startState = scanner.nextLine();
        scanner.nextLine();
        while(scanner.hasNextLine()){
            current = scanner.nextLine();
            if(current.equals("TRANSITIONS")){
                break;
            }
            endStates.add(current);
        }
        while(scanner.hasNextLine()){
            current = scanner.nextLine();
            if(current.equals("END")){
                break;
            }
            String[] link = current.split(" ");
            if(transitionsNFA.containsKey(link[0])){
                if(transitionsNFA.get(link[0]).keySet().contains(link[1])){
                    transitionsNFA.get(link[0]).put(link[1],
                            transitionsNFA.get(link[0]).get(link[1]) + " " + link[2]);
                }
                else{
                    transitionsNFA.get(link[0]).put(link[1], link[2]);
                }
            }
            else{
                transitionsNFA.put(link[0], new HashMap<String, String>());
                transitionsNFA.get(link[0]).put(link[1],link[2]);
            }
        }
    }

    public static String union(String str1, String str2){
        String finalStr = "";
        String[] arr1 = str1.split(" ");
        String[] arr2 = str2.split(" ");

        for(int i = 0; i < arr1.length; i++){
            if(!finalStr.contains(arr1[i])){
                finalStr = finalStr + " " + arr1[i];
            }
        }

        for(int i = 0; i < arr2.length; i++){
            if(!finalStr.contains(arr2[i])){
                finalStr = finalStr + " " + arr2[i];
            }
        }

        return finalStr.trim();
    }

    public static String targetFinder(String state, String alph){
        String finalStr = "";
        for(String subState : state.split(" ")){
            if(transitionsNFA.keySet().contains(subState)){
                if(transitionsNFA.get(subState).keySet().contains(alph)){
                    finalStr = union(finalStr,transitionsNFA.get(subState).get(alph));
                }
                else{
                    finalStr = union(finalStr,"");
                }
            }
            else{
                finalStr = union(finalStr,"");
            }
        }
        return finalStr.trim();
    }

    public static void converter(String state){
        transitionsDFA.put(state, new HashMap<String,String>());
        for(String alph : alphabet){
            String targetState = targetFinder(state, alph);
            transitionsDFA.get(state).put(alph, targetState);
            boolean isRepeated = false;
            for(String subState : transitionsDFA.keySet()){
                if(subState.equals(targetState)){
                    isRepeated = true;
                    break;
                }
            }
            if(!isRepeated){
                converter(targetState);
            }
        }
    }

    public  static void printDFA(){
        System.out.println("ALPHABET");
        for(String alph : alphabet){
            System.out.println(alph);
        }
        System.out.println("STATES");
        for(String state : transitionsDFA.keySet()){
            if(state == ""){
                System.out.println("DeadEnd");
            }
            else {
                System.out.println(state.replaceAll("\\s", ""));
            }
        }
        System.out.println("START");
        System.out.println(startState);
        System.out.println("FINAL");
        for(String state : transitionsDFA.keySet()){
            for(String end : endStates){
                if(state.contains(end)){
                    System.out.println(state.replaceAll("\\s", ""));
                }
            }
        }
        System.out.println("TRANSITIONS");
        for(String state : transitionsDFA.keySet()){
            for(String alph: alphabet){
                if(state == ""){
                    System.out.println("DeadEnd "+ alph + " DeadEnd");
                }
                else{
                    if(transitionsDFA.get(state).get(alph)==""){
                        System.out.println(state.replaceAll("\\s", "") +" "+ alph + " DeadEnd");
                    }
                    else{
                        System.out.println(state.replaceAll("\\s", "") +" "+ alph + " " + transitionsDFA.get(state).get(alph).replaceAll("\\s", ""));
                    }
                }
            }
        }
        System.out.println("END");

    }
}