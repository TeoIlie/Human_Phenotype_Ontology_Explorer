import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
public class Query {
    /*
    Query class has two important functions.
    1. processQueryFile reads a file queries.txt and outputs
    the term traces of the corresponding id's in a file results.txt.
    2. buildMaxpathFile outputs the max path and all path traces equal to
    the max path length into a file maxpath.txt.
     */

    //Attributes
    private final ArrayList<Term> terms;    //same terms as generated in HPOExplorer

    //Constructor
    Query (ArrayList<Term> terms){
        this.terms = terms;
    }

    //Methods
    public void processQueryFile(){
    /*
    read a file called queries.txt and write, for each id, the path trace in a text file results.txt
     */
        /*
        ==============
        Read from file
        ==============
         */
        Scanner fromFileStream = null;

        // Open the file. Check for file I/O exceptions.
        try {
            fromFileStream = new Scanner(new FileInputStream("queries.txt"));
        }
        catch (FileNotFoundException e) {
            System.out.println("Error opening the file 'queries.txt'. Program aborted!");
            System.exit(0);
        }

        //Put the lines of the query file in an array list of strings 'QueryLines'
        String inputLine;
        ArrayList<String> QueryLines = new ArrayList<>();

        while (fromFileStream.hasNextLine()) {
            inputLine = fromFileStream.nextLine();
            QueryLines.add(inputLine);
        }
        fromFileStream.close();

        //put the id's from QueryLines into an array of id's to be queried
        int[] idQueries = getIds(QueryLines);

        /*
        =============
        Write to file
        =============
         */
        PrintWriter toFileStream = null;

        // Check for file I/O exceptions.
        try {
            toFileStream = new PrintWriter(new FileOutputStream("results.txt"));
        }
        catch (FileNotFoundException e) {
            System.out.println("Error opening the file 'results.txt'. Program aborted!");
            System.exit(0);
        }

        //write the query results of each id in file 'results.txt'
        String entry;

        for (int i = 0; i < idQueries.length; i++) {
            toFileStream.println("[query_answer]");
            entry = getIdResult(idQueries[i]);
            toFileStream.println(entry);
        }
        toFileStream.close();
    }

    public void buildMaxpathFile(){
    /*
    Generates a file 'maxpath.txt' of all the term path traces corresponding to the longest path
    in the tree 'terms'.
     */
        //Create an object to write to the maxpath.txt file
        PrintWriter toFileStream = null;

        //Check for file I/O exceptions.
        try {
            toFileStream = new PrintWriter(new FileOutputStream("maxpath.txt"));
        }
        catch (FileNotFoundException e) {
            System.out.println("Error opening the file 'maxpath.txt'. Program aborted!");
            System.exit(0);
        }

        //Get an arrayList of integers representing the indices in 'terms'
        //of the terms with the maximum path length
        ArrayList<Integer> maxPathLengthIndices = getMaxPathLengthIndices();

        //Any of the indices will yield the max path length.
        int maxPathLength = getPathLength(maxPathLengthIndices.get(0));

        //Print the path trace of terms corresponding to indices in maxPathLengthIndices
        String entry = "";
        int id = 0;

        for (int i = 0; i < maxPathLengthIndices.size(); i++) {
            //Print max path length at the top of the file
            toFileStream.println("[max_path=" + maxPathLength + "]");

            id = terms.get(maxPathLengthIndices.get(i)).getId();
            entry = getIdResult(id);
            toFileStream.println(entry);
        }
        toFileStream.close();
    }

    private int[] getIds(ArrayList<String> QueryLines) {
    /*
    Takes in an array list of strings of lines of queries.txt, returns an array of integer id's to be queried
     */
        int[] idQueries = new int[QueryLines.size()];
        String currLine;

        for(int i = 0; i < QueryLines.size(); i++){
            currLine = QueryLines.get(i);
            //make sure the query line is valid, and an id can be read, else put in '-1' id
            try {
                idQueries[i] = Integer.parseInt(currLine.split(":")[2]);
            }
            catch (Exception e){
                idQueries[i] = -1;
            }
        }

        return idQueries;
    }

    private String getIdResult(int id) {
    /*
    Gets an id of a query and returns the entire entry as a single string,
    including error messages where applicable.
     */
        //save the string to be returned in entry, i.e. entry in results.txt
        String entry = "";
        //store current term as you go up the nodes
        Term currTerm;
        //store index in terms
        int indexInTerms;

        if(id == -1)
            //if the id is flagged invalid by getIds methods
            return "QUERY CANNOT BE COMPLETED. QUERY.TXT ENTRY NOT FORMATTED PROPERLY.";

        //try to get initial values of variables, granted the id exists
        try {
            indexInTerms = HPOExplorer.getLocation(id);
            currTerm = terms.get(indexInTerms);
        }
        catch (Exception e) {
            entry = "QUERY CANNOT BE COMPLETED. NO SUCH ID EXISTS.";
            return entry;
        }
        entry += currTerm.getContent();

        while (!(currTerm.getIs_a() == 0)) {
            //continue going up until the parent is 0, or in other
            //words you are at the 'all' node with id 1
            currTerm = currTerm.getParent();
            entry += "\n";
            entry += currTerm.getContent();
        }

        return entry;
    }

    private ArrayList<Integer> getMaxPathLengthIndices() {
    /*
    Checks all the 'leaf' nodes, i.e. nodes with no children, and finds the longest path
    length in the tree 'terms', returning the indices of all terms with max path.
     */
        ArrayList<Integer> maxPathLengthIndices= new ArrayList<>();
        int maxPathLength = 0;
        int currPathLength;

        for (int index = 0; index < terms.size(); index++){
            //traverse the terms
            if (terms.get(index).isTermLeaf()) {
                //only check leaf terms - they have potential of being longest paths
                currPathLength = getPathLength(index);

                if (currPathLength == maxPathLength) {
                    //add index of current path to arrayList
                    maxPathLengthIndices.add(index);
                } else if (currPathLength > maxPathLength) {
                    //if a longer path has been found, remember it in maxPathLength, update array list
                    maxPathLength = currPathLength;
                    maxPathLengthIndices.clear();
                    maxPathLengthIndices.add(index);
                }
            }
        }

        return maxPathLengthIndices;
    }

    private int getPathLength(int index) {
    /*
    Returns the path length of a term at index 'index', from itself to root 'all'.
    (i.e. path from all to all is 0 not 1).
     */
        int pathLength = 0;
        Term currTerm = terms.get(index);

        while (currTerm.getIs_a() != 0) {
            //continue until the is_a value is 0, i.e. 'all' root node reached
            pathLength++;
            currTerm = currTerm.getParent();
        }

        return pathLength;
    }
}
