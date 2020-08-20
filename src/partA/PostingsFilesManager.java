package partA;

import java.io.*;
import java.util.*;


class PostingFile {
    // This class represents a single posting file containing terms
    private String filepathToRead;
    private String filepathToWrite;
    private HashMap<String, String[]> TermDitionary;
    private HashMap<String, String> postingFilesMapping;
    private DictionaryMeneger dictionaryMeneger;
    private HashMap<String, String> listOfDocByDocID;
    private HashMap<String,Integer> postingToCounter;

    /**
     * constractor , create postingFile map and a directory for the posting files
     *
     * @param filepathToRead
     * @param filePathToWrite
     */
    PostingFile(String filepathToRead, String filePathToWrite, DictionaryMeneger dictionaryMeneger) {
        this.filepathToRead = filepathToRead;
        this.filepathToWrite = filePathToWrite;
        this.TermDitionary = new HashMap<>();
        this.postingFilesMapping = new HashMap<>();
        this.dictionaryMeneger = dictionaryMeneger;
        this.listOfDocByDocID = new HashMap<>();
        this.postingToCounter = new HashMap<>();
        createPostingFileMap();
        createMainFolderToWritePostingFiles();
        creatPostingToCounter();

    }

    /**
     * create the posting file directory
     */
    private void createMainFolderToWritePostingFiles() {
        File letterFile = new File(filepathToWrite);
        if (!letterFile.exists()) {
            letterFile.mkdir();
        }
    }

    public DictionaryMeneger getDictionaryMeneger() {
        return dictionaryMeneger;
    }

    /**
     * create the posting file map of path, each path for letter/lettrers
     */
    void createPostingFileMap() {
        postingFilesMapping.put("A", "posting_A");

        postingFilesMapping.put("B", "posting_B");
        postingFilesMapping.put("C", "posting_C");

        postingFilesMapping.put("D", "posting_D");

        postingFilesMapping.put("E", "posting_E");
        postingFilesMapping.put("F", "posting_F");

        postingFilesMapping.put("G", "posting_G");
        postingFilesMapping.put("H", "posting_H");
        postingFilesMapping.put("I", "posting_I");

        postingFilesMapping.put("J", "posting_J");
        postingFilesMapping.put("K", "posting_K");
        postingFilesMapping.put("L", "posting_L");

        postingFilesMapping.put("M", "posting_M");
        postingFilesMapping.put("N", "posting_N");

        postingFilesMapping.put("O", "posting_O");
        postingFilesMapping.put("P", "posting_P");

        postingFilesMapping.put("Q", "posting_Q");
        postingFilesMapping.put("R", "posting_R");

        postingFilesMapping.put("S", "posting_S");

        postingFilesMapping.put("T", "posting_T");

        postingFilesMapping.put("U", "posting_U");
        postingFilesMapping.put("V", "posting_V");
        postingFilesMapping.put("W", "posting_W");
        postingFilesMapping.put("X", "posting_X");
        postingFilesMapping.put("Y", "posting_Y");
        postingFilesMapping.put("Z", "posting_Z");

    }

    public HashMap<String, String> getPostingFilesMapping() {
        return postingFilesMapping;
    }

    public void readFolders() {
        ArrayList<Term> terms = new ArrayList<>();
        File mainFolder = new File(filepathToRead);

        for (File folderOfIndextFiles : mainFolder.listFiles()) {
            if (folderOfIndextFiles.getPath().contains("Letters")) {
                //ABC
                for (File folder : folderOfIndextFiles.listFiles()) {
                    if (folder.listFiles() == null || folder.listFiles().length == 0) {
                        folder.delete();
                        continue;
                    }
                    if (!(folder.getPath().length() > 0)) {
                        continue;
                    }
                    String letter = "" + folder.getPath().charAt(folder.getPath().length() - 1);
                    ArrayList<Term> toPosting = mergeToPostFiles(folder);
                    if (toPosting == null) {
                        folder.delete();
                        continue;
                    }
                    //addToTermDic(toPosting);
                    // todo: change to eith
                    WriteToPostingFile(termsWithOutNames(toPosting), postingFilesMapping.get(letter), filepathToWrite);
                    folder.delete();
                }
                folderOfIndextFiles.delete();
            } else if (folderOfIndextFiles.getPath().contains("Numbers")) {

                ArrayList<Term> numericTermToPosting = mergeToPostFiles(folderOfIndextFiles);
                if (numericTermToPosting != null) {
                    //addToTermDic(numericTermToPosting);
                    //      System .out.println("The number of numeric Terms :" + numericTermToPosting.size());
                    WriteToPostingFile(termsWithOutNames(numericTermToPosting), "numericPosting1", filepathToWrite);
                }
                //here init count lines
                folderOfIndextFiles.delete();
            }
        }
        mainFolder.delete();
        dictionaryMeneger.write();
    }

