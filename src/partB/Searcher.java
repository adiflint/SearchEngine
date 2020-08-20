package partB;
import javafx.util.Pair;
import partA.*;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;


public class Searcher {
    //update dictionary path

    private int numberOfDoc = 50 ;
    private boolean isSteming;
    private ParseQuery parseQuery;
    private DictionaryMeneger dictionaryMeneger;
    private Ranker ranker;
    private ArrayList<Query> queriesAsDocs;
    private HashMap<String, ArrayList<Term>> quariesToListOfTerms;
    //the final renking fot each query
    private HashMap<String,ArrayList<Pair<String,Double>>> finalRankForEach;
    private boolean semantic;

    public Searcher(DictionaryMeneger dictionaryMeneger ,boolean toStem , boolean semantic) throws IOException {
        this.isSteming = toStem;
        this.quariesToListOfTerms = new HashMap<>();
        this.parseQuery = new ParseQuery(dictionaryMeneger.getPath(),toStem);
        this.dictionaryMeneger = dictionaryMeneger;
        this.queriesAsDocs = new ArrayList<>();
        dictionaryMeneger.loadAllDictionary(toStem);
        this.finalRankForEach = new HashMap<>();
        this.semantic =semantic;
    }
    /**
     * this function will read a
     * @param path
     */
    public void setASetOfQueries(String path)throws IOException{
        ArrayList<Pair<String,Double>> rankedDocForQuery = new ArrayList<>();
        finalRankForEach = new HashMap<>();

        HashMap<String, ArrayList<Term>> queriesWithTerms = parseQuery.ParseQueriesFromFile(new File(path));
        ArrayList<Query> queries =parseQuery.getQueries();
        ranker = new Ranker(dictionaryMeneger,isSteming,parseQuery);
   //     for( String queryID : queriesWithTerms.keySet()){
        //todo: see my new changes
        for( Query query: queries){
            String origQuer = query.getQuery();
            ArrayList<Term> termsPerQ = queriesWithTerms.get(query.getNum());
            if( semantic) {
                // here  get the query
                origQuer =origQuer +" "+addSemanticTerms(termsPerQ);
                ArrayList<Term> termsToRankWithSemantic = parseQuery.ParseQueryFromString(origQuer);
                rankedDocForQuery=ranker.runRanking(termsToRankWithSemantic,query.getNum());
            }
            else{
                rankedDocForQuery =ranker.runRanking(termsPerQ,query.getNum());
            }
            finalRankForEach.put(query.getNum(),rankedDocForQuery);
        }
        writResult();
        showRanking();
    }

    public void setQuery(String query)throws IOException{
        ArrayList<Pair<String,Double>> rankedDocForQuery;
        finalRankForEach = new HashMap<>();
        ranker = new Ranker(dictionaryMeneger,isSteming,parseQuery);
        ArrayList<Term> termsToRank = parseQuery.ParseQueryFromString(query);
        // new semantic
        if( semantic) {
            query = query +" "+addSemanticTerms(termsToRank);
            ArrayList<Term> termsToRankWithSemantic = parseQuery.ParseQueryFromString(query);
            rankedDocForQuery=ranker.runRanking(termsToRankWithSemantic,"1");
        }
        //end semantic -now need else
        else{
            rankedDocForQuery=ranker.runRanking(termsToRank,"1");
        }
        //this.queriesAsDocs = parseQuery.getQueries();
        //add to the final mp of score if this case the size of the map is 1
        this.finalRankForEach.put("1",rankedDocForQuery);
        showRanking();
    }
    private String addSemanticTerms(ArrayList<Term> termsPerQ) {
        ArrayList<String> termsAsStrings= termToString(termsPerQ);
        LSI lsiModel = new LSI(termsAsStrings);
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> semanticWords = new ArrayList<>();
        for( Term term : termsPerQ) {
            semanticWords = lsiModel.word2vec(term.getM_Term());
            if (semanticWords.size() == 0)
                continue;
            // now we put all this terms in string like a query
            for (String word : semanticWords) {
                stringBuilder.append(word + " ");
            }
        }
        return stringBuilder.toString().trim();
    }

    private ArrayList<String> termToString(ArrayList<Term> termsPerQ) {
        ArrayList<String> arrayStrings = new ArrayList<>();
        for( Term term: termsPerQ){
            arrayStrings.add(term.getM_Term().toLowerCase());
        }
        return arrayStrings;
    }


    public Ranker getRanker(){
        return this.ranker;
    }


    public void showRanking(){
        if(finalRankForEach==null){
            return;
        }
        /*
        create table
         */
        ArrayList<String> sortedByQueryID = sortQueryById(finalRankForEach.keySet());


        ArrayList<Pair<String,Double>> allDocsRanks;
        String[] array = new String[4];
        String[][] data = new String[finalRankForEach.size()*50][4];
        String[] column = {"queryID", "number", "docID", "score"};
        int i =0;
        for( String queryID : sortedByQueryID) {
            allDocsRanks = finalRankForEach.get(queryID);
            int index =0;
            for( Pair pair : allDocsRanks) {
                if (i>=data.length) {
                    break;
                }
                array[0] = queryID; // query id
                array[1] = array[1] = "" + (index + 1); //number of doc
                if (index >= allDocsRanks.size()) {
                    array[2] = "" + 0;
                    array[3] = "" + 0;
                } else {
                    array[2] = (String)pair.getKey(); // doc id
                    array[3] = "" + pair.getValue(); //score
                }
                data[i] = array;
                array = new String[4];
                i++;
                index++;
            }
        }
        /*
        display table
         */
        JFrame frame = new JFrame("result: relevant docs for query sorted by rank");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(500, 400));
        JTable table = new JTable(data, column);
        table.setBounds(100, 100, 200, 300);
        //table.addMouseListener(new Scrollable();
        JScrollPane panel = new JScrollPane(table);
        //add(panel, BorderLayout.CENTER);
        frame.add(panel);
        frame.setVisible(true);
    }

    //new by inbar
    public void writResult(){
        String path = dictionaryMeneger.getPath()+"\\results.txt";
        StringBuilder toWrite = new StringBuilder();
        ArrayList<String> sortedByQueryID = sortQueryById(finalRankForEach.keySet());
        for (String queryID : sortedByQueryID){
            toWrite.append(createFormat(queryID));
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(path), false));
            bufferedWriter.write(toWrite.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
        }
        catch (Exception exp) {
        }
    }

    private ArrayList<String> sortQueryById(Set<String> keySet) {
        ArrayList<String> sordet = new ArrayList<>();
        for (String key : keySet){
            sordet.add(key);
        }
        Collections.sort(sordet);
        return sordet;
    }

    private String createFormat(String queryID) {
        String Iter = "0";
        String Rank = "1";
        String Sim = "42.38";
        String Run_id = "mtLF";
        String infoForQueryByDoc ="";
        ArrayList<Pair<String,Double>> originalDocList = finalRankForEach.get(queryID);
        //ArrayList<Pair<String,Double>> sortedDocList = sortByDocId(originalDocList);
        for (Pair docAndRank : originalDocList){
            infoForQueryByDoc+= queryID+" "+Iter+" "+docAndRank.getKey()+" "+Rank+" "+Sim+" "+Run_id+"\n";
        }
        return infoForQueryByDoc;
    }

    private ArrayList<Pair<String, Double>> sortByDocId(ArrayList<Pair<String, Double>> originalDocList) {
        ArrayList<Pair<String, Double>> sorted = new ArrayList<>();
        Collections.sort(originalDocList, new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        sorted = originalDocList;
        return sorted;
    }


}
