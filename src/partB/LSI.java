package partB;

import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Word2VecModel;
import com.medallia.word2vec.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LSI {
    ArrayList<String> vocabillary;
    ArrayList<String > ori_terms;
    int  countResult;
    com.medallia.word2vec.Searcher semanticSearcher;
    public LSI(ArrayList<String > ori_terms) {
        try {
            this.ori_terms = ori_terms;
            countResult = 2;

            String modelFilePath = "word2vec.c.output.model.txt";
            Word2VecModel model = Word2VecModel.fromTextFile(new File(modelFilePath));

            semanticSearcher = model.forSearch();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public  ArrayList<String> word2vec(String term){
         vocabillary = new ArrayList<>();
        try {

            int countResult = 5;
            int couner = 0;
            List<Searcher.Match> matches = semanticSearcher.getMatches(term, countResult);
            for (com.medallia.word2vec.Searcher.Match match : matches) {
                if( couner ==2){
                    break;
                }
                if( !ori_terms.contains(match.match().toLowerCase())){
            //    if (!match.match().equals(term.toLowerCase())) {
                    vocabillary.add(match.match()); //the term
                    couner++;
                }
            }
        }
         catch (com.medallia.word2vec.Searcher.UnknownWordException e) {
            // unknown term
        }
        return vocabillary;
    }

}
