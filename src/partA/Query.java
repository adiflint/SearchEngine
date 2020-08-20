package partA;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Query extends Doc {

    private String query;
    private String num;
    private String desc;



    public Query(String num , String titel , String desc ) {
        super();
        if(num!=null){
            this.num = num;
        }
        if(titel!=null){
            this.query = titel+" "+desc;
        }

        if(desc!=null){
            this.desc = desc;

        }

        if(this.num!=null && this.query!=null){
            setValuesFromQuery(this.num,query);
        }
    }

    public Query(String titel){
        this.query = titel;
        this.num="1";
        desc="";
        setValuesFromQuery(num,query);
    }


    private void readInfoToQuery(Element element){
        element.toString();
    }






    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


}