    public ArrayList<Term> ReadAllTermInFile(File file, boolean toDeleteFile) {
        String termsInString = "";
        ArrayList<Term> termList = new ArrayList<>();
        String currLine = "";
        if (file == null) {
            return null;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(file.getPath())));
            StringBuilder sb = new StringBuilder();
            while ((currLine = br.readLine()) != null) {
                sb.append(currLine + "\n");
            }
            if (sb != null) {
                sb.deleteCharAt(0);
                currLine = sb.toString();
                if (!(currLine.length() > 0)) {
                    return null;
                }
                currLine.substring(0, currLine.length() - 1);
                br.close();
            } else {
                return null;
            }
            currLine = sb.toString();
            currLine.substring(0, currLine.length() - 1);
            br.close();
        } catch (Exception e) {
        }
        if (toDeleteFile) {
            file.delete();
        }
        return createTermsFromFile(currLine);
    }

    /**
     * write a term to the file
     *
     * @param fileToWrite
     * @param nameForPostingFile
     */
    private void WriteToPostingFile(String fileToWrite, String nameForPostingFile, String pathToWrite) {
        String path = pathToWrite + "\\" + nameForPostingFile;
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(path), false));
            bufferedWriter.write(fileToWrite);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
        }
    }

    /**
     * from string to list of terms
     *
     * @param lines
     * @return
     */
    ArrayList<Term> createTermsFromFile(String lines) {
        if (lines != null) {
            ArrayList<Term> arrayTerm = new ArrayList<>();
            String[] terms = lines.split(",");
            for (String termToDES : terms) {
                Term term = TempDeserialize(termToDES);
                arrayTerm.add(term);
            }
            return arrayTerm;
        }
        return null;
    }

    public Term TempDeserialize(String serializedObj) {
        //boolean isCapital = false;
        String[] splitted = serializedObj.split("[\\:\\n]");
        if (splitted.length <= 3 || splitted[0] == null || splitted[1] == null || splitted[2] == null || splitted[3] == null) {
            return null;
        }
        String term = splitted[0];
        String bool = splitted[1];
        int totalFres = Integer.parseInt(splitted[2]);
        String[] splittedDocFreqs = splitted[3].split("[|]");
        HashMap<String, Integer> docFreqs = new HashMap<>();
        for (int i = 0; i < splittedDocFreqs.length - 1; i = i + 2) {
            if (splittedDocFreqs[i] == null || splittedDocFreqs[i + 1] == null) {
                continue;
            }
            String docID = splittedDocFreqs[i];
            int frecTermInDoc = Integer.parseInt(splittedDocFreqs[i + 1]);
            docFreqs.put(docID, frecTermInDoc);
        }
        return new Term(term, totalFres, bool, docFreqs);
    }


    /**
     * read files and creat posting files for ABC
     *
     * @param foldersName
     */
    private ArrayList<Term> mergeToPostFiles(File foldersName) {
        ArrayList<Term> mergedList = new ArrayList<>();
        int countPostings = 1;
        while (foldersName.listFiles().length > 2) {
            File[] listOfFiles = foldersName.listFiles();
            for (int i = 0; i < listOfFiles.length - 1; i = i + 2) {
                ArrayList<Term> t1 = ReadAllTermInFile(listOfFiles[i], true);
                ArrayList<Term> t2 = ReadAllTermInFile(listOfFiles[i + 1], true);
                mergedList = merge(t1, t2, false);
                if (mergedList == null) {
                    continue;
                }
                WriteToPostingFile(mergedList.toString(), "merge" + countPostings, foldersName.getPath());
                countPostings++;
            }
        }
        //to remining files to marge
            if (foldersName.listFiles().length <= 2 && foldersName.listFiles().length > 0) {
            File[] listOfFiles = foldersName.listFiles();
            mergedList = new ArrayList<>();

            //one remining file to posting
            if (foldersName.listFiles().length == 1) {
                ArrayList<Term> t1 = ReadAllTermInFile(listOfFiles[0], true);
                mergedList = merge(t1, null, true);

                //2 remining file to posting
            } else if (foldersName.listFiles().length == 2) {
                ArrayList<Term> t1 = ReadAllTermInFile(listOfFiles[0], true);
                ArrayList<Term> t2 = ReadAllTermInFile(listOfFiles[1], true);
                mergedList = merge(t1, t2, true);
            }
            if (mergedList != null) {
                WriteToPostingFile(mergedList.toString(), "merge" + countPostings, foldersName.getPath());
                countPostings++;
            }
        }
        //no remining file to posting

        if (foldersName.listFiles().length == 0) {
            return null;
        }
        return ReadAllTermInFile(foldersName.listFiles()[0], true);
    }

    public HashMap<String, String> getListOfDocByDocID() {
        return listOfDocByDocID;
    }

    public void setListOfDocByDocID(HashMap<String, String> listOfDocByDocID) {
        this.listOfDocByDocID = listOfDocByDocID;
    }

