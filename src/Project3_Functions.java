import java.util.HashMap;

public class Project3_Functions {

    static String Generate_LSystem(int iterations, String start, HashMap<Character, String> rules) {
        //TODO: Implement the L-System generator. Apply the rules to the starting string, and loop some number of times.
        if(iterations == 0){
            return start;
        }
        String currString = start;
        String currRetString = "";
        // *A* > B*
        for (int i = 0; i < iterations; i++) {
            currRetString = "";
            for (int j = 0; j < currString.length(); j++) {
                if (rules.containsKey(currString.charAt(j))) {
                    currRetString += rules.get(currString.charAt(j));
                }
                else{
                    currRetString += currString.charAt(j);
                }
            }
            currString = currRetString;
        }
        return currRetString;
        //Return value included so code compiles.


    }
}