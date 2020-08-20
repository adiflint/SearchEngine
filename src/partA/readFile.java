package partA;
import runner.*;
import java.awt.*;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.File;
import java.io.IOException;
import org.jsoup.nodes.Element;
import partB.Ranker;

import javax.swing.*;
//import javax.swing.text.html.parser.Parser;

public class readFile {

    private String pathToCorpuse ;
    private String pathToPosting ;
    private boolean toStemming;
    private ArrayList<Doc> allDocs;
    private Parser parse;
    private long startTime;
    private long endTime;
    private String messege;


    public readFile(String pathCorpuse,String pathPosting, boolean toStem) throws IOException{
        startTime = System.currentTimeMillis();
        this.pathToCorpuse = pathCorpuse;
        this.pathToPosting = pathPosting;
        this.toStemming = toStem;
        allDocs = new ArrayList<>();
        parse = new Parser(pathCorpuse,pathPosting,toStemming);
        messege = "";
        listFilesForFolder(new File(pathCorpuse));

    }

    /**
     * reads 10 files, separate them to docs and send them to the parser
     * @param folder
     * @throws IOException
     */
    public void listFilesForFolder(final File folder) throws IOException {
        int count = 20;
        int countFolders= folder.listFiles().length;
        for (File fileEntry : folder.listFiles()) {
            File currFile = null;
            if(fileEntry.getPath().contains("05 stop_words")){
                countFolders--;
                continue;
            }
            currFile = fileEntry.listFiles()[0];
            //split the file into documents
            //System.out.println(fileEntry.getName());
            fromFileToDocs(currFile, fileEntry.getPath());
            count--;
            countFolders--;
            if( countFolders==0||count<=0){
                //to deleteeeee
                //System.out.println("finished 10 chuncks//n"+System.currentTimeMillis());
                parse.startParsing(allDocs);
                count = 20;
                allDocs = new ArrayList<>();
            }

        }
        parse.setToDictionaryMeneger();
        //getter to dictionary
        parse.getIndexer().startPosting();
        //here print
        ///TODO change hereeeeeee 9.1
        Ranker ranker = new Ranker(parse.getDictionaryMeneger(), toStemming);
        ranker.finalRank();



        endTime  = System.currentTimeMillis();
        this.messege= parse.getIndexer().getPostingsFilesManager().getDictionaryMeneger().getMessege();
        this.messege = messege+"\n"+"total time in seconds: "+(endTime-startTime)/1000;

        JFrame frame = new JFrame("iteration completed");
        frame.setPreferredSize(new Dimension(700,500));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        JOptionPane.showMessageDialog(frame, this.messege);
        frame.setVisible(false);
        System.out.println("finish indexing. total time in s: "+((endTime-startTime)/1000));
    }

    /**
     * create a doc from text file
     * @param file
     * @param path
     */
    public void fromFileToDocs(File file,String path){
        try {
            Document xmlFile = Jsoup.parse(file, "UTF-8");
            org.jsoup.select.Elements ListOfDocs = (org.jsoup.select.Elements)( xmlFile.getElementsByTag("DOC"));
            for(Element element : ListOfDocs){
                Doc newDoc = new Doc(element,path);
                allDocs.add(newDoc);
            }
        }
        catch (
                IOException e) {
        }
    }

    public Parser getParse() {
        return parse;
    }
}



