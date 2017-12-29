package com.freeipodsoftware.abc;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class TagEditorGui extends Composite {
    private static final String[] genres = new String[]{"Alternative", "Blues", "Books & Spoken", "Children's Music", "Classical", "Comedy", "Country", "Dance", "Easy Listening", "Electronic", "Folk", "Funk", "House", "Hip Hop", "Indie", "Industrial", "Jazz", "Metal", "Misc", "New Age", "Poscast", "Pop", "Punk", "Religious", "Rock", "Reggae", "Soft Rock", "Soundtrack", "Techno", "Trance", "Unclassifiable", "World"};
    private Composite tagsComposite = null;
    private Label writerLabel = null;
    protected Text writerText = null;
    private Label narratorLebel = null;
    protected Text narratorText = null;
    private Label titleLabel = null;
    protected Text titleText = null;
    private Label seriesLabel = null;
    protected Text series = null;
    private Label label4 = null;
    protected Combo genreCombo = null;
    private Label label5 = null;
    protected Text yearText = null;
    private Label label6 = null;
    protected Text trackText = null;
    private Label totalBooksLabel = null;
    protected Text totalBooksText = null;
    private Label label8 = null;
    protected Text commentText = null;
    private Label label9 = null;
    private Label label10 = null;

    public TagEditorGui(Composite parent) {
        super(parent, 0);
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

        this.writerLabel = new Label(this.tagsComposite, 0);
        this.writerLabel.setText(Messages.getString("TagEditorGui.writer"));
        this.writerLabel.setLayoutData(gridData8);
        this.writerText = new Text(this.tagsComposite, 2048);
        this.writerText.setText("");
        this.writerText.setLayoutData(gridData1);

        this.narratorLebel = new Label(this.tagsComposite, 0);
        this.narratorLebel.setText(Messages.getString("TagEditorGui.narrator"));
        this.narratorLebel.setLayoutData(gridData12);
        this.narratorText = new Text(this.tagsComposite, 2048);
        this.narratorText.setLayoutData(gridData3);

        this.titleLabel = new Label(this.tagsComposite, 0);
        this.titleLabel.setText(Messages.getString("TagEditorGui.title"));
        this.titleLabel.setLayoutData(gridData7);
        this.titleText = new Text(this.tagsComposite, 2048);
        this.titleText.setLayoutData(gridData);

        this.seriesLabel = new Label(this.tagsComposite, 0);
        this.seriesLabel.setText(Messages.getString("TagEditorGui.series"));
        this.seriesLabel.setLayoutData(gridData111);
        this.series = new Text(this.tagsComposite, 2048);
        this.series.setLayoutData(gridData2);

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
        this.label6.setText(Messages.getString("TagEditorGui.bookNumber"));
        this.label6.setLayoutData(gridData5);
        this.trackText = new Text(this.tagsComposite, 2048);
        this.trackText.setLayoutData(gridData21);
        this.label9 = new Label(this.tagsComposite, 0);
//        this.label9.setText(Messages.getString("TagEditorGui.numberTotal"));
        this.label9.setText("");
        this.label9.setFont(new Font(Display.getDefault(), "Tahoma", 8, 2));
        this.label9.setForeground(Display.getCurrent().getSystemColor(17));
        this.label9.setLayoutData(gridData15);
        this.label9.setEnabled(true);

        this.totalBooksLabel = new Label(this.tagsComposite, 0);
        this.totalBooksLabel.setText(Messages.getString("TagEditorGui.totalBooks"));
        this.totalBooksLabel.setLayoutData(gridData9);

        this.totalBooksText = new Text(this.tagsComposite, 2048);
        this.totalBooksText.setLayoutData(gridData31);

        this.label10 = new Label(this.tagsComposite, 0);
//        this.label10.setText(Messages.getString("TagEditorGui.numberTotal"));
        this.label10.setText("");
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
        for (String genre : genres) {
            this.genreCombo.add(genre);
        }

    }
}
