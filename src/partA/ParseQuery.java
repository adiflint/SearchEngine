package partA;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseQuery  extends Parser{

    private ArrayList<Query> queries ;
    private HashMap<String,ArrayList<Term>> termForEachQuryByQueryNum;
    private HashMap<String,ArrayList<Term>> queryToTermsList;
    private HashMap<String,String> queryToQueryNumber ;
    private String query;

    public ParseQuery(String path,boolean isSteming)throws IOException{
        super(path,isSteming);
        this.queryToQueryNumber = new HashMap<>();
        //   this.termForEachQuryByQueryNum = new HashMap<>();
        this.queryToTermsList = new HashMap<>();
        this.queries = new ArrayList<>();
    }

    public ArrayList<Term> ParseQueryFromString(String queryTitel)throws IOException{
        ArrayList<Term> termsFromQuery = fromStringToQuery(queryTitel);
        //createTermForEachQury();
        //createMappingFromQueryTitleToQueryNumber();
        this.queries.add(new Query(queryTitel));
        return termsFromQuery;
    }

    public HashMap<String, ArrayList<Term>> ParseQueriesFromFile(File file)throws IOException{
        fromFileToQueries(file);
        //parse every query and create map: queryID- list of terms
        termsListOfQuery();
        return queryToTermsList;
    }



    /**
     * return list of query from file
     * @param file
     */
    private void fromFileToQueries(File file)throws IOException{
        ArrayList<Query>newQueries = new ArrayList<>();
        Document xmlFile = null;
        try {
            xmlFile = Jsoup.parse(file, "UTF-8");
            Elements ListOfQueries =  xmlFile.getElementsByTag("top");
            for(int i = 0 ; i < ListOfQueries.size() ; i++){
                Element query =ListOfQueries.get(i);
                Elements num = query.getElementsByTag("num");
                Elements title = query.getElementsByTag("title");
                Elements desc = query.getElementsByTag("desc");
                //Elements narr = query.getElementsByTag("narr");

                String[]queryText = num.text().substring(8).split("\\s");
                String numAsString = queryText[0];
                String titleAsString =title.text();
                String descAsString = desc.text();
                //Description:
                String[]noDescription = descAsString.split("Description:");
                if(noDescription.length>1){
                    descAsString=noDescription[1];
                }

                String[] onlyDesc = descAsString.split("Narrative:");
                if(onlyDesc.length>0){
                    descAsString=onlyDesc[0];
                }

                newQueries.add(new Query(numAsString,titleAsString,descAsString));

            }

        }
        catch (
                IOException e) {
        }
        this.queries=newQueries;
    }

    private void termsListOfQuery() throws IOException{
        HashMap<String,Term> tmpListFromParser = new HashMap<>();
        ArrayList<Doc> queriesAsDocs = queryToDoc();
        ArrayList<Doc> toParser = new ArrayList<>();
        for( Doc query : queriesAsDocs){
            toParser.add(query);
            tmpListFromParser =startParsing(toParser);
            ArrayList<Term> originalArr =  fromHashMapToArray(tmpListFromParser);

            //TODO:""
            //originalArr = createMoreTerms(originalArr);
            queryToTermsList.put(query.m_docNum,fromHashMapToArray(tmpListFromParser));
            toParser= new ArrayList<>();
        }
    }



    private ArrayList<Term> fromHashMapToArray(HashMap<String, Term> tmpListFromParser) {
        ArrayList<Term> terms = new ArrayList<>();
        for( String term : tmpListFromParser.keySet()){
            terms.add(tmpListFromParser.get(term));
        }
        return terms;
    }


    public String getQuery() {
        return query;
    }

    private void createAQueryFromString(String queryText) {
        String num = "";
        String title = "";
        String desc="";
        String narr="";
        Pattern pattern = Pattern.compile("<num>");
        Matcher matcher =pattern.matcher(queryText);
        while (matcher.find()){
            num = queryText.substring(matcher.end()+9,matcher.end()+13);
        }
    }

    /**
     * get string query and parse it
     * @param query
     * @return
     * @throws IOException
     */
    private ArrayList<Term> fromStringToQuery(String query)throws IOException{
        queries = new ArrayList<>();
        queries.add(new Query(query));
        startParsing(queryToDoc());

        HashMap<String, Term> termDic = termsDictionary;
        HashMap<String, Term> numericTermDic = NumericPatternTermsDic;
        ArrayList<Term> mergedTermsDic = maregeDics(termDic,numericTermDic);
        return mergedTermsDic;
    }

    private ArrayList<Term> maregeDics(HashMap<String, Term> termDic, HashMap<String, Term> numericTermDic) {
        ArrayList<Term> merged = new ArrayList<>();
        for (String term : numericTermDic.keySet()){
            merged.add(numericTermDic.get(term));
        }
        for (String term : termDic.keySet()){
            merged.add(termDic.get(term));
        }
        return merged;
    }

    private ArrayList<Doc> queryToDoc(){
        ArrayList<Doc> queritsInDoc =new ArrayList<>();
        for (Query q : queries){
            queritsInDoc.add((Doc)q);
        }
        return queritsInDoc;
    }

    private void createTermForEachQury(){
        getTheTermsFromDicInParse(getNumericPatternTermsDic());
        getTheTermsFromDicInParse(getTermsDictionary());
    }

    private void getTheTermsFromDicInParse(HashMap<String, Term> dic){
        for (String term : dic.keySet()){
            for (String queryNum : dic.get(term).getM_docsDictionary().keySet()){
                Term t = dic.get(term);
                if(termForEachQuryByQueryNum.containsKey(queryNum)){

                    ArrayList<Term> update = termForEachQuryByQueryNum.get(queryNum);
                    update.add(t);
                    termForEachQuryByQueryNum.replace(queryNum,update);
                }
                else {
                    ArrayList<Term> update = new ArrayList<>();
                    update.add(t);
                    termForEachQuryByQueryNum.put(queryNum,update);
                }
            }

        }
    }

    /**
     * create map from query title to query num
     */
    private void createMappingFromQueryTitleToQueryNumber(){
        for(Query query : queries){
            queryToQueryNumber.put(query.getQuery(),query.m_docNum);
        }
        return;
    }

    public ArrayList<Query> getQueries() {
        return queries;
    }

    public HashMap<String, String> getQueryToQueryNumber() {
        return queryToQueryNumber;
    }

    public HashMap<String, ArrayList<Term>> getTermForEachQuryByQueryNum() {
        return termForEachQuryByQueryNum;
    }


}
