package partA;
import javafx.util.Pair;
import partB.Ranker;
import runner.*;

import javax.jws.soap.SOAPBinding;
import javax.xml.crypto.dom.DOMCryptoContext;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Parser {
    private HashSet<String> stopWords;
    protected HashMap<String, Term> termsDictionary;
    protected HashMap<String, Term> NumericPatternTermsDic;
    private LinkedHashMap<String, Integer> tmpEntitiesInDoc;
    private HashMap<String, String[]> entitiesPerDoc;
    StringBuilder docText;
    Doc currDoc;
    int currOffset;
    boolean isStemming;
    String[] textArray;
    ArrayList<String> bigNumbers;
    ArrayList<String> months;
    ArrayList<Doc> listOfDocs;
    private Indexer indexer;
    private HashMap<String, String[]> docsDictionary;
    protected DictionaryMeneger dictionaryMeneger;
    private static String pathToCurpos;
    private String pathToPosting;
    private App app;
    private HashMap<String,String> listOfDocsByDocID;
    static int countOfDoc = 0;
    private HashMap<String,String> mapOfIndexToDocID;

    public Parser(String pathToCurpos , String pathToPosting, boolean isStemming) throws IOException {
        this.app = app;
        termsDictionary = new HashMap<>();
        NumericPatternTermsDic = new HashMap<>();
        entitiesPerDoc = new HashMap<>();
        tmpEntitiesInDoc = new LinkedHashMap<>();
        docText = new StringBuilder();
        currOffset = 0;
        this.isStemming = isStemming;
        bigNumbers = new ArrayList<>();
        initBigNumbers();
        months = new ArrayList<>();
        initMonthArray();
        listOfDocs = new ArrayList<>();
        this.pathToCurpos = pathToCurpos;
        this.pathToPosting = pathToPosting;
        dictionaryMeneger = new DictionaryMeneger(pathToPosting);
        dictionaryMeneger.setToStem(isStemming);
        indexer = new Indexer(pathToPosting,isStemming,dictionaryMeneger);
        docsDictionary = new HashMap<>();
        createSetOfStopWords();
        this.listOfDocsByDocID = new HashMap<>();
        this.mapOfIndexToDocID = new HashMap<>();
    }
    private void initMonthArray() {
        String[] monthStrings= _getMonths();
        for( String m:monthStrings ){
            months.add(m);
        }
    }

    private void initBigNumbers() {
        bigNumbers.add("thousand");
        bigNumbers.add("million");
        bigNumbers.add("billion");
        bigNumbers.add("trillion");
    }


    public Parser(String pathToCurpos,boolean isStemming)throws IOException {

        this.app = app;
        termsDictionary = new HashMap<>();
        NumericPatternTermsDic = new HashMap<>();
        entitiesPerDoc = new HashMap<>();
        tmpEntitiesInDoc = new LinkedHashMap<>();
        docText = new StringBuilder();
        currOffset = 0;
        this.isStemming = isStemming;
        bigNumbers = new ArrayList<>();
        initBigNumbers();
        months = new ArrayList<>();
        initMonthArray();
        listOfDocs = new ArrayList<>();
        this.pathToCurpos = pathToCurpos;
        //this.pathToPosting = pathToPosting;
        dictionaryMeneger = new DictionaryMeneger(pathToCurpos);
        dictionaryMeneger.setToStem(isStemming);
       indexer = new Indexer(pathToPosting,isStemming,dictionaryMeneger);
        docsDictionary = new HashMap<>();
        createSetOfStopWords();
        this.listOfDocsByDocID = new HashMap<>();
        this.mapOfIndexToDocID = new HashMap<>();

    }

    public HashMap<String, Term> getTermsDictionary() {
        return termsDictionary;
    }

    public DictionaryMeneger getDictionaryMeneger() {
        return dictionaryMeneger;
    }

    public void setToDictionaryMeneger(){
        dictionaryMeneger.setDocDictionary(docsDictionary);
        dictionaryMeneger.setMapOfIndexToDocID(mapOfIndexToDocID);
        dictionaryMeneger.setEntityPerDocDic(entitiesPerDoc);

        setDocMap();
    }

    private void setDocMap(){
        indexer.getPostingsFilesManager().setListOfDocByDocID(listOfDocsByDocID);
    }
    public void setDocsDictionary(HashMap<String, String[]> docsDictionary) {
        this.docsDictionary = docsDictionary;
    }

    /*
    read file  class reads to this method
    splitIntoCleanTokens send the docs to cleaning and applying all rules,
    it creates terms from the regex new forms and deletes them from the text if decided
     getNumericPatternTermsDic return the terms list. so we have two seperats lists!
     */
    public HashMap<String, Term> startParsing(ArrayList<Doc> listOfDocs) throws IOException{
        //moved from the buttum
        termsDictionary = new HashMap<>();
        NumericPatternTermsDic = new HashMap<>();

        for( Doc docToParse : listOfDocs){
            newCleanDoc(docToParse );
            //TO DELETE:
            sortTmpEntityDic();
            chooseTop10Entities(docToParse.getM_docNum());
            //init tmp entity list for next doc
            tmpEntitiesInDoc = new LinkedHashMap<>();
            docToParse.deleteText();
            docToParse.setMax_Tf();
            //todo: here changed 2:33
            docToParse.setM_sizeOfDoc(termsDictionary.size());
            //todo : up here
            //doc info is: docID : maxTF, num of unique terms, doc lengh, doc date
            String[] docInfo = {""+docToParse.getMax_Tf(),""+docToParse.getNumOfUniqueTerms(),""+docToParse.getM_sizeOfDoc(),docToParse.getM_date()};
            docsDictionary.put(docToParse.getM_docNum(),docInfo);
            listOfDocsByDocID.put(docToParse.getM_docNum(),""+countOfDoc);
            mapOfIndexToDocID.put(""+countOfDoc, docToParse.getM_docNum());
            countOfDoc++;
            //here check
//            if(docToParse.getM_docNum().equals("FBIS3-3366")){
//
//                System.out.println("list of terms from doc FBIS3-3366\n");
//                Set<String> keysInSet = docToParse.getM_termDictionary().keySet();
//                ArrayList<String> keys =new ArrayList<>(keysInSet);
//                Collections.sort(keys);
//                for(String key : keys){
//                    System.out.println("term:"+key +"\tfrequency in this doc:"+docToParse.getM_termDictionary().get(key)+"\n");
//                }
//            }
        }
        System.out.println("finish parsing-"+java.time.LocalTime.now());
        indexer.createTermFiles(termsDictionary,NumericPatternTermsDic,listOfDocs.size());
        indexer.initIndexer();
        this.listOfDocs =listOfDocs;

        return termsDictionary;
    }

    private void chooseTop10Entities(String docID) {
        //here enter to real map
        entitiesPerDoc.put(docID,new String[tmpEntitiesInDoc.size()*2]);
        int i =0;
        for( String str: tmpEntitiesInDoc.keySet()){
            //i place is the entity, i+1 is fre in doc
            //TODO: CHECK ITS REALLY SORTED
//            if(tmpEntitiesInDoc.size()< i ){
//                break;
//            }
            entitiesPerDoc.get(docID)[i] = str;
            int freq = tmpEntitiesInDoc.get(str);
            entitiesPerDoc.get(docID)[i+1] = ""+freq;
            i+=2;
        }
    }
    public Indexer getIndexer() {
        return indexer;
    }

    public ArrayList<Doc> getListOfDocs(){
        return this.listOfDocs;
    }
    public  HashMap<String, Term> getNumericPatternTermsDic(){
        return this.NumericPatternTermsDic;
    }
    String newCleanDoc(Doc doc){
        currDoc = doc;
        String text = currDoc.getM_text().toString();
        text=  text.replaceAll("|\"|\\?|\\\\|\\.\\.\\.|\\.\\.|\\||>|<|//|" , "");
        textArray = text.split("[\\s\\:\\;\\+\\=\\{\\}\\(\\)\\[\\]\\&\\&&[\\#]\\*]");
        for ( int i =0; i<textArray.length; i++){
            boolean isFraction = false;
            boolean isSlides =false;
            boolean isBig =false;
            boolean toChange = false;
            String term= textArray[i];
            String connectedWord="";
            /// todo: new
            String[] withDots = term.split("[.\\,]");
            if( withDots.length>1){
                // if its number to remove .
                boolean isNumber = containsOnlyNumbers(withDots);
                if(isNumber){
                    if( term.contains(".")){
                        //the same
                        if( term =="")
                            continue;
                    }
                    if( term.contains(",")){
                       term= removeAllCommas(withDots);
                       if( term ==""){
                           continue;
                       }
                    }
                }
                else{
                    if(withDots[0].equals("") ){
                        if(withDots.length>1 && withDots[1]!="") {
                            term =withDots[1];
                        }
                        else
                            continue;
                    }
                    else {
                        term = withDots[0];
                        //this we need to transfer !!!!!!!!!
                        connectedWord = cleanWord(textArray[i].substring(term.length() + 1));
                    }
                }
            }
            ///todo: up here
            term= cleanWord(term); //מילה נקייה
            if( term ==""){
                continue;
            }
            if( term != "" && term!= "." && term!="%" && term!="'") {
                String replacement = term;
                String entity = term;

                //is its a stop word, continue
                if (isLetter(term)&& stopWords.contains(term.toLowerCase()) && !term.equals("between")) {
                    textArray[i] = "";
                    continue;
                }
                //entities, continue
                if (startWithCapital(replacement)&& replacement.length()>0 && !replacement.equals("A") && i<textArray.length-1
                        &&((textArray[i].length()>0)&& (isLetter(""+textArray[i].charAt(textArray[i].length()-1))||  textArray[i].charAt(textArray[i].length()-1)=='-'))) {
                    int numEntity=i+1;
                    if( entity.contains("'")){
                        entity = remove_s(entity);
                    }
                    addToDic(entity, termsDictionary);
                    //maybe add funcyion that cleans only for names ?". like this
                    while (numEntity < textArray.length && endsWithLetter(entity) && (startWithCapital(cleanWord(textArray[numEntity])) ||
                            (textArray[numEntity].startsWith("-") && textArray[numEntity].length()>1&& startWithCapital(textArray[numEntity].substring(1)))))  {
                        //add to dic also each word from the entity
                        if( textArray[numEntity].contains("'")){
                            textArray[numEntity] = remove_s(textArray[numEntity]);
                        }
                       // addToDic(entity, termsDictionary);
                        addToDic(cleanWord(textArray[numEntity]), termsDictionary);
                        entity = entity + " " + textArray[numEntity];
                        numEntity++;
                    }
                    replacement = cleanWord(entity);
                    removeFromArray(i+1,numEntity-i-2);
                    i = numEntity-1;
                    if(!replacement.equals(term)) {
                        addToDic(replacement, termsDictionary);
                        addToEntitiesDic(replacement, currDoc.getM_docNum());
                    }
                    continue;
                }
                // todo: changes here

                if(!connectedWord.equals("") && term.length()>0){
                    textArray[i] =connectedWord;
                    if( i != textArray.length-1) {
                        i--;
                        toChange = true;
                    }
                }
                //todo: uphere
                //normal word (adi, adi88, 88adi), adi-inbar,continue
                // if (  term.length() >1 && !isNumber(term)&& !term.equals("between")) {
                if (  term.length() >1 && !isNumber(term.substring(0,1)) && !term.contains("$") &&!term.contains("%")) {
                    // todo: changes here
                    if( term.contains("-") || term.contains("/")){
                        //save both words sepeate
                        addSeperateWordsToDic(term);
                        addToDic(term, termsDictionary);
                    }
                    // todo: changes up here
                    //remove sufix 's
                    else if(term.endsWith("'s")){
                        int index = term.indexOf("'");
                        term = term.substring(0,index);
                        addToDic(term, termsDictionary);
                    }
                    //word
                    else if( isLetter(term.substring(0,1)) ){
                        //term dic
                        addToDic(term, termsDictionary);
                    }
                    else if( isNumber(term.substring(0,1))) //numeric
                        addToDic(term, NumericPatternTermsDic);
                    continue;
                }
                if(toChange ==true)
                    continue;
                //floating number
                if( term.contains(".") && term.indexOf('.')<term.length()-1){
                    term= floatingPointNumbar(term, term.indexOf('.'));
                    replacement= term;
                }
                // number with fraction after
                if(i<textArray.length-1 && isFraction(cleanWord(textArray[i+1]))) {
                    replacement = replacement+" "+cleanWord(textArray[i+1]);
                    isFraction= true;
                    i++;
                    // isSlides =true;
                }
                // for 4-4 or 4-word or 4/6
                if ((term.contains("-") || term.contains("/")) && term.length()>2 ) {
                    String[] numbers = term.split("[-/]+");
                    if (numbers.length>1&& term.contains("-")) {
                        // word-word-word
                        if (numbers.length >= 3 && isLetter(numbers[0]) && isLetter(numbers[1]) && isLetter(numbers[2])) {
                            addToDic(term, termsDictionary);
                        }
                        //word-word
                        else if (numbers.length >= 2 && isLetter(numbers[0]) && isLetter(numbers[1])) {
                            addToDic(term, termsDictionary);
                        }//num-word
                        else if (numbers.length >= 2 && isDigit(numbers[0]) && isLetter(numbers[1])) {
                            addToDic(term, NumericPatternTermsDic);
                        }//word-num
                        else if (numbers.length >= 2 && isLetter(numbers[0]) && isDigit(numbers[1])) {
                            addToDic(term, termsDictionary);
                        } // num-num
                        else if( numbers.length >= 2 && isDigit(numbers[0]) && isDigit(numbers[1])) {
                            replacement = getBigNumbersReplacement(numbers[0]) + "-" + getBigNumbersReplacement(numbers[1]);
                            addToDic(replacement, NumericPatternTermsDic);
                        }
                        removeFromArray(i, 0);
                        continue;
                    } else if ( numbers.length>1 &&term.contains("/")) {
                        if (isDigit(numbers[0]) && isDigit(numbers[1])) {
                            replacement = getBigNumbersReplacement(numbers[0]) + "/" + getBigNumbersReplacement(numbers[1]);
                            addToDic(replacement, NumericPatternTermsDic);
                            removeFromArray(i, 0);
                            continue;
                        }

                    }
                }

                //gram to g and kg
                if( isNumber(replacement) && i<textArray.length-1 && (cleanWord(textArray[i+1].toLowerCase()).equals("gram")||
                        cleanWord(textArray[i+1].toLowerCase()).equals("grams"))){
                    if( Double.parseDouble(replacement)>=1000){
                        replacement=Double.parseDouble(replacement)/1000+"kg";
                    }
                    else
                        replacement = replacement+"g";
                    addToDic(replacement, NumericPatternTermsDic);
                    // i++;
                    removeFromArray(i,1);
                    continue;
                }

                //month year to change again the 1.954K with the function
                if (isNumber(replacement) && Double.parseDouble(replacement) >= 1000 &&  Double.parseDouble(replacement) <=3000) {
                    //may 1994
                    if (i > 0 && months.contains((cleanWord(textArray[i - 1].toLowerCase())))) {
                        int index = months.indexOf(cleanWord(textArray[i - 1].toLowerCase()));
                        String formattedMonth = this.formatTwoDigitsNumber((index) / 2 + 1);
                        String formatDay = this.formatTwoDigitsNumber(parseNumber(term));
                        replacement = formatDay + "-" + formattedMonth;
                        addToDic(replacement, NumericPatternTermsDic);
                        continue;
                    }// 1994 april
                    else if (i < textArray.length - 1 && months.contains((cleanWord(textArray[i + 1].toLowerCase())))) {
                        int index = months.indexOf(cleanWord(textArray[i + 1].toLowerCase()));
                        String formattedMonth = this.formatTwoDigitsNumber((index) / 2 + 1);
                        String formatDay = this.formatTwoDigitsNumber(parseNumber(term));
                        replacement = formatDay + "-" + formattedMonth;
                        addToDic(replacement, NumericPatternTermsDic);
                        continue;

                    }
                }
                //check for big numbers foramat
                //with slides forward
                if (i <textArray.length - 1 &&
                        ((isNumber(replacement) || ((replacement.length() > 1 && replacement.charAt(0) == '$' && isNumber(replacement.substring(1))))) &&
                                (cleanWord(textArray[i + 1]).toLowerCase().equals("thousand")
                                        || cleanWord(textArray[i + 1]).toLowerCase().equals("million")
                                        || cleanWord(textArray[i + 1]).toLowerCase().equals("billion")
                                        || cleanWord(textArray[i + 1]).equals("trillion")))) {
                    //String s = containsMiliion(textArray[i+1]);
                    if (replacement.contains("$") && i < textArray.length - 1 && isNumber(replacement.substring(1))) {
                        replacement = "$" + getBigNumbersReplacement(cleanWord(replacement.substring(1) + textArray[i + 1].toLowerCase()));
                    } else {
                        if (i < textArray.length - 1 && replacement.length()>=1&&isNumber(replacement)){
                            ///change1@#$%^&*(
                            replacement = getBigNumbersReplacement(replacement+ cleanWord(textArray[i + 1].toLowerCase()));
                        }
                    }
                    isSlides =true;
                    isBig=true;

                }
                //check for big numbers format and $bigNumbar
                //now big numer can be with floating pint also
                else if ((!isBig && isNumber(replacement)&& (Double.parseDouble(term) >= 1000)) || (term.length() > 1 && term.charAt(0) == '$'
                        && isNumber(term.substring(1)) && Double.parseDouble(term.substring(1)) >= 1000)) {
                    if (textArray[i].contains("$")) {
                        replacement = "$" + getBigNumbersReplacement(replacement.substring(1));
                    } else  {
                        if( isNumber(replacement))
                            replacement = getBigNumbersReplacement(term);
                    }
                    isBig = true;
                }
                //datesss
                //     formattedMonth = this.formatTwoDigitsNumber((monthIndex/2) + 1);
                else if( !isBig) {
                    if (isDigit(replacement) & !isFraction) {
                        if (isNumber(replacement) && Integer.parseInt(replacement) >= 0 && Integer.parseInt(replacement) <= 31) {
                            //may 14
                            if (i > 0 && months.contains(cleanWord(textArray[i - 1].toLowerCase()))) {
                                //replacement = getMonthReplacement();
                                int index = months.indexOf(cleanWord(textArray[i - 1].toLowerCase()));
                                String formattedMonth = this.formatTwoDigitsNumber((index) / 2 + 1);
                                String formatDay = this.formatTwoDigitsNumber(parseNumber(term));
                                replacement = formattedMonth + "-" + formatDay;
                                addToDic(replacement, NumericPatternTermsDic);
                                continue;
                            }
                            //14 may
                            else if (i < textArray.length - 1 && months.contains(cleanWord(textArray[i + 1].toLowerCase()))) {
                                int index = months.indexOf(cleanWord(textArray[i + 1].toLowerCase()));
                                String formattedMonth = this.formatTwoDigitsNumber((index) / 2 + 1);
                                String formatDay = this.formatTwoDigitsNumber(parseNumber(term));
                                replacement = formattedMonth + "-" + formatDay;
                                addToDic(replacement, NumericPatternTermsDic);
                                continue;
                            }

                        }

                    }
                }
                //prices$$$$$b$check $$$$$ no extra slides
                //$price,$price million, #price billion
                if (textArray[i].contains("$") &&term.length()
                        >=1 ) {
                    if (replacement.contains("B")) {
                        replacement = replacement.substring(1, replacement.length() - 1) + "000 M Dollars";
                    } else if (replacement.contains("M")) {
                        replacement = replacement.substring(1, replacement.length() - 1) + " M Dollars";
                    } else if (replacement.contains("K")) {
                        replacement = replacement.substring(1, replacement.length() - 1) + "," + "000 Dollars";
                    } else
                        replacement = replacement.substring(1) + " Dollars";
                    addToDic(replacement, NumericPatternTermsDic);
                    if( isSlides){
                        removeFromArray(i,1);
                    }
                    else
                        removeFromArray(i,0);
                    continue;
                }
                //check % with slide to textArray[1+1]
                else if ( replacement.contains("%") || (i < textArray.length-1&&((cleanWord(textArray[i + 1]).equals("percent") ||
                        cleanWord(textArray[i + 1]).equals("percentage"))))) {
                    if (!replacement.contains("%")) {
                        replacement = term + "%";
                        removeFromArray(i,1);
                        i++;
                    }
                    else
                        removeFromArray(i,0);
                    addToDic(replacement, NumericPatternTermsDic);
                    continue;
                }

                // price dollars
                //with slides forward
                else if (isNumber(term) && i < textArray.length-1 && cleanWord(textArray[i + 1]).toLowerCase().equals("dollars")) {
                    replacement = replacement + " Dollars";
                    i++;
                    addToDic(replacement, NumericPatternTermsDic);
                    continue;
                }
                //price m dollars. price bn dollars
                else if (isNumber(term) && i < textArray.length - 2 && (cleanWord(textArray[i + 1]).toLowerCase().equals("m") ||
                        cleanWord(textArray[i + 1]).toLowerCase().equals("bn") && cleanWord(textArray[i + 2]).toLowerCase().equals("dollars"))) {
                    if (cleanWord(textArray[i + 1]).toLowerCase().equals("m")) {
                        replacement = term + " M Dollars";
                    } else if (cleanWord(textArray[i + 1]).toLowerCase().equals("bn")) {
                        replacement = term + "000 M Dollars";
                    }
                    removeFromArray(i,2);
                    i += 2;
                    addToDic(replacement, NumericPatternTermsDic);
                    continue;
                }
                //pricem dollars. pricebn dollars no spaces!
                else if (term.length() > 1&& replacement.length()>1 && i<textArray.length-1 &&((isNumber(replacement.substring(0,replacement.length()-1)) && replacement.contains("m")) || (term.length() > 2
                        && isNumber(replacement.substring(0,replacement.length()-2))
                        && replacement.contains("bn"))) && cleanWord(textArray[i + 1]).toLowerCase().equals("dollars")) {
                    {
                        if (replacement.substring(replacement.length()-1).toLowerCase().equals("m")) {
                            replacement = term.substring(0,term.length()-1) + " M Dollars";
                            removeFromArray(i,1);
                            i++;
                            addToDic(replacement, NumericPatternTermsDic);
                            continue;
                        }
                        else if (replacement.substring(replacement.length()-2).toLowerCase().equals("bn")) {
                            replacement = term.substring(0,term.length()-2) + "000 M Dollars";
                            removeFromArray(i,1);
                            i++;
                            addToDic(replacement, NumericPatternTermsDic);
                            continue;
                        }
                    }
                }
                //price(million) u.s dollars
                else if( replacement.length()>1 &&isNumber(replacement.substring(0,replacement.length()-1)) &&
                        i<textArray.length-3 &&textArray[i+2].toLowerCase().equals("u.s.")&&
                        cleanWord(textArray[i+3]).toLowerCase().equals("dollars")){
                    if( replacement.contains("B")) {
                        replacement = replacement.substring(0, replacement.length() - 1) + "000 M Dollars";
                        removeFromArray(i,2);
                        i+=2;
                        addToDic(replacement,NumericPatternTermsDic);
                        continue;
                    }
                    else if( replacement.contains("M")) {
                        replacement = replacement.substring(0, replacement.length() - 1) + " M Dollars";
                        removeFromArray(i,2);
                        i+=2;
                        addToDic(replacement,NumericPatternTermsDic);
                        continue;
                    }

                }
//                 //between range
                // כרגע לא מחליף את המספרים לתצוגה של M לשנות אם יהיה זמן

                if(i > 0 && i < textArray.length - 2 && cleanWord(textArray[i - 1]).toLowerCase().equals("between")) {
                    replacement = betweenRangFormat(isSlides, replacement, i);
                    //remove from array in the function
                    if( replacement.length()>0)
                        addToDic(replacement, NumericPatternTermsDic);
                }



                //between range for normal second number
//                 if (textArray[i + 1].toLowerCase().equals("and") && isNumber(cleanWord(textArray[i + 2]))) {
//                        replacement = replacement+ "-" + cleanWord(textArray[i+2]);
//                        removeFromArray(i, 2);
//                        i += 2;
//                        addToDic(replacement, NumericPatternTermsDic);
//                        continue;
//                    }  //between rage for big numbers
//                 //between 2 and 2 million
//
//                     else if (i < textArray.length - 3 && (textArray[i + 1].toLowerCase().equals("and") &&isNumber(textArray[i + 2]) &&
//                         bigNumbers.contains(cleanWord(textArray[i + 3]).toLowerCase()))) {
//
//                        String formatNext = getBigNumbersReplacement( textArray[i + 2]+cleanWord(textArray[i + 3]).toLowerCase());
//                        replacement = replacement+"-"+formatNext;
//                     removeFromArray(i, 2);
//                     i +=3;
//                     addToDic(replacement, NumericPatternTermsDic);
//                     continue;
//                    }
//                }

                //only for miliions numbers
                else{
                    if( isSlides ){
                        removeFromArray(i,1);
                        addToDic(replacement, NumericPatternTermsDic);
                        //  i++;
                    }
                    //CHECK IF ENTERS ONLY NUMBERS
                    else {
                        //to check char(0) number numeric, letter term
                        //maybe check beforeee! no need to comer here
                        removeFromArray(i,0);
                        addToDic(replacement, NumericPatternTermsDic);
                    }
                }
            }
        }
        return text;

    }

    private void addSeperateWordsToDic(String term) {
            String[] splits = term.split("[-\\/]");
            for( int i =0; i<splits.length;i++) {
                if (cleanWord(splits[i]) != "") {
                    addToDic(cleanWord(splits[i]), termsDictionary);
                }
            }
    }

    private boolean endsWithLetter(String entity) {
        return isLetter(""+entity.charAt(entity.length()-1));
    }

    private String removeAllCommas(String[] withDots) {
        String number = "";
        for( String str : withDots){
            number = number+str;
        }
        return number;
    }

    private boolean containsOnlyNumbers(String[] withDots) {
        for( String str : withDots){
            if(!isNumber(str)){
                return false;
            }
        }
        return true;
    }

    public boolean isFraction(String nextStr) {
        if(nextStr.length()>=3){
            String[] numbers = nextStr.split("/");
            if( numbers.length ==2 &&isOneDigit(numbers[0])&& isOneDigit(numbers[1])){
                return true;
            }
        }
        return false;
    }

    private boolean isOneDigit(String number) {
        if(number.length()!=1)
            return false;
        return (number.charAt(0)>='0' && number.charAt(0)<='9');
    }

    private String betweenRangFormat(boolean isSlides, String replacement, int i){
        //for normal first number
        if(!isSlides && i > 0 && i < textArray.length - 2 && cleanWord(textArray[i - 1]).toLowerCase().equals("between")){
            //if the second is normal or fration
            //between 2 and 4 | between 2 and 4.5| between 2 and 3/4
            if (isNumber(cleanWord(textArray[i + 2]))|| isFraction(cleanWord(textArray[i + 2]))){
                String numFormat=cleanWord(textArray[i + 2]);
                if( cleanWord(textArray[i + 2]).contains("."))
                    numFormat=floatingPointNumbar(numFormat,numFormat.indexOf("."));
                replacement = replacement+"-"+numFormat;
                removeFromArray(i,2);
                return replacement;
            }
        }
        //between 2 million and...4/4 million
        else if(isSlides && i<textArray.length-3 &&  textArray[i + 2].toLowerCase().equals("and") ){
            //the second is big number
            if( i<textArray.length-4 && isDigit(cleanWord(textArray[i + 3]))&&
                    (bigNumbers.contains(cleanWord(textArray[i +4]).toLowerCase())|| Double.parseDouble(cleanWord(textArray[i + 3]))>=1000)) {
                String formatNext = getBigNumbersReplacement( textArray[i + 3]+cleanWord(textArray[i + 4]).toLowerCase());
                replacement = replacement+"-"+formatNext;
            }
            else{
            }
//            if( i<)
//             else if (i < textArray.length - 3 && (textArray[i + 1].toLowerCase().equals("and") &&isNumber(textArray[i + 2]) &&
//                         bigNumbers.contains(cleanWord(textArray[i + 3]).toLowerCase()))) {
//
//                        String formatNext = getBigNumbersReplacement( textArray[i + 2]+cleanWord(textArray[i + 3]).toLowerCase());
//                        replacement = replacement+"-"+formatNext;
//                     removeFromArray(i, 2);
//                     i +=3;
//                     addToDic(replacement, NumericPatternTermsDic);
        }
        return "";
    }

    //check that input is legal
    public String floatingPointNumbar( String term, int indexOfDot){
        String foramtNumber = "";
        int numOfNumbersAfterDot= term.substring(indexOfDot+1).length();
        if( numOfNumbersAfterDot<=3){
            foramtNumber = term;
        }
        else
            foramtNumber = term.substring(0,indexOfDot+4);
        return foramtNumber;

    }

    private boolean startWithCapital(String word) {
        for( int i =0; i<word.length(); i++){
            if(!isLetter(""+word.charAt(i))){
                return false;
            }
        }
        if( word.length()>=1){
            return (word.charAt(0)>='A' && word.charAt(0)<='Z');
        }
        return false;
    }

    private void removeFromArray(int index, int offset) {
        for( int i =index; i<index+offset+1; i++){
            textArray[i] ="";
        }
    }

    public String cleanWord(String word) {
        //(adi
        if( word == ""){
            return "";
        }
        if( word.length()>0) {
            char x = word.charAt(0);
            //not a number or letter
            if (word == "" || ((word.length() <= 1&& !((x >= 'a' && x <= 'z') || (x >= 'A' && x <= 'Z') || (x >= '0' && x <= '9')))))
                return "";
            String cleanWord = word;
            int i = 0;
            x = word.charAt(i);
            while (i < cleanWord.length() && !((x >= 'a' && x <= 'z') || (x >= 'A' && x <= 'Z') || (x >= '0' && x <= '9') || (x == '$'))) {
                //LENFGT-1
                //i =0 clean =3(.), i=1 clean =2 .), i=2 clean =.
                cleanWord = cleanWord.substring(i+1);
                i++;
                if (i < cleanWord.length()-1 ) {
                    //   i++;
                    x = word.charAt(i);
                }

            }
            if (cleanWord.length() > 1) {
                i = cleanWord.length() - 1;
                x = cleanWord.charAt(i);
                while (i >= 0 && !((x >= 'a' && x <= 'z') || (x >= 'A' && x <= 'Z') || (x == '%') || (x >= '0' && x <= '9'))) {
                    cleanWord = cleanWord.substring(0, i);
                    i--;
                    if (i >= 0) {
                        //     i--;
                        x = cleanWord.charAt(i);// i=1, 7)
                    }

                }
            }

            return cleanWord;
        }
        return "";
    }
    //help function
    int parseNumber (String numberString){
        return Integer.parseInt(numberString.replace("k", "").replace(".", ""));
    }
    private String formatTwoDigitsNumber(int date){
        if (date < 10){
            return "0" +date;
        }
        return Integer.toString(date);
    }
    String[] _getMonths() {
        return new String[] {"january","jan","february","feb","march","mar","april","apr","may","may","june","jun","july",
                "jul","august","aug","september","sep","october","oct","november","nov","december","dec"};
    }
    private String containsMiliion(String s) {
        String toReturn = "";
        if( s.substring(0,7).equals("million"))
            toReturn ="million";
        else if( s.substring(0,7).equals("billion"))
            toReturn ="billion";
        else if( s.substring(0,8).equals("trillion"))
            toReturn ="trillion";
        else if( s.substring(0,8).equals("thousand"))
            toReturn ="thousand";
        return toReturn;
    }
    private String createSubstring(StringBuilder docText, int startIndex, int endIndex) {
        String textSubstring ="";
        if( startIndex-currOffset >= 0){
            if(endIndex-currOffset <docText.length()-21){
                textSubstring =docText.substring(startIndex-currOffset, endIndex-currOffset+21);
            }
            else{
                textSubstring = docText.substring(startIndex-currOffset);
            }
        }
        else{
            if( endIndex-currOffset <docText.length()-21)
                textSubstring =docText.substring(0, endIndex-currOffset+21);
            else{

            }
        }
        return textSubstring;
    }
    public StringBuilder _replaceBetweenIndexes(StringBuilder text, int start, int end, String replacement){
        if(start>=0 && start<docText.length() && end<docText.length() && end>start)
            return text.replace(start, end, replacement);
            //              return text.substring(0, start) + replacement + text.substring(end);
        else
            return docText;
    }
    public String getBigNumbersReplacement(String term){
        BigDecimal multiplier = new BigDecimal("1");
        if (term.contains("thousand")){
            multiplier = new BigDecimal("1000");
        } else if (term.contains("million")){
            multiplier = new BigDecimal("1000000");
        } else if (term.contains("billion") || term.endsWith("bn")){
            multiplier = new BigDecimal("1000000000");
        }
        else if (term.contains("trillion")){
            multiplier =new BigDecimal("1000000000000");
        }
        term = removeAllTerms(term, new String[] {"thousand", "million", "billion", "bn", "trillion"}).trim();
        BigDecimal number = new BigDecimal(term);
        number = number.multiply(multiplier);
        return _formatNumber(number);
    }
    public static String removeAllTerms(String text, String[] terms){
        for (String term : terms) {
            text = text.replace(term, "");
        }
        return text;
    }
    private String _formatNumber(BigDecimal number){
        // understand format- if less then tree
        DecimalFormat df = new DecimalFormat("#.###");
        if (number.compareTo(new BigDecimal("1000000000000")) >=0){// greater tnen trillion
            BigDecimal result = number.divide(new BigDecimal("1000000000"));
            result = result.setScale(3, RoundingMode.FLOOR);
            return df.format(result)+ 'B';
        }
        else if (number.compareTo(new BigDecimal("1000000000")) >=0  ){
            BigDecimal result = number.divide(new BigDecimal("1000000000"));
            result = result.setScale(3, RoundingMode.FLOOR);
            return df.format(result)+'B';
        }
        else if (number.compareTo(new BigDecimal("1000000")) >=0){
            BigDecimal result = number.divide(new BigDecimal("1000000"));
            result = result.setScale(3, RoundingMode.FLOOR);
            return df.format(result)+ 'M';
        }
        else if (number.compareTo(new BigDecimal("1000")) >=0){
            BigDecimal result = number.divide(new BigDecimal("1000"));
            /// see if need to add this scale on other places !!!!!!
            result = result.setScale(3, RoundingMode.FLOOR);
            return df.format(result)+ 'K';
        }
        else{
            return df.format(number);
        }
    }
    public Matcher _matchPattern(String text, String stringPattern){
        Pattern pattern = Pattern.compile(stringPattern);
        return pattern.matcher(text);
    }
    private String removeStopwords(String original) throws IOException {
        //  ArrayList<String> allWords = Stream.of(original.toLowerCase().split(" ")).collect(Collectors.toCollection(ArrayList<String>::new));
        String [] allWords = original.split(" ");
        String result="";
        for( String word: allWords){
            if( word!="" && isLetter(word)){
                //consider do only when word lower case
                if(!stopWords.contains(word)){
                    result=result+" "+word;
                }
            }
        }
        return result;
    }

    private boolean isLetter(String word) {
        for( int i=0; i<word.length(); i++){
            char c= word.charAt(i);
            if(!((c>='a'&& c<='z') || (c>='A'&& c<='Z'))){
                return false;
            }
        }
        return true;
    }
    private void addToDic(String replacement, HashMap<String, Term>  dic) {
        if( replacement == "")
            return;
        replacement= cleanWord(replacement.trim());
        if( replacement == "")
            return;
        if( replacement.length()>0 && !replacement.equals("")) {
            if (isStemming && isLetter(replacement))
                replacement = stemmTheText(replacement);

            //check if term exists
            if (!dic.containsKey(replacement.toLowerCase())) {
                dic.put(replacement.toLowerCase(), new Term(replacement, currDoc.getM_docNum()));
            } else
                dic.get(replacement.toLowerCase()).updateTerm(replacement, currDoc.getM_docNum());

            //add term to doc
            currDoc.addToTermDic(replacement.toLowerCase());
        }
    }
    private void addToEntitiesDic( String entity, String docID){
        //add apperance of entity to doc entities list
        if( entity =="")
            return;
        if( tmpEntitiesInDoc.containsKey(entity)){
            tmpEntitiesInDoc.replace(entity, tmpEntitiesInDoc.get(entity)+1);
        }
        else
            tmpEntitiesInDoc.put(entity, 1);
    }
    public String  stemmTheText(String termToStemm){
        Stemmer stemmer = new Stemmer();
        char[]charArray=termToStemm.toCharArray();
        stemmer.add(charArray,termToStemm.length());
        stemmer.stem();
        return  stemmer.toString();
    }
    public void createSetOfStopWords()throws IOException {
        stopWords = new HashSet<>();
        FileReader reader = new FileReader(new File(pathToCurpos+"\\05 stop_words.txt"));
        // FileReader reader = new FileReader(getClass().getClassLoader().getResource("stopWords.txt").getFile());
        BufferedReader buffer = new BufferedReader(reader);
        String stopWord = null;
        try{
            while ((stopWord=buffer.readLine())!=null){
                stopWords.add(stopWord);
            }
        }
        catch (IOException e){
            System.out.println("fuck of");
        }
        //      Collections.sort(stopWords);
        buffer.close();
        dictionaryMeneger.setStopWords(stopWords);
    }
    private boolean isNumber(String word){
        if( word.length() <1)
            return false;
        if(word.contains(".") && word.length()>=3){
            int index = word.indexOf('.' );
            if(index<word.length()-1&&isDigit(word.substring(0,index))&&isDigit(word.substring(index+1))){
                return true;
            }
            else
                return false;
        }
        else
            return isDigit(word);
    }
    private boolean isDigit(String word){
        if( word.length()<1)
            return false;
        for( int i =0; i<word.length(); i++){
            if(!( word.charAt(i)>='0' && word.charAt(i)<='9')){
                return false;
            }
        }
        return true;
    }
    public void sortTmpEntityDic(){
        List<Map.Entry<String, Integer> > list = new LinkedList<Map.Entry<String, Integer> >(tmpEntitiesInDoc.entrySet());
        //soet the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        //from grater to small
        Collections.reverse(list);
        tmpEntitiesInDoc = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            tmpEntitiesInDoc.put(aa.getKey(), aa.getValue());
        }
    }

    private String remove_s(String entity){
        if(entity.toLowerCase().endsWith("'s")) {
            int index = entity.indexOf("'");
            entity = entity.substring(0, index);
        }
            return entity;
    }



}







