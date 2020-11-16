package com.example.pdfreader;

import com.itextpdf.text.pdf.PdfReader;

import java.util.ArrayList;

public class DocumentsManagerModel implements java.io.Serializable {
    public  int maxOpenDoc = 3;
     ArrayList<DocumentTab> recentlyOpenDocs;
    private DocumentTab currDocTab;
    static DocumentsManagerModel documentsManagerModel = null;

    private DocumentsManagerModel() {
        recentlyOpenDocs=new ArrayList<>();
    }

    public static DocumentsManagerModel getSingleton() {
        if (documentsManagerModel == null)
            documentsManagerModel = new DocumentsManagerModel();
        return documentsManagerModel;
    }

    public  DocumentTab getDocumentTab(String docPath) {
        //reading the doc.
        DocumentTab docTab;
        int docTabIndex=-1;
        try {

            int start = docPath.lastIndexOf("/");
            String docName = docPath.substring(start + 1, docPath.length() - 4);
            //check if the documentTabExist
            docTabIndex = isDocumentTabExist(docPath);
            if (docTabIndex == -1) {//create new docTab
                if (recentlyOpenDocs.size() == maxOpenDoc)
                    removeDocumentTab(0);
                docTabIndex = recentlyOpenDocs.size();
                docTab = new DocumentTab(docTabIndex, docPath, docName, 1, 0);
                recentlyOpenDocs.add(docTab);
            }

        } catch (Exception e) {
            // writeError(e.getMessage());
        }
        return recentlyOpenDocs.get(docTabIndex);
    }

    public  int isDocumentTabExist(String docPath) {
        int size = recentlyOpenDocs.size();
        for (int i = 0; i < size; i++) {
            if (docPath.equalsIgnoreCase(recentlyOpenDocs.get(i).getDocPath()))
                return i;
        }
        return -1;
    }

    public void removeDocumentTab(int docTabIndexToRemove) {
        int lastDocTabIndex = recentlyOpenDocs.size() - 1;
        for (int i = docTabIndexToRemove; i < lastDocTabIndex; i++) {
            recentlyOpenDocs.set(i, recentlyOpenDocs.get(i + 1));
        }
        recentlyOpenDocs.remove(lastDocTabIndex);
    }
    public void setCurrDocumentTab(DocumentTab currDocTab){
        this.currDocTab=currDocTab;
    }
    public DocumentTab getCurrDocumentTab(){
        return currDocTab;
    }
}
