package com.example.pdfreader;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.InputStream;

public class PdfModel {
    private String docName;
    private String docPath;
    private int pagesNum;
    private String currPage;
    private String previousPage;
    private String nextPage;
    private String lastError;
    private Status status;
    PdfReader pdfReader;
    private DocumentTab currDocTab;
    public PdfModel(){
        currDocTab=null;
        nextPage="";
        previousPage="";
        currPage="";
    }
    public void loadDocumentTab(DocumentTab docTab){
        currDocTab=docTab;
        this.docPath=docTab.getDocPath();
        //reading the doc.
        try{

            status=Status.Waiting;
            pdfReader = new PdfReader(docPath);
            pagesNum= pdfReader.getNumberOfPages();
            currPage=readPage(docTab.getCurrPageNum());
            if(pagesNum>docTab.getCurrPageNum())
                nextPage=readPage(docTab.getCurrPageNum()+1);
            if(1<docTab.getCurrPageNum())
                previousPage=readPage(docTab.getCurrPageNum()-1);
        }catch(Exception e){
            writeError(e.getMessage());
        }
    }


    public PdfModel(String docPath){
        this.docPath=docPath;
        //reading the doc.
        try{
            int start=docPath.lastIndexOf('\\');
             docName=docPath.substring(start+1,docPath.length()-4);
            status=Status.Waiting;
            pdfReader = new PdfReader(docPath);
            pagesNum= pdfReader.getNumberOfPages();
          currPage=readPage(1);
          if(pagesNum>1)
              nextPage=readPage(2);



        }catch(Exception e){
           writeError(e.getMessage());
        }
    }
    public PdfModel(InputStream docStream,String path){
       // this.docPath=docPath;
        //reading the doc.
        try{
            status=Status.Waiting;
            pdfReader = new PdfReader(docStream);
            pagesNum= pdfReader.getNumberOfPages();
            currPage=readPage(1);
            if(pagesNum>1)
                nextPage=readPage(2);
        }catch(Exception e){
            writeError(e.getMessage());
        }
    }
    public String readPage(int pageNum){
        try{
            status=Status.Waiting;
            String page = "";
            page = PdfTextExtractor.getTextFromPage(pdfReader, pageNum).trim();
            status=Status.Ready;
            return page;
        }catch(Exception e){
            writeError(e.getMessage());
            return null;
        }
    }
    public String getNextPage(){
        if(nextPage!=null){
            previousPage=currPage;
            currPage=nextPage;
            currDocTab.setCurrPageNum(currDocTab.getCurrPageNum()+1);
            if(currDocTab.getCurrPageNum()+1<=pagesNum)
                nextPage=readPage(currDocTab.getCurrPageNum()+1);
            else
                nextPage=null;
            return currPage;
        }else{
            writeError("Cant get next page");
            return null;
        }
    }
    public String getPreviousPage(){
        if(previousPage!=null){
            nextPage=currPage;
            currPage=previousPage;
            currDocTab.setCurrPageNum(currDocTab.getCurrPageNum()-1);
            if(currDocTab.getCurrPageNum()-1>=1)
                previousPage=readPage(currDocTab.getCurrPageNum()-1);
            else
                previousPage=null;
            return currPage;
        }else{
            writeError("Cant get previous page");
            return null;
        }
    }

    public String getCurrPage(){
        return currPage;
    }
    public Status getStatus(){
        return status;
    }
    public String getLastError(){
        return lastError;
    }
    public void close(){
        try{
            pdfReader.close();
        }catch(Exception e){
            writeError(e.getMessage());
        }

    }
    public DocumentTab getCurrDocTab(){
        return currDocTab;
    }
    public void writeError(String messageError){
        lastError=messageError;
        status=Status.Error;
    }
    public enum Status{
        Ready,Waiting,Error
    }
}
