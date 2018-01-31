package com.freeipodsoftware.abc;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;

import static com.freeipodsoftware.abc.AppProperties.WEBSITE_URL;

public class AboutDialogGui extends Composite {
    private Label label = null;
    private Label label1 = null;
    private Link link = null;
    private Link link1 = null;
    private Link link2 = null;
    private Link link3 = null;
    private Link link4 = null;
    private Link link5 = null;
    private Composite composite = null;
    private Button closeButton = null;
    private Link link6;
    private Link link7 = null;
    private Label label2 = null;
    private Label label3 = null;
    private Link jid3Link = null;

    public Button getCloseButton() {
        return this.closeButton;
    }

    private void createComposite() {
        GridData gridData3 = new GridData();
        gridData3.grabExcessHorizontalSpace = true;
        gridData3.widthHint = 80;
        gridData3.horizontalAlignment = 3;
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 0;
        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.grabExcessVerticalSpace = true;
        gridData2.verticalAlignment = 3;
        gridData2.horizontalAlignment = 4;
        this.composite = new Composite(this, 0);
        this.composite.setLayoutData(gridData2);
        this.composite.setLayout(gridLayout);
        this.closeButton = new Button(this.composite, 0);
        this.closeButton.setText(Messages.getString("AboutComposite.close"));
        this.closeButton.setLayoutData(gridData3);
        this.closeButton.setFocus();
    }

    private void assignLink(Link link, final String url) {
        link.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Program.launch(url);
            }
        });
    }

    public static void main(String[] args) {
        Display display = Display.getDefault();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setSize(new Point(300, 200));
        new AboutDialogGui(shell);
        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }

    public AboutDialogGui(Composite parent) {
        super(parent, 0);
        this.initialize();
    }

    private void initialize() {
        GridData gridData11 = new GridData();
        gridData11.grabExcessHorizontalSpace = true;
        gridData11.widthHint = 510;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.marginHeight = 0;
        gridLayout1.marginWidth = 0;
        gridLayout1.numColumns = 1;
        this.setLayout(gridLayout1);
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = true;
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = 510;
        Label titleLabel = new Label(this, 0);
        titleLabel.setText(Messages.getString("MainWindow2.programName"));
        titleLabel.setFont(new Font(Display.getDefault(), "Tahoma", 8, 1));
        Label versionLabel = new Label(this, 0);
        versionLabel.setText(Messages.getString("AboutComposite.version") + Version.getVersionString());
        this.link7 = new Link(this, 0);
        this.link7.setText(Messages.getString("AboutComposite.website") + ": <a>AudioBookConverter</a>");
        this.assignLink(this.link7, WEBSITE_URL);
        this.label = new Label(this, 64);
        this.label.setText(Messages.getString("AboutComposite.description"));
        this.label.setLayoutData(gridData);
        this.label1 = new Label(this, 0);
        this.label1.setText(Messages.getString("AboutComposite.thirdPartyText") + ":");
        this.link = new Link(this, 0);
        this.link.setText("<a>FFMpeg</a>");
        this.assignLink(this.link, "https://ffmpeg.org");
        this.link1 = new Link(this, 0);
        this.link1.setText("<a>MP4V2</a>");
        this.assignLink(this.link1, "https://code.google.com/p/mp4v2//");
        this.link2 = new Link(this, 0);
        this.link2.setText("<a>Apache Jakarta Commons</a>");
        this.assignLink(this.link2, "https://commons.apache.org/");
        this.link3 = new Link(this, 0);
        this.link3.setText("<a>Eclipse SWT</a>");
        this.assignLink(this.link3, "http://eclipse.org/swt");
        this.jid3Link = new Link(this, 0);
        this.jid3Link.setText("<a>FFMpeg Cli Wrapper</a>");
        this.assignLink(this.jid3Link, "https://github.com/bramp/ffmpeg-cli-wrapper");
        this.link5 = new Link(this, 0);
        this.link5.setText(Messages.getString("AboutComposite.developedBy") + " <a>Florian Fankhauser</a> " + Messages.getString("AboutComposite.usingJava") + ".");
        this.assignLink(this.link5, "http://ffxml.net");
        Link modifiedLink = new Link(this, 0);
        modifiedLink.setText(Messages.getString("AboutComposite.modifiedBy") + " " + "<a>Yarick Yermak</a>" + ".");
        this.assignLink(modifiedLink, "https://github.com/yermak/");

        this.link6 = new Link(this, 0);
        this.link6.setText(Messages.getString("AboutComposite.forMoreInformationVisit") + " <a>" + WEBSITE_URL + "</a>.");
        this.assignLink(this.link6, WEBSITE_URL);
        this.link4 = new Link(this, 0);
        this.link4.setText(Messages.getString("AboutComposite.releasedUnderThe") + " <a>GNU General Public License</a>.");
        this.assignLink(this.link4, "http://www.gnu.org/licenses/gpl.html");
        this.label2 = new Label(this, 64);
        this.label2.setText("BECAUSE THE PROGRAM IS LICENSED FREE OF CHARGE, THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW. EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES PROVIDE THE PROGRAM \"AS IS\" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION. IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR REDISTRIBUTE THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU FOR DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE PROGRAM (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER PROGRAMS), EVEN IF SUCH HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. ");
        this.label2.setForeground(Display.getCurrent().getSystemColor(16));
        this.label2.setBackground(Display.getCurrent().getSystemColor(1));
        this.label2.setLayoutData(gridData11);
        this.label3 = new Label(this, 0);
        this.label3.setText(Messages.getString("AboutComposite.iPodRegisteredTrademark"));
        this.label3.setForeground(Display.getCurrent().getSystemColor(17));
        this.createComposite();
    }
}
