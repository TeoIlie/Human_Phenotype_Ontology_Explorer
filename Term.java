import java.util.*;
public class Term {
    /*
    Term class remembers the id, is_a, content, parent, and children of a term in the text file HPO.txt.
    It has getter methods for all of these, as well as mutator methods setParent and addChild.
     */

    //Attributes
    private int id;     //stores the id
    private int is_a;   //stores the is_a - id of parent
    private String content; //stores the entire term as a String
    private Term parent;    //stores the parent Term object
    private ArrayList<Term> children = new ArrayList<>();   //stores the children Term objects

    //Constructor
    Term(int id, int is_a, String content){
        this.id = id;
        this.is_a = is_a;
        this.content = content;
    }

    //Methods

    //Getter methods
    public int getId() {
        return id;
    }

    public int getIs_a() {
        return is_a;
    }

    public String getContent() {
        return content;
    }

    public Term getParent() {
        return parent;
    }

    public boolean isTermLeaf() {
        return children.isEmpty();
    }

    //Mutator methods
    public void setParent(Term parent){
        this.parent = parent;
    }

    public void addChild(Term child){
        this.children.add(child);
    }
}