//    private String termsWithOutNames(ArrayList<Term> terms){
//        StringBuilder allTerms = new StringBuilder();
//        for (Term term : terms){
//            if(term!=null){
//                String[] splitedTerm = term.toString().split("\n");
//                if(splitedTerm.length>1){
//                    allTerms.append(splitedTerm[1]+"\n");
//                }
//            }
//
//        }
//        return allTerms.toString();
//    }

    private ArrayList<Term> merge(ArrayList<Term> t1, ArrayList<Term> t2, boolean lastTo) {
        ArrayList<Term> afterMerge = new ArrayList<>();

        //if one of the lists is null
        if (t1 == null || t2 == null || t1.size() == 0 || t2.size() == 0) {
            if (t1 != null && t1.size() > 0) {
                if (lastTo) {
                    for (int i = 0; i < t1.size(); i++) {
                        Term term1 = t1.get(i);
                        if (term1 != null) {
                            afterMerge=addToDictionaries(term1,afterMerge);

                        }
                    }
                }
                return afterMerge;
            }
            else if (t2 != null && t2.size() > 0) {
                if (lastTo) {
                    for (int i = 0; i < t2.size(); i++) {
                        Term term2 = t2.get(i);
                        if (term2 != null) {
                            afterMerge=addToDictionaries(term2,afterMerge);


                        }
                    }
                }
                return afterMerge;
            }
            else {
                return null;
            }
        }

        Term term1 = null;
        Term term2 = null;

        //both lists are bigger than 1
        //else {
        term1 = t1.remove(0);
        term2 = t2.remove(0);
        while (t1.size() > 0 && t2.size() > 0 && term1 != null && term2 != null) {
            //term1<term2
            if (term1.getM_Term().trim().compareTo(term2.getM_Term().trim()) < 0) {
                if (lastTo) {
                    afterMerge=addToDictionaries(term1,afterMerge);

                }
                else  {
                    afterMerge.add(term1);
                }
                term1 = t1.remove(0);
            }
            //if there are equals terms
            else if (term1.getM_Term().trim().compareTo(term2.getM_Term().trim()) == 0) {
                term1.merge(term2);
                if (lastTo) {
                    afterMerge=addToDictionaries(term1,afterMerge);

                }
                else  {
                    afterMerge.add(term1);
                }
                term1 = t1.remove(0);
                term2 = t2.remove(0);
            }
            //term2<term1
            else {
                if (lastTo) {
                    afterMerge=addToDictionaries(term2,afterMerge);

                } else  {
                    afterMerge.add(term2);
                }
                term2 = t2.remove(0);
            }
            //}
        }

        if (t1.size() == 0 && t2.size() > 0) {
            for (int i = 0; i < t2.size(); i++) {
                if (t2.get(i) == null) {
                    continue;
                }
                if (lastTo) {
                    afterMerge=addToDictionaries(t2.get(i),afterMerge);

                }
                else  {
                    afterMerge.add(t2.get(i));
                }
            }
        }
        else if (t2.size() == 0 && t1.size() > 0) {
            for (int i = 0; i < t1.size(); i++) {
                if (t1.get(i) == null) {
                    continue;
                }
                //System.out.println(i);
                if (lastTo) {
                    afterMerge= addToDictionaries(t1.get(i),afterMerge);
                }
                else {
                    afterMerge.add(t1.get(i));
                }
            }
        }
        return afterMerge;
    }


    public void clear() {
        dictionaryMeneger.clear(dictionaryMeneger.getPath());
        File folder = new File(filepathToWrite);
        for (File file : folder.listFiles()) {
            file.delete();
        }
        folder.delete();
    }

    /**
     * boolean isCapital
     *
     * @param terms
     * @return
     */
    private String termsWithOutNames(ArrayList<Term> terms) {
        StringBuilder allTerms = new StringBuilder();
        for (Term term : terms) {
            if (term != null) {
                String[] splitedTerm = term.toString().split("\n");
                if (splitedTerm.length > 1) {
                    String[] termInfo = splitedTerm[1].split("[:]");
                    if (termInfo.length >= 3) {
                        allTerms.append(termInfo[0] + ":");
                        allTerms.append(termInfo[1] + ":");
                        String[] termDocInfo = termInfo[2].split("[|]");
                        for (int i = 0; i < termDocInfo.length - 1; i = i + 2) {
                            allTerms.append(listOfDocByDocID.get(termDocInfo[i]) + "|");
                            allTerms.append(termDocInfo[i + 1] + "|");
                        }
                    }
                    allTerms.append("\n");
                }
            }
        }
        return allTerms.toString();
    }


    //new
    private ArrayList<Term> addToDictionaries(Term term,ArrayList<Term> list){
        if( term.getM_Term().length()<1){
            return list;
        }
        String letter = (term.m_Term.trim()).substring(0,1);
        String postingFileName = postingFilesMapping.get(letter.toUpperCase());
        if(postingFileName==null){
            postingFileName="numericPosting1";
        }
        int counter = postingToCounter.get(postingFileName);

        if(term.getM_docsDictionary().size()>1 && !list.contains(term)){
            counter++;
            dictionaryMeneger.addTermToTermDic(term,counter);
            postingToCounter.replace(postingFileName,counter);
            list.add(term);
        }
        return list;
    }




    void creatPostingToCounter(){
        HashMap<String,Integer> map = new HashMap<>();
        for (String postingFileName : postingFilesMapping.keySet()){

            map.put(postingFilesMapping.get(postingFileName),0);
        }
        map.put("numericPosting1",0);
        this.postingToCounter = map;
    }



    //for checking
    private String termsWithNames(ArrayList<Term> terms) {
        StringBuilder allTerms = new StringBuilder();
        for (Term term : terms) {
            if (term != null) {
                String[] splitedTerm = term.toString().split("\n");
                if (splitedTerm.length > 1) {
                    String[] termInfo = splitedTerm[1].split("[:]");
                    if (termInfo.length >= 3) {
                        allTerms.append(splitedTerm[0] + "-");
                        allTerms.append(termInfo[0] + ":");
                        allTerms.append(termInfo[1] + ":");
                        String[] termDocInfo = termInfo[2].split("[|]");
                        for (int i = 0; i < termDocInfo.length - 1; i = i + 2) {
                            allTerms.append(listOfDocByDocID.get(termDocInfo[i]) + "|");
                            allTerms.append(termDocInfo[i + 1] + "|");
                        }
                    }
                    allTerms.append("\n");
                }
            }
        }
        return allTerms.toString();
    }

    public String getFilepathToRead() {
        return filepathToRead;
    }

    public void setFilepathToRead(String filepathToRead) {
        this.filepathToRead = filepathToRead;
    }


}