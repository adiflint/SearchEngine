package partA;

import com.sun.org.apache.xpath.internal.operations.Equals;

import java.util.*;


public class Term implements java.io.Serializable {

    int m_totalFrecInCurpose ;
    String m_Term;
    HashMap<String, Integer> m_docsDictionary;
    public boolean isOnlyCapital;


    //here we create a new term that appears at the first time
    public Term(String term, String docID) {
        this.isOnlyCapital = isStartWithCapital(term);

        if( term.toLowerCase().charAt(0)>='a' && term.toLowerCase().charAt(0)<='z'){
            term = term.toLowerCase();
        }
        this.m_totalFrecInCurpose++;
        this.m_Term = term.trim();
        m_docsDictionary = new HashMap<>();
        m_docsDictionary.put(docID, 1);
    }



    public Term (String term ,int totalFres,String isCapital, HashMap<String, Integer>docs){
        this.m_Term = term;
        this.m_totalFrecInCurpose=totalFres;
        if(isCapital.contains("f")){
            this.isOnlyCapital = false;
        }
        else{
            this.isOnlyCapital=true;
        }
        this.m_docsDictionary = docs;
    }

    public String getM_Term() {
        return m_Term.trim();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Term){
            return this.m_Term.equals(((Term) obj).m_Term);
        }
        return false;
    }

    @Override
    public String toString() {
        return TempSerialize();
    }

    public int df(){
        return m_docsDictionary.size();
    }

    public int tfInDocument(String docId){
        return m_docsDictionary.get(docId);
    }

    public void updateTerm(String term, String docId ){
        if( isOnlyCapital){
            isOnlyCapital = isStartWithCapital(term);
        }
        //if doc already exists in the term's doc list, increase in 1
        if( m_docsDictionary.containsKey(docId)){
            int currFreq= m_docsDictionary.get(docId);
            m_docsDictionary.replace(docId, currFreq+1);
        }
        // if not -create a new docID key
        else{
            this.m_docsDictionary.put(docId,1);
        }
        m_totalFrecInCurpose++;
    }

    private boolean isStartWithCapital(String term){
        if( term != null && term.length()>=1)
            return (term.charAt(0)>='A' && term.charAt(0)<='Z');
        return false;

    }

    /**
    serializing in the temporery files
     **/
    String TempSerialize(){ //לעשות בלי האובייקט טרם פריקוונסי

        List<String> docsInTerm = new ArrayList<>(m_docsDictionary.keySet());
        String[] serializedDocFreqs = new String[docsInTerm.size()*2];
        int index = 0 ;
        for (int i=0; i< docsInTerm.size()&&index<=serializedDocFreqs.length ; i++){
            serializedDocFreqs[index] = docsInTerm.get(i);
            serializedDocFreqs[index+1]= this.m_docsDictionary.get(docsInTerm.get(i)).toString();
            index=index+2;
        }
        String boolS = ""+this.isOnlyCapital;
        char bool = boolS.charAt(0);
        return m_Term.trim()+"\n"+ bool+":"+m_totalFrecInCurpose+":"+
                String.join("|", serializedDocFreqs)+"\n";
    }

    //getters and setters
    public int getM_totalFrecInCurpose() {
        return m_totalFrecInCurpose;
    }

    public void setM_totalFrecInCurpose(int m_totalFrecInCurpose) {
        this.m_totalFrecInCurpose = m_totalFrecInCurpose;
    }

    public void setM_Term(String m_Term) {
        this.m_Term = m_Term.trim();
    }

    public HashMap<String, Integer> getM_docsDictionary() {
        return m_docsDictionary;
    }

    public void setM_docsDictionary(HashMap<String, Integer> m_docsDictionary) {
        this.m_docsDictionary = m_docsDictionary;
    }

    public boolean isOnlyCapital() {
        return isOnlyCapital;
    }

    public void setOnlyCapital(boolean onlyCapital) {
        isOnlyCapital = onlyCapital;
    }

    //
//    private int getTotalTf(){
//        int total = 0;
//        for(String doc : m_DicOfDocs.keySet()){
//            total = total + m_DicOfDocs.get(doc);
//        }
//        return total;
//    }

    //marge to ident' terms
    void merge(Term term) {
        this.isOnlyCapital = this.isOnlyCapital&&term.isOnlyCapital;
        this.m_totalFrecInCurpose = this.m_totalFrecInCurpose+term.m_totalFrecInCurpose;
        List<String> docsID = new ArrayList<>(term.m_docsDictionary.keySet());
        for (String docId : docsID){
            if(this.m_docsDictionary.containsKey(docId)){
                int tdf = this.m_docsDictionary.remove(docId);
                this.m_docsDictionary.put(docId,tdf+term.m_docsDictionary.get(docId));
            }
            else {
                this.m_docsDictionary.put(docId,term.m_docsDictionary.get(docId));
            }
        }

    }

}
