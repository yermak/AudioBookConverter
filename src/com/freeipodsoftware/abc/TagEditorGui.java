package com.freeipodsoftware.abc;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class TagEditorGui extends Composite {
    private static final String[] genres = new String[]{"Alternative", "Blues", "Books & Spoken", "Children's Music", "Classical", "Comedy", "Country", "Dance", "Easy Listening", "Electronic", "Folk", "Funk", "House", "Hip Hop", "Indie", "Industrial", "Jazz", "Metal", "Misc", "New Age", "Poscast", "Pop", "Punk", "Religious", "Rock", "Reggae", "Soft Rock", "Soundtrack", "Techno", "Trance", "Unclassifiable", "World"};
    private Composite tagsComposite = null;
    private Label label = null;
    protected Text artistText = null;
    private Label label1 = null;
    protected Text writerText = null;
    private Label label2 = null;
    protected Text titleText = null;
    private Label label3 = null;
    protected Text albumText = null;
    private Label label4 = null;
    protected Combo genreCombo = null;
    private Label label5 = null;
    protected Text yearText = null;
    private Label label6 = null;
    protected Text trackText = null;
    private Label label7 = null;
    protected Text discText = null;
    private Label label8 = null;
    protected Text commentText = null;
    private Label label9 = null;
    private Label label10 = null;

    public TagEditorGui(Composite parent, int style) {
        super(parent, style);
        this.initialize();
    }

    private void initialize() {
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 1;
        gridLayout1.marginWidth = 0;
        gridLayout1.marginHeight = 0;
        this.createTagsComposite();
        this.setLayout(gridLayout1);
        this.setSize(new Point(490, 145));
    }

    private void createTagsComposite() {
        GridData gridData22 = new GridData();
        gridData22.horizontalAlignment = 4;
        GridData gridData15 = new GridData();
        gridData15.grabExcessHorizontalSpace = true;
        gridData15.horizontalAlignment = 4;
        GridData gridData14 = new GridData();
        gridData14.grabExcessHorizontalSpace = false;
        GridData gridData13 = new GridData();
        gridData13.horizontalAlignment = 4;
        gridData13.grabExcessHorizontalSpace = true;
        GridData gridData12 = new GridData();
        gridData12.horizontalAlignment = 3;
        GridData gridData111 = new GridData();
        gridData111.horizontalAlignment = 3;
        GridData gridData10 = new GridData();
        gridData10.horizontalAlignment = 3;
        GridData gridData9 = new GridData();
        gridData9.horizontalAlignment = 3;
        GridData gridData8 = new GridData();
        gridData8.horizontalAlignment = 3;
        GridData gridData7 = new GridData();
        gridData7.horizontalAlignment = 3;
        GridData gridData6 = new GridData();
        gridData6.horizontalAlignment = 3;
        GridData gridData5 = new GridData();
        gridData5.horizontalAlignment = 3;
        GridData gridData41 = new GridData();
        gridData41.horizontalAlignment = 3;
        GridData gridData31 = new GridData();
        gridData31.horizontalAlignment = 1;
        gridData31.grabExcessHorizontalSpace = false;
        GridData gridData21 = new GridData();
        gridData21.horizontalAlignment = 1;
        gridData21.grabExcessHorizontalSpace = false;
        GridData gridData11 = new GridData();
        gridData11.horizontalSpan = 5;
        gridData11.horizontalAlignment = 4;
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = 4;
        gridData3.horizontalSpan = 2;
        gridData3.grabExcessHorizontalSpace = true;
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = 4;
        gridData2.horizontalSpan = 2;
        gridData2.grabExcessHorizontalSpace = true;
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = 4;
        gridData1.horizontalSpan = 2;
        gridData1.grabExcessHorizontalSpace = true;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = 4;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 6;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        this.tagsComposite = new Composite(this, 0);
        this.tagsComposite.setLayout(gridLayout);
        this.tagsComposite.setLayoutData(gridData13);
        this.label = new Label(this.tagsComposite, 0);
        this.label.setText(Messages.getString("TagEditorGui.artist"));
        this.label.setLayoutData(gridData8);
        this.artistText = new Text(this.tagsComposite, 2048);
        this.artistText.setText("");
        this.artistText.setLayoutData(gridData1);
        this.label1 = new Label(this.tagsComposite, 0);
        this.label1.setText(Messages.getString("TagEditorGui.writer"));
        this.label1.setLayoutData(gridData12);
        this.writerText = new Text(this.tagsComposite, 2048);
        this.writerText.setLayoutData(gridData3);
        this.label2 = new Label(this.tagsComposite, 0);
        this.label2.setText(Messages.getString("TagEditorGui.title"));
        this.label2.setLayoutData(gridData7);
        this.titleText = new Text(this.tagsComposite, 2048);
        this.titleText.setLayoutData(gridData);
        this.label3 = new Label(this.tagsComposite, 0);
        this.label3.setText(Messages.getString("TagEditorGui.album"));
        this.label3.setLayoutData(gridData111);
        this.albumText = new Text(this.tagsComposite, 2048);
        this.albumText.setLayoutData(gridData2);
        this.label4 = new Label(this.tagsComposite, 0);
        this.label4.setText(Messages.getString("TagEditorGui.genre"));
        this.label4.setLayoutData(gridData6);
        this.createGenreCombo();
        this.label5 = new Label(this.tagsComposite, 0);
        this.label5.setText(Messages.getString("TagEditorGui.year"));
        this.label5.setLayoutData(gridData10);
        this.yearText = new Text(this.tagsComposite, 2048);
        this.yearText.setLayoutData(gridData14);
        new Label(this.tagsComposite, 0);
        this.label6 = new Label(this.tagsComposite, 0);
        this.label6.setText(Messages.getString("TagEditorGui.track"));
        this.label6.setLayoutData(gridData5);
        this.trackText = new Text(this.tagsComposite, 2048);
        this.trackText.setLayoutData(gridData21);
        this.label9 = new Label(this.tagsComposite, 0);
        this.label9.setText(Messages.getString("TagEditorGui.numberTotal"));
        this.label9.setFont(new Font(Display.getDefault(), "Tahoma", 8, 2));
        this.label9.setForeground(Display.getCurrent().getSystemColor(17));
        this.label9.setLayoutData(gridData15);
        this.label9.setEnabled(true);
        this.label7 = new Label(this.tagsComposite, 0);
        this.label7.setText(Messages.getString("TagEditorGui.disc"));
        this.label7.setLayoutData(gridData9);
        this.discText = new Text(this.tagsComposite, 2048);
        this.discText.setLayoutData(gridData31);
        this.label10 = new Label(this.tagsComposite, 0);
        this.label10.setText(Messages.getString("TagEditorGui.numberTotal"));
        this.label10.setEnabled(true);
        this.label10.setFont(new Font(Display.getDefault(), "Tahoma", 8, 2));
        this.label10.setForeground(Display.getCurrent().getSystemColor(17));
        this.label10.setLayoutData(gridData22);
        this.label8 = new Label(this.tagsComposite, 0);
        this.label8.setText(Messages.getString("TagEditorGui.comment"));
        this.label8.setLayoutData(gridData41);
        this.commentText = new Text(this.tagsComposite, 2048);
        this.commentText.setLayoutData(gridData11);
    }

    private void createGenreCombo() {
        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = 4;
        gridData4.horizontalSpan = 2;
        gridData4.grabExcessHorizontalSpace = true;
        this.genreCombo = new Combo(this.tagsComposite, 0);
        this.genreCombo.setLayoutData(gridData4);
        this.fillGenres();
    }

    private void fillGenres() {
        for (int i = 0; i < genres.length; ++i) {
            this.genreCombo.add(genres[i]);
        }

    }
}
