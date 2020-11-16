package com.example.pdfreader;

import java.util.Objects;

public class DocumentTab implements java.io.Serializable {
    private int tabIndex;
    private String docPath;
    private String docName;
    private int currPageNum;
    private int pageOffset;
    private String currPage;

    public DocumentTab(int tabIndex, String docPath, String docName, int currPageNum, int pageOffset) {
        this.tabIndex = tabIndex;
        this.docPath = docPath;
        this.docName = docName;
        this.currPageNum = currPageNum;
        this.pageOffset = pageOffset;
    }

    public String getDocName() {
        return docName;
    }

    public String getDocPath() {
        return docPath;
    }

    public void setCurrPage(String currPage) {
        this.currPage = currPage;
    }

    public String getCurrPageIndex() {
        return currPage;
    }
    public void setCurrPageNum(int currPageNum) {
        this.currPageNum = currPageNum;
    }

    public int getCurrPageNum() {
        return currPageNum;
    }
    public int getTabIndex() {
        return tabIndex;
    }

    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentTab that = (DocumentTab) o;
        return Objects.equals(docPath, that.docPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docPath);
    }
}
