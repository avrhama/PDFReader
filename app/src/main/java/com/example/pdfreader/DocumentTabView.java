package com.example.pdfreader;

import android.content.Context;

public class DocumentTabView extends androidx.appcompat.widget.AppCompatButton {
    private DocumentTab docTab;
    private float swipeDown;
    private float swipeRight;
    private float swipeUpOffset;
    private float swipeLeftRightOffset;
    private float lastX;

    public DocumentTabView(Context context) {
        super(context);
    }

    public void setDocumentTab(DocumentTab docTab) {
        this.docTab = docTab;
        this.setText(docTab.getDocName());
    }

    public DocumentTab getDocumentTab() {
        return docTab;
    }

    public float getSwipeDown() {
        return swipeDown;
    }

    public void setSwipeDown(float swipeDown) {
        this.swipeDown = swipeDown;
    }

    public float getSwipeUpOffset() {
        return swipeUpOffset;
    }

    public void setSwipeUpOffset(float swipeUpOffset) {
        this.swipeUpOffset = swipeUpOffset;
    }

    public float getSwipeRight() {
        return swipeRight;
    }

    public void setSwipeRight(float swipeRight) {
        this.swipeRight = swipeRight;
    }

    public float getSwipeLeftRightOffset() {
        return swipeLeftRightOffset;
    }

    public void setSwipeLeftRightOffset(float swipeLeftRightOffset) {
        this.swipeLeftRightOffset = swipeLeftRightOffset;
    }

    public float getLastX() {
        return lastX;
    }

    public void setLastX(float lastX) {
        this.lastX = lastX;
    }

    public int getIndex() {
        return docTab.getTabIndex();
    }

    public void setIndex(int index) {
        docTab.setTabIndex(index);
    }
}
