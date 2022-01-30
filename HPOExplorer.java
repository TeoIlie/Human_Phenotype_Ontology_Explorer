import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
public class HPOExplorer {
    /*
    HPOExplorer is the main driving class of the program. It reads a file HPO.txt and builds a tree
    structure in an ArrayList of Term object 'terms', where each Term is linked to its children and its
    single parent. It then uses the Query class to process the query.txt file and output the results.txt file,
    as well as to write the maxpath.txt file. (see Query class description).
     */

    //Attributes
    private static  ArrayList<Term> terms= new ArrayList<>();    //terms stores all the term objects in an ArrayList
                                                                //sorted by their id's

    public static void main(String[] args) {
    /*
    ================================
    main function drives the program
    ================================
     */
        //First we read the contents of the HPO.txt file and put the terms into Term object in attribute 'terms'
        buildTermsList();

        //Second, we go through the list of terms and assign parents and children
        assignRelations();

        //Read query.txt file and put answers in file results.txt
        Query queryObj = new Query(terms);
        queryObj.processQueryFile();

        //Calculate maximum path in terms tree, and put results in maxpath.txt file
        queryObj.buildMaxpathFile();
    }

    private static void buildTermsList() {
    /*
    ================================================================
    buildTerms fills 'terms' with the terms read from the HPO.txt file
    ================================================================
    */
        Scanner fromFileStream = null;

        // Open the HPO file. Check for file I/O exceptions.
        try {
            fromFileStream = new Scanner(new FileInputStream("HPO.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("Error opening the file \"HPO.txt\". File reading aborted.");
            System.exit(0);
        }

        //Put all the lines of the HPO file into an ArrayList of Strings 'HPOLines'
        String inputLine;
        ArrayList<String> HPOLines = new ArrayList<>();

        while (fromFileStream.hasNextLine()) {
            inputLine = fromFileStream.nextLine();
            HPOLines.add(inputLine);
        }

        //close file reading object
        fromFileStream.close();

        //Now take 'HPOLines' and use it to build the Term objects in ArrayList 'terms'
        //Find a line that says '[Term]' and pass from there to an empty line. This information is one term

        int index = 0;          //index in HPOLines
        ArrayList<String> currTerm = new ArrayList<>();
        String currLine;

        while (index < HPOLines.size()) {
            //traverse the HPOLines ArrayList

            currLine = HPOLines.get(index);

            if (currLine.equals("[Term]")) {
                //when a line [Term] has been found, put everything from it until an empty line in currTerm
                currTerm.clear();

                while (!currLine.equals("")) {
                    currTerm.add(currLine);
                    index++;
                    if (index < HPOLines.size())
                        currLine = HPOLines.get(index);
                }
                buildTerm(currTerm);
                //buildTerm takes the list of strings 'currTerm' and builds a term, if it is not obsolete
                //and puts it in attribute 'terms' in it's correct order s.t. terms is sorted by id's

            }
            index++;
        }
    }

    private static void assignRelations() {
    /*
    ======================================================================================================
    assigns to all the terms their single parent, given by is_a, and their children, which may be multiple.
    ======================================================================================================
     */
        Term curr;
        Term parent;
        int parentIndex;

        for (int i = 1; i < terms.size(); i++) {    //traverse 'terms'. Omit term at index 0 'all' has no parents.
            //find the parent of term at index i, store in 'parent' Term object
            curr = terms.get(i);
            parentIndex = getLocation(curr.getIs_a());

            parent = terms.get(parentIndex);

            //assign parent to child, and child to parent
            curr.setParent(parent);
            parent.addChild(curr);
        }
    }

    private static void buildTerm(ArrayList<String> termLines){
    /*
    builds a term using the ArrayList of lines 'termLines' of a term, and puts it in the correct position in terms.
    discards the term if it is_obsolete. Uses first instance if 'is_a' for the is_a attribute of the term.
     */
        //First determine if the term is obsolete (has a line starting with is_obsolete)
        String[] currLine;

        for (int i = 0; i < termLines.size(); ++i){

            currLine = termLines.get(i).split(":");

            if (currLine[0].equals("is_obsolete"))
                return;
        }

        //If it's not obsolete, we isolate the id and the is_a (the first one)
        //We also build 'content string - the whole term as a string for use in the constructor

        int id = 0;     //holds the id
        int is_a = 0;   //hols the is_a
        boolean is_aFound = false;  //flag if is_a has been found, to ensure we take the first occurence
        String content = "";

        for (int i = 0; i < termLines.size(); ++i){
            content += termLines.get(i);
            content += "\n";
            currLine = termLines.get(i).split(":");

            if (currLine[0].equals("is_a") && !is_aFound){
                //save the correct is_a
                is_a = Integer.parseInt(currLine[2].split(" ")[0]);
                is_aFound = true;
            }
            if (currLine[0].equals("id")){
                //save the id
                id = Integer.parseInt(currLine[2]);
            }
        }

        //Now build the term 'curr'
        Term curr = new Term(id, is_a, content);

        //Insert curr into the right place in 'terms'
        int indexInTerms = getInsertingLocation(id);

        //Add the term 'curr' in terms attribute
        terms.add(indexInTerms, curr);
    }

    private static int getInsertingLocation(int id){
    /*
    returns the integer index a term should be inserted in terms using binary search
     */
        //first guarantee the terms has at least two elements to use the algorithm
        //special cases for 0 or 1 elements
        if (terms.isEmpty())
            return 0;

        //now 'terms' is known to be at least two elements
        int first = 0;
        int last = terms.size() - 1;
        int mid = 0;
        boolean found = false;

        while (first <= last && !found) {

            mid = (first + last) >>> 1;     //computes the floor of (first + last)/2

            if ((mid == terms.size() - 1) || (terms.get(mid).getId() < id && terms.get(mid+1).getId() > id)) {
                found = true;
            } else if (terms.get(mid).getId() < id) {
                first = mid + 1;
            } else {
                last = mid - 1;
            }
        }

        if  (found)
            return mid + 1; //return mid + 1 because to insert between indices i and i+1 in an array list, the syntax
                            //requires you return i+1
        else
            return -1;
    }

    public static int getLocation(int id){
    /*
    return the index of term with id 'id' using binary search.
     */
        int first = 0;
        int last = terms.size() - 1;
        int mid = 0;
        boolean found = false;

        while (first <= last && !found) {

            mid = (first + last) >>> 1;     //computes the floor of (first + last)/2

            if (id == terms.get(mid).getId()) {
                found = true;
            } else if (terms.get(mid).getId() < id) {
                first = mid + 1;
            } else {
                last = mid - 1;
            }
        }

        if  (found)
            return mid;
        else
            return -1;
    }
}
