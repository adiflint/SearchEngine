package partB;
import javafx.util.Pair;
import partA.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Ranker {

    private ArrayList<Term> queryTerms;
    private HashSet<String> docs;
    private DictionaryMeneger dictionaryMeneger;
    private boolean isStem;
    private HashMap<String, String> mapOfIndexToDocID;
    private ArrayList<Pair<String,Double>> allDocsRanks;
    private HashMap<String, ArrayList<Pair<String, Double>>> totalScoredDoc;
    private computeRanking computeRanking;


    public Ranker( DictionaryMeneger dictionaryMeneger, boolean isStem, ParseQuery parseQuery) {
        this.docs = new HashSet<>();
        this.isStem =isStem;
        this.dictionaryMeneger = dictionaryMeneger;
        this.totalScoredDoc = new HashMap<>();
        //dictionaryMeneger.loadAllDictionary();
        dictionaryMeneger.setToStem(isStem);
        mapOfIndexToDocID = dictionaryMeneger.getMapOfIndexToDocID();
        this.computeRanking = new computeRanking(dictionaryMeneger,parseQuery);

    }

    //constractor for readFile
    public Ranker(DictionaryMeneger dictionaryMeneger, boolean isStem){
        this.docs = new HashSet<>();
        this.isStem =isStem;
        this.dictionaryMeneger = dictionaryMeneger;
        this.totalScoredDoc = new HashMap<>();
        dictionaryMeneger.loadAllDictionary(isStem);
        dictionaryMeneger.setToStem(isStem);
        mapOfIndexToDocID = dictionaryMeneger.getMapOfIndexToDocID();
    }

    public ArrayList<Pair<String,Double>> runRanking(ArrayList<Term> terms,String qID)throws IOException{
        long startTime = System.currentTimeMillis();
        this.queryTerms = terms;
        getQueryTermsFromPosting();
        this.docs = getFirstListOfDoc();
        this.allDocsRanks = computeRanking.computeRenkingForQuery(queryTerms,terms,qID, this.docs);
        sortRank();
        //get50

        long endTime = System.currentTimeMillis();
        System.out.println("Finish Query : "+qID +"  after total time in seconds: "+(endTime-startTime));
        return get50FirstDocs(allDocsRanks);

    }

    private HashSet<String> getFirstListOfDoc(){
        HashSet<String> docsList = new HashSet<>();
        for (Term term : queryTerms){
            for (String docID : term.getM_docsDictionary().keySet()){
                if(!docsList.contains(docID)){
                    docsList.add(docID);
                }
            }
        }
        cleanFromQueryDoc();
        return docsList;
    }

    /*
    takes only terms from the query that exists in the dictionary
     */
    private void cleanFromQueryDoc(){
        ArrayList<Term> queryList = new ArrayList<>();
        for (Term term : queryTerms){
            if(dictionaryMeneger.getTermDictionary().containsKey(term.getM_Term())||
                    dictionaryMeneger.getTermDictionary().containsKey(term.getM_Term().toUpperCase())){
                queryList.add(term);
            }
        }
        this.queryTerms = queryList;
    }

    private boolean containsOnlyNumbers(String s) {
        if(s!=null){
            for (char c : s.toCharArray()){
                if((c>='a'&&c<='z')||(c>='A'&&c<='Z')){
                    return false;
                }
            }
        }
        return true;
    }

    private void getQueryTermsFromPosting() throws IOException{

        ArrayList<Term>newTerm = new ArrayList<>();
        for( Term term :queryTerms){
            String currTerm = term.getM_Term();
            int line=0;
            String[] locationLowerCase = dictionaryMeneger.getTermDictionary().get(currTerm.toLowerCase());
            String[] locationApperCase = dictionaryMeneger.getTermDictionary().get(currTerm.toUpperCase());

            //todo: new changes here

            if(currTerm.endsWith("s")){
                String termNoS = currTerm.substring(0,currTerm.length()-1);
                String[] locationLowerCase1 = dictionaryMeneger.getTermDictionary().get(termNoS.toLowerCase());
                if( locationLowerCase1 != null){
                    line =Integer.parseInt(locationLowerCase1[2]);
                    String termInfo = readLine(line, currTerm);
                    Term termtoAdd = PostingDesrialize(termInfo, termNoS);
                    newTerm.add( termtoAdd);
                }
                String[] locationApperCase2 = dictionaryMeneger.getTermDictionary().get(termNoS.toUpperCase());
                if( locationApperCase2 != null){
                    line =Integer.parseInt(locationApperCase2[2]);
                    String termInfo = readLine(line, currTerm);
                    Term termtoAdd = PostingDesrialize(termInfo, termNoS);
                    newTerm.add( termtoAdd);
                }
            }
            if(currTerm.endsWith("es")){
                String termNoS = currTerm.substring(0,currTerm.length()-2);
                String[] locationLwoerCase3 = dictionaryMeneger.getTermDictionary().get(termNoS.toLowerCase());
                if( locationLwoerCase3 != null){
                    line =Integer.parseInt(locationLwoerCase3[2]);
                    String termInfo = readLine(line, currTerm);
                    Term termtoAdd = PostingDesrialize(termInfo, termNoS);
                    newTerm.add( termtoAdd);
                }
                String[] locationApperCase4 = dictionaryMeneger.getTermDictionary().get(termNoS.toUpperCase());
                if( locationApperCase4 != null){
                    line =Integer.parseInt(locationApperCase4[2]);
                    String termInfo = readLine(line, currTerm);
                    Term termtoAdd = PostingDesrialize(termInfo, termNoS);
                    newTerm.add( termtoAdd);
                }
            }

            // todo: up hereee

            if( locationLowerCase == null &&locationApperCase==null)
                continue;
            if(locationApperCase!=null){
                line =Integer.parseInt(locationApperCase[2]);
            }
            else if (locationLowerCase!=null){
                line =Integer.parseInt(locationLowerCase[2]);
            }

            String termInfo = readLine(line, currTerm);
            Term termtoAdd = PostingDesrialize(termInfo, currTerm);
            newTerm.add( termtoAdd);
        }

        this.queryTerms = newTerm;
    }

    public String readLine(int line, String currTerm) throws IOException {
        String path = getPostingPath(currTerm);
        String lineToRead = "";
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            lineToRead = lines.skip(line-1).findFirst().get();
        }
        return lineToRead;
    }

    /**
     *     find the posting file of the term
     */
    private String getPostingPath(String termName) {
        String folderName ="";
        //folder name
        if(isStem){
            folderName ="Stemmer-PostingFiles";
        }
        else
            folderName = "PostingFiles";
        //file name
        String postingFileName = dictionaryMeneger.getPostingFilesMapping().get(termName.substring(0,1).toUpperCase());

        return dictionaryMeneger.getPath()+"\\"+folderName+"\\"+postingFileName;

    }

    private Term PostingDesrialize(String serializedObj, String termName){
        String[] splitted = serializedObj.split("[\\:\\n]");
        if(splitted.length<=2||splitted[0]==null||splitted[1]==null||splitted[2]==null) {
            return null;
        }
        String bool = splitted[0];
        int totalFres = Integer.parseInt(splitted[1]);
        String[] splittedDocFreqs = splitted[2].split("[|]");
        HashMap<String, Integer> docFreqs = new HashMap<>();
        for (int i = 0; i < splittedDocFreqs.length - 1; i = i + 2) {
            if (splittedDocFreqs[i] == null || splittedDocFreqs[i + 1] == null) {
                continue;
            }
            String docID = mapOfIndexToDocID.get(splittedDocFreqs[i]);
            int frecTermInDoc = Integer.parseInt(splittedDocFreqs[i + 1]);
            docFreqs.put(docID, frecTermInDoc);
        }
        return new Term(termName,totalFres,bool,docFreqs);
    }


    public void finalRank(){
        HashMap<String, ArrayList<Pair<String, Integer>>> entitiesByDocs = EntitiesByDocs();
        for ( String docID : entitiesByDocs.keySet()){
            totalScoredDoc.put(docID, new ArrayList<>());
            ArrayList<Pair<String, Double>> allscoreByDoc = rankEntitiesForDoc(entitiesByDocs.get(docID));
            ArrayList<Pair<String, Double>> newScoredArray = new ArrayList<>();
            for( Pair pair : allscoreByDoc){
                String pair_value =(String)pair.getKey();
                String numInCorpus_s = dictionaryMeneger.getTermDictionary().get(pair_value.toUpperCase())[1];
                double numInCorpus_i = Double.parseDouble(numInCorpus_s);
                double totalInCorpus =numEntityInCorpus(allscoreByDoc);
                double normalizedByCorpous = numInCorpus_i/totalInCorpus;
                double final_score = 0.9*(Double)pair.getValue() + 0.1*normalizedByCorpous;
                BigDecimal bd = new BigDecimal(final_score);
                bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
                final_score = bd.doubleValue();
                newScoredArray.add(new Pair<String, Double>((String)pair.getKey(), final_score));
            }
            totalScoredDoc.put(docID,chooseTop5(newScoredArray));
        }
        dictionaryMeneger.setDocs5TopEntities(totalScoredDoc,isStem);
    }

    private ArrayList<Pair<String, Double>> get50FirstDocs(ArrayList<Pair<String,Double>> allDocsRanks) {
        ArrayList<Pair<String, Double>> listOf50 = new ArrayList<>();
        for( int i =0; i<allDocsRanks.size(); i++){
            if( i == 50){
                break;
            }
            listOf50.add(allDocsRanks.get(i));
        }
        return listOf50;
    }

    private ArrayList<Pair<String, Double>> chooseTop5(ArrayList<Pair<String, Double>> newScoredArray) {
        Collections.sort(newScoredArray, new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        Collections.reverse(newScoredArray);
        ArrayList<Pair<String, Double>> chooseTop5 = new ArrayList<>();

        for( int i =0; i<5 ; i++){
            if(i>=newScoredArray.size() )
                break;
            chooseTop5.add(newScoredArray.get(i));
        }
        return chooseTop5;
    }

    /*
    checks who is real entity
     */
    public  HashMap<String, ArrayList<Pair<String, Integer>>> EntitiesByDocs(){
        //this function return maximum 10 entities or less per doc
        HashMap<String, String[]> entitiesInDoc = dictionaryMeneger.getEntityDocMaxFreq();
        /*
        now we check for each doc if entities are really entities or not  !
        we create Hash map of docID-ArrayList (pair: entity, freq )(now its in string[])
         */
        HashMap<String, ArrayList<Pair<String, Integer>>> finalEntitiesPerDoc = new HashMap<>();
        //ArrayList<String> keySet = addKeySet(entitiesInDoc);
        for( String docID : entitiesInDoc.keySet()){
            ArrayList<Pair<String, Integer>> listOfEntity = new ArrayList<>();
            // now we see the string[] size 20
            String[] array = entitiesInDoc.get(docID);
            for( int i=0; i<array.length ; i+=2){
                String entity = array[i];
                if( entity == null)
                    break;
                /*  if is real entity*/
                if( dictionaryMeneger.getTermDictionary().containsKey(entity.toUpperCase())){
                    int freq = Integer.parseInt(array[i+1]);
                    listOfEntity.add(new Pair<>(entity, freq));
                }
                else
                    continue;
            }
            //for each doc we have list of entities-FREQ
            finalEntitiesPerDoc.put(docID, listOfEntity);
        }
        return finalEntitiesPerDoc;
    }
    public int numOfEntitiesFreqInDoc( ArrayList<Pair<String, Integer>> entitiesInDoc ){
        int counter =0;
        for( Pair pair : entitiesInDoc){
            counter+= (int)pair.getValue();
        }
        return counter;
    }
    /*
     doc id- totalEntities in doc
     */
    public HashMap<String, Integer> docWithTotalEntities(HashMap<String, ArrayList<Pair<String, Integer>>> array){
        HashMap<String, Integer> totalEntitiesInDoc = new HashMap<>();
        for( String docID : array.keySet()){
            totalEntitiesInDoc.put(docID,numOfEntitiesFreqInDoc(array.get(docID)));
        }
        return totalEntitiesInDoc;
    }
    /*
    now we need to creat pairs: entiy - score
     */

    public ArrayList<Pair<String, Double>> rankEntitiesForDoc(ArrayList<Pair<String, Integer>> entitiesInDoc){
        int totalEntities= numOfEntitiesFreqInDoc(entitiesInDoc);
        ArrayList<Pair<String, Double>> rankedEntities = new ArrayList<>();
        for( Pair pair :entitiesInDoc ){
            String entity = (String)pair.getKey();
            int value =(int)pair.getValue();
            double score = (double)value/totalEntities;
            rankedEntities.add(new Pair<>(entity,score));
        }
        return rankedEntities;
    }

    public double numEntityInCorpus ( ArrayList<Pair<String, Double>> array){
        //array : entity  -מס הופעות
        double counter = 0;
        for( Pair pair : array){
            String toUpper =(String) pair.getKey();
            String freq =dictionaryMeneger.getTermDictionary().get(toUpper.toUpperCase())[1];
            counter+=Double.parseDouble(freq);
        }
        return counter;
    }

    public void printRanking(){
        for (Pair rankAndDoc : allDocsRanks){
            System.out.println("the document: "+rankAndDoc.getKey()+", gut a rank of: "+rankAndDoc.getValue());
        }
    }

    private void sortRank(){
        if(allDocsRanks!=null){
            Collections.sort(allDocsRanks, new Comparator<Pair<String, Double>>() {
                @Override
                public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
            Collections.reverse(allDocsRanks);
        }
    }

    public ArrayList<Pair<String, Double>> getAllDocsRanks() {
        return allDocsRanks;
    }

    public void setAllDocsRanks(ArrayList<Pair<String, Double>> allDocsRanks) {
        this.allDocsRanks = allDocsRanks;
    }
}
