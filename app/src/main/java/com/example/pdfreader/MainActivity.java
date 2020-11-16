package com.example.pdfreader;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    int editTextOffsetDown;
    int editTextOffsetUp;
    int scrollbarOffsetDown;
    int outBoundScrollingCount = 0;
    PdfModel pdfModel;
    DocumentsManagerModel dmm;
    EditText t;
    LinearLayout docTabsLayout;
    String dmmPath;
    private static final int OPEN_REQUEST_CODE = 41;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        docTabsLayout = (LinearLayout) findViewById(R.id.tabsContainerLayout);
        docTabViewList = new ArrayList<DocumentTabView>();
        t = (EditText) findViewById(R.id.pageText);
        pdfModel = new PdfModel();
        dmmPath = getApplicationContext().getDataDir().getPath() + "/dmm.ser";
        File dmmFile = new File(dmmPath);

        if (!dmmFile.exists()) {
            dmm = DocumentsManagerModel.getSingleton();
        } else {
            if(loadDmm)
            loadDMM();
            else
                dmm = DocumentsManagerModel.getSingleton();

        }

        TextView t2 = (TextView) findViewById(R.id.testText2);
        t2.setText(getApplicationContext().getDataDir().getPath());

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //filter in pdf files
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        Button testBtn = (Button) findViewById(R.id.testBtn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //openFileDialog
                    startActivityForResult(intent, OPEN_REQUEST_CODE);

                } catch (Exception e) {
                    Log.e("open crash", e.getMessage());
                }
            }
        });


        t.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //when finger down we want to locate the touched letter.
                    try {
                        float x = event.getX() + v.getScaleX();
                        int y = (int) event.getY();
                        Layout layout = ((EditText) v).getLayout();
                        int line = layout.getLineForVertical(y);
                        int offset = layout.getOffsetForHorizontal(line, x);

                        String text = t.getText().toString();
                        if (offset >= text.length() || text.charAt(offset) == ' ') {
                            editTextOffsetDown = -1;

                        }
                        editTextOffsetDown = offset;
                    } catch (Exception e) {
                        Log.e("OnDownException", e.getMessage());
                    }

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //when finger up we want to locate the touched letter.
                    try {
                        float x = event.getX() + v.getScaleX();
                        int y = (int) event.getY();
                        Layout layout = ((EditText) v).getLayout();
                        int line = layout.getLineForVertical(y);
                        int offset = layout.getOffsetForHorizontal(line, x);

                        String text = t.getText().toString();
                        int length = text.length();
                        if (offset >= length || text.charAt(offset) == ' ' || editTextOffsetDown == -1) {
                            editTextOffsetUp = -1;
                            return true;
                        }


                        editTextOffsetUp = offset;
                        int selectStart, selectEnd;
                        //determine the selectStart and selectEnd according to swipe's direction.
                        if (editTextOffsetDown < editTextOffsetUp) {
                            selectStart = getSentenceHead(text, editTextOffsetDown);
                            selectEnd = getSentenceTail(text, editTextOffsetUp);
                        } else {
                            selectStart = getSentenceHead(text, editTextOffsetUp);
                            selectEnd = getSentenceTail(text, editTextOffsetDown);
                        }
                        //paint the selected sentence.
                        Spannable WordtoSpan = new SpannableString(text);
                        WordtoSpan.setSpan(new ForegroundColorSpan(Color.rgb(85,221,242)), selectStart, selectEnd + 1, Spannable.SPAN_COMPOSING);
                        t.setText(WordtoSpan);

                        String selectedText=text.substring(selectStart,selectEnd);
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(v.getContext().CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("selectedText", selectedText);
                        clipboard.setPrimaryClip(clip);
                    } catch (Exception e) {
                        Log.e("OnUpException", e.getMessage());
                    }

                }

                return true;
            }
        });
        ImageView scrollbar = (ImageView) findViewById(R.id.imageViewScrollbar);
        scrollbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    try {
                        //if (scrollbarOffsetDown == -1)
                        scrollbarOffsetDown = (int) event.getY();

                        // return true;
                    } catch (Exception e) {
                        Log.e("OnDownException", e.getMessage());
                    }

                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    try {
                        scrollbarOffsetDown = -1;
                       /* scrollbarOffsetUp=(int) event.getY();
                        int scrollOffset=scrollbarOffsetUp-scrollbarOffsetDown;

                        int verticalPos=scrollOffset+t.getVerticalScrollbarPosition();
                        if(t.canScrollVertically(verticalPos))
                            t.scrollBy(0,verticalPos);*/
                    } catch (Exception e) {
                        Log.e("OnUpException", e.getMessage());
                    }

                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int currFingerPos = (int) event.getY();
                    //divided by scalar to make smooth move[not too fast]
                    int scrollOffset = (currFingerPos - scrollbarOffsetDown) / 5;
                    if (scrollOffset == 0)
                        return true;
                    int verticalPos = scrollOffset + t.getVerticalScrollbarPosition();
                    if (t.canScrollVertically(verticalPos)) {
                        t.scrollBy(0, verticalPos);
                        outBoundScrollingCount = 0;
                    } else {
                        outBoundScrollingCount++;
                        if (outBoundScrollingCount == 3) {
                            outBoundScrollingCount = 0;
                            if (scrollOffset > 0) {
                                pdfModel.getNextPage();
                                t2.setText("go next");
                            } else {
                                pdfModel.getPreviousPage();
                                t2.setText("go previous");
                            }
                            if (pdfModel.getStatus() == PdfModel.Status.Ready) {
                                t.setText(pdfModel.getCurrPage());
                                if (scrollOffset > 0)
                                    t2.setText("get next");
                                else
                                    t2.setText("get previous");
                            }
                            t2.setText(pdfModel.getLastError());
                        } else {
                            String m = scrollOffset > 0 ? "down" : "up";
                            // t2.setText("cant go " + m);
                            t2.setText("cant go " + String.valueOf(scrollOffset));
                        }
                    }
                }

                return true;
            }
        });

        File docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File book = new File(docs, "books/test.pdf");
        int g = 0;
        if (true)
            return;

        pdfModel = new PdfModel(book.getPath());
        pdfModel.getNextPage();
        pdfModel.getNextPage();
        pdfModel.getNextPage();
        if (pdfModel.getStatus() == PdfModel.Status.Ready) {
            t.setText(pdfModel.getCurrPage());
        }

    }

    private int getSentenceHead(String txt, int i) {
        //returns the first letter's index of the word where 'i' is located.
        int head = i;
        while (head > 0) {
            char c = txt.charAt(head - 1);
            if (c == ' ' || c == '\n' || c == '\t' || c == '\r') {
                break;
            }
            head--;
        }
        return head;
    }

    private int getSentenceTail(String txt, int i) {
        //returns the last letter's index of the word where 'i' is located.
        int tail = i;
        int length = txt.length();
        while (tail < length - 1) {
            char c = txt.charAt(tail + 1);
            if (c == ' ' || c == '\n' || c == '\t' || c == '\r') {
                break;
            }
            tail++;
        }
        return tail;
    }

    ArrayList<DocumentTabView> docTabViewList;

    public void loadDocument(String docPath) {

        //File book = new File(docs, "books/test.pdf");
        //pdfModel = new PdfModel(docStream, docPath);
        DocumentTab docTab = dmm.getDocumentTab(docPath);
        // t.setText(String.valueOf(dmm.recentlyOpenDocs.size()));
        DocumentTabView docTabView = null;
        //check if the docTabView exists.
        for (int i = 0; i < docTabViewList.size(); i++) {
            if (docTabViewList.get(i).getDocumentTab().equals(docTab)) {
                docTabView = docTabViewList.get(i);
                break;
            }
        }
        if (docTabView == null) {
            if (docTabViewList.size() == dmm.maxOpenDoc) {
                removeDocumentTab(dmm.maxOpenDoc - 1);

            }

            docTabView = createDocumentTabView(docTab);
            docTabViewList.add(docTabView);
            docTabsLayout.addView(docTabView);

        }
        documentTabViewClicked(docTabView);
        saveDMM();
        if (true)
            return;


        pdfModel = new PdfModel(docPath);
        pdfModel.getNextPage();
        pdfModel.getNextPage();
        pdfModel.getNextPage();
        if (pdfModel.getStatus() == PdfModel.Status.Ready) {
            t.setText(pdfModel.getCurrPage());
        }
    }

    /*removes the item at docTabIndex and moves all the items at j to j-1
    where j>i.
    * */
    public void removeDocumentTab(int docTabIndexToRemove) {
        docTabsLayout.removeView(docTabViewList.get(docTabIndexToRemove));
        dmm.removeDocumentTab(docTabIndexToRemove);
        int lastDocTabIndex = docTabViewList.size() - 1;
        DocumentTabView docTabView;
        for (int i = docTabIndexToRemove; i < lastDocTabIndex; i++) {
            docTabView = docTabViewList.get(i + 1);
            docTabView.setIndex(i);
            docTabViewList.set(i, docTabView);
        }
        docTabViewList.remove(lastDocTabIndex);

        if (docTabIndexToRemove != pdfModel.getCurrDocTab().getTabIndex())
            return;
        pdfModel = new PdfModel();
        //no more available docTab.
        if (lastDocTabIndex == 0) {
            t.setText("");

        } else {
            int i = docTabIndexToRemove - 1;
            if (i < 0)
                i = 0;
            documentTabViewClicked(docTabViewList.get(i));
        }
    }

    @SuppressLint("RestrictedApi")
    public DocumentTabView createDocumentTabView(DocumentTab docTab) {
        DocumentTabView docTabView = new DocumentTabView(this);
        docTabView.setWidth(50);
        docTabView.setHeight(100);
        docTabView.setDocumentTab(docTab);
        docTabView.setAutoSizeTextTypeUniformWithConfiguration(1, 17, 1, TypedValue.COMPLEX_UNIT_DIP);
        docTabView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {

                        docTabView.setSwipeDown(event.getY());
                        docTabView.setSwipeRight(event.getX());
                        docTabView.setLastX(docTabView.getX());
                    } catch (Exception e) {
                        Log.e("setSwipeOffsetDown", e.getMessage());
                    }

                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {


                    float swipeUpOffset = (event.getY() - docTabView.getSwipeDown());
                    float swipeRightLeftOffset = ((event.getX()) - docTabView.getSwipeRight());

                    float epsilon = 1;
                    if ((swipeUpOffset > -epsilon && swipeUpOffset < epsilon) && (swipeRightLeftOffset > -epsilon && swipeRightLeftOffset < epsilon)) {
                        documentTabViewClicked(docTabView);
                        return false;
                    }
                    docTabView.setSwipeUpOffset(swipeUpOffset);
                    docTabView.setY(v.getY() + swipeUpOffset);
                    //if the tabView is the first and moves to right.
                    if (swipeRightLeftOffset > 0 && docTabView.getIndex() == 0) {
                        //prevent from tabView cross the  right 'border'.
                        if (docTabView.getX() + swipeRightLeftOffset > docTabView.getLastX()) {
                            return true;
                        }
                    }
                    //if the tabView is the last and moves to left.
                    if (swipeRightLeftOffset < 0 && docTabView.getIndex() == docTabViewList.size() - 1) {
                        //prevent from tabView cross the  left 'border'.
                        if (docTabView.getX() + swipeRightLeftOffset < docTabView.getLastX()) {
                            return true;
                        }
                    }
                    docTabView.setSwipeLeftRightOffset(swipeRightLeftOffset);
                    docTabView.setX(docTabView.getX() + swipeRightLeftOffset);
                    switchDocTabViews(docTabView);

                    /*if (swipeUpOffset < 0)
                        t.setText(docTabView.getDocumentTab().getDocName() + " up!");
                    else
                        t.setText(docTabView.getDocumentTab().getDocName() + " down!");*/
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    try {
                        docTabView.setSwipeDown(-1);
                        docTabView.setSwipeRight(-1);
                        int upOffset = (int) docTabView.getSwipeUpOffset();
                        upOffset = (int) (docTabsLayout.getY() - docTabView.getY());
                        docTabView.setX(docTabView.getLastX());
                        if (Math.abs(upOffset) > (docTabView.getHeight() / 2)) {
                            //docTabsLayout.removeView(docTabView);
                            removeDocumentTab(docTabView.getIndex());
                            saveDMM();
                        } else {
                            docTabView.setY(docTabsLayout.getY());
                        }


                    } catch (Exception e) {
                        Log.e("OnUpException", e.getMessage());
                    }

                }

                return true;
            }
        });
        return docTabView;
    }

    public void documentTabViewClicked(DocumentTabView docTabView) {
        DocumentTab lastSelectedDocTab = pdfModel.getCurrDocTab();
        if (lastSelectedDocTab != null && lastSelectedDocTab.getTabIndex() != docTabView.getIndex()) {
            DocumentTabView lastSelectedDocTabView = docTabViewList.get(lastSelectedDocTab.getTabIndex());
            lastSelectedDocTabView.setTextColor(Color.GRAY);
        }
        docTabView.setTextColor(Color.WHITE);
        pdfModel.loadDocumentTab(docTabView.getDocumentTab());
        dmm.setCurrDocumentTab(docTabView.getDocumentTab());
        if (pdfModel.getStatus() == PdfModel.Status.Ready)
            t.setText(pdfModel.getCurrPage());
        else
            t.setText(pdfModel.getLastError());

        saveDMM();
    }

    public void switchDocTabViews(DocumentTabView docTabView) {

        int leftOffset = (int) (docTabView.getX() - docTabView.getLastX());
        //if the tab had cross at least half of the tab width, perform  exchange.
        if (Math.abs(leftOffset) > docTabView.getWidth() / 2) {
            try {
                int j = 1;
                //if true:the docTabView is moves from left to right thus its neighbour is to its right side.
                //elements in the list 0 to n from right to left.
                if (leftOffset > 0)
                    j = -1;
                //exchange positions and indices
                int neighbourIndex = docTabView.getIndex() + j;
                DocumentTabView neighbour = docTabViewList.get(neighbourIndex);
                float temp = neighbour.getX();
                neighbour.setX(docTabView.getLastX());
                docTabView.setX(temp);
                docTabView.setLastX(temp);

                docTabViewList.set(neighbourIndex, docTabView);
                docTabViewList.set(docTabView.getIndex(), neighbour);
                neighbour.setIndex(docTabView.getIndex());
                docTabView.setIndex(neighbourIndex);

            } catch (Exception e) {
                Log.e("exchangeViewError", e.getMessage());
            }

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Uri currentUri = null;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_REQUEST_CODE) {
                if (resultData != null) {

                    String docPath = UriUtils.getPathFromUri(this, resultData.getData());
                    if (docPath == null) {
                        docPath = resultData.getData().getPath().substring(10);
                    }
                    loadDocument(docPath);

                }
            }
        }
    }

    public void saveDMM() {
        try {
            FileOutputStream fileOut = new FileOutputStream(dmmPath);
            ObjectOutputStream ois = new ObjectOutputStream(fileOut);
            ois.writeObject(dmm);
        } catch (IOException e) {
            t.setText("open dmm.ser: " + e.getMessage());
        }
    }
    boolean loadDmm=true;
    public void loadDMM() {
        try {
            FileInputStream fileIn = new FileInputStream(dmmPath);
            ObjectInputStream ois = new ObjectInputStream(fileIn);
            dmm = (DocumentsManagerModel) ois.readObject();
            t.setText(String.valueOf(dmm.recentlyOpenDocs.size()));
            if(dmm.recentlyOpenDocs.size()<1)
                return;
            //create docTabBView from loaded dmm
            for (int i = 0; i < dmm.recentlyOpenDocs.size(); i++) {
                DocumentTabView docTabView = createDocumentTabView(dmm.recentlyOpenDocs.get(i));
                docTabViewList.add(docTabView);
                docTabsLayout.addView(docTabView);
            }
            DocumentTabView docTabView=docTabViewList.get(dmm.getCurrDocumentTab().getTabIndex());
            documentTabViewClicked(docTabView);
        } catch (IOException | ClassNotFoundException e) {
            t.setText("open dmm.ser: " + e.getMessage());
        }
    }
}