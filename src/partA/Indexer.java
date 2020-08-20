package partA;

import partA.PostingFile;

import java.io.*;
import java.util.*;
import java.util.List;


public class Indexer  {

    private HashMap<String, Term> currentTerms;
    private PostingFile postingsFilesManager;
    private int iterationNum;
    private String pathToPosting;
    private String mainFolderPath;
    private DictionaryMeneger dictionaryMeneger;
    private String message;

    public Indexer(String pathToPosting,boolean toStem,DictionaryMeneger dictionaryMeneger) {
        this.currentTerms = new HashMap<String, Term>();
        this.iterationNum = 1;
        this.mainFolderPath = PathToReadyFolder(pathToPosting, "Indexer",toStem);
        if(toStem==true){
            pathToPosting= pathToPosting + "\\" + "Stemmer-PostingFiles";
        }
        else {
            pathToPosting= pathToPosting + "\\" + "PostingFiles";
        }
        this.postingsFilesManager = new PostingFile(mainFolderPath, pathToPosting,dictionaryMeneger);
        this.pathToPosting = pathToPosting;
        this.dictionaryMeneger = dictionaryMeneger;
    }


    //could after chank of files that were readed
    public void initIndexer() {
        this.currentTerms = new HashMap<String, Term>();
        this.postingsFilesManager = new PostingFile(mainFolderPath, pathToPosting,dictionaryMeneger);
        this.iterationNum++;
    }

    public void startPosting() {
        postingsFilesManager.readFolders();
    }

    //write the temperery files for one eteration>>>>main function to add temp file
    void createTermFiles(HashMap<String, Term> termsDic, HashMap<String, Term> numericTermsDic,int numOfDocs) {
        writeTermDic(termsDic, mainFolderPath);
        writeNumericTermDic(numericTermsDic, mainFolderPath);
        int numOfTerms = termsDic.size()+numericTermsDic.size();
        getNumOfDocs(numOfDocs);
        getNumOfTerms(numOfTerms);
//        JFrame frame = new JFrame("iteration completed");
//        frame.setPreferredSize(new Dimension(700,500));
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//        message = "Num of docs parsed :"+numOfDocs+"    Num of term in chuck: "+numOfTerms;
//        JOptionPane.showMessageDialog(frame, message);
//        frame.setVisible(false);
        //System.out.println("finish indexing-" + java.time.LocalTime.now());
    }

    public int getNumOfDocs(int numOfDocs) {
        return numOfDocs;
    }
    public int getNumOfTerms(int numOfTerms) {
        return numOfTerms;
    }

    boolean isTheFirstCharABC(String firstChar) {
        char first = firstChar.charAt(0);
        return ((first >= 'A' && first <= 'Z') || (first >= 'a' && first <= 'z'));
    }

    /**
     * write the regular terms to temp file
     *
     * @param termsDic
     * @param Path
     */
    void writeTermDic(HashMap<String, Term> termsDic, String Path) {
        String mainPath = Path + "\\Letters";

        //for each letter, term list for writing for this iteration
        HashMap<String, ArrayList<Term>> finalTerms = new HashMap<>();
        for (String term : termsDic.keySet()) {
            //clean the term from "
            String firstChar = "";
            //chack input ftom term list
            if (term.length() == 0) {
                continue;
            }
            firstChar = term.substring(0, 1).toUpperCase();
            if (isTheFirstCharABC(firstChar)) {

            }
            if (finalTerms.get(firstChar) == null) {
                ArrayList<Term> list = new ArrayList<>();
                finalTerms.put(firstChar, list);
            }
            //add term to the list it the first char
            termsDic.get(term).setM_Term(term.toLowerCase());
            finalTerms.get(firstChar).add(termsDic.get(term));
        }

        for (String firstChar : finalTerms.keySet()) {
            ArrayList<Term> toWrite = sortByTerms(finalTerms.get(firstChar));
            String path = mainPath + "\\" + firstChar.toUpperCase() + "\\" + firstChar.toUpperCase() + iterationNum;
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(path), true));
                bufferedWriter.append(toWrite.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (Exception e) {
            }
        }
    }

    //sort the terms and return sorted list.
    ArrayList<Term> sortTerms(HashMap<String, Term> termsDic) {
        List<String> sortedTermsByKey = new ArrayList<>(termsDic.keySet());
        Collections.sort(sortedTermsByKey);
        ArrayList<Term> sortedTerms = new ArrayList<>();
        for (String key : sortedTermsByKey) {
            sortedTerms.add(termsDic.get(key));
        }
        return sortedTerms;
    }

    private ArrayList<Term> sortByTerms(ArrayList<Term> toSort) {
        Collections.sort(toSort, new Comparator<Term>() {
            @Override
            public int compare(Term o1, Term o2) {
                return o1.getM_Term().compareTo(o2.getM_Term());
            }
        });
        return toSort;
    }

    /**
     * write numeric terms to file
     *
     * @param numericTermsDic
     * @param mainPath
     */
    void writeNumericTermDic(HashMap<String, Term> numericTermsDic, String mainPath) {
        ArrayList<Term> numericTermsToWrite = sortTerms(numericTermsDic);
        String pathForNumbers = mainPath + "\\Numbers\\numbers" + iterationNum;
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(pathForNumbers), true));
            bufferedWriter.append(numericTermsToWrite.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
        }
    }

    public String PathToReadyFolder(String path, String mainFolderName,boolean stemmer) {
        String stem = "";
        if(stemmer==true){
            stem = "Stemmer-";
        }
        String cuttPath = path + "\\" + stem+mainFolderName;

        char[] letters = new char[26];
        char chatIntByAscci = 'A';
        for (int i = 0; i < letters.length; i++) {
            letters[i] = chatIntByAscci;
            chatIntByAscci++;
        }
        //create main folder for the 27 folders of letters and numbers

        //create folder Indexer
        File folder = new File(cuttPath);
        folder.mkdir();

        //create folder for ABC terms
        File mainFolder = new File(cuttPath + "\\Letters");
        mainFolder.mkdir();


        //create the numbers folder
        File numbersFolder = new File(cuttPath + "\\Numbers");
        numbersFolder.mkdir();


        //create file in the suitable folder
        for (char letter : letters) {
            String folderName = "" + letter;
            File letterFile = new File(mainFolder.getPath() + "\\" + folderName);
                letterFile.mkdir();

        }
        return cuttPath;
    }

    public PostingFile getPostingsFilesManager() {
        return postingsFilesManager;
    }
}