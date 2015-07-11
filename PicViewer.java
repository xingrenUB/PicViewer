import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.*;
// import com.drew.imaging.ImageProcessingException;
import java.util.Iterator;
import java.util.Date;

public class PicViewer implements ActionListener {
    private JLabel imgLabel;
    private JFrame mainFrame;
    private Container con;
    private JButton openBtn, preBtn, nextBtn, refreshBtn, delBtn;
    private JTextArea exifArea;
    private JPanel ctrlPanel;
    private JScrollPane imgPanel, exifPanel;
    private File[] files;
    private File path;
    private int idx = -1, minIdx, maxIdx;
    private int width = 1600, height = 800;
    
    public PicViewer(File path) {
        mainFrame = new JFrame("PicViewer");
        con = mainFrame.getContentPane();
        ctrlPanel = new JPanel();
        ctrlPanel.setLayout(new FlowLayout());

        openBtn = new JButton("Open File");
        preBtn = new JButton("Previous");
        nextBtn = new JButton("Next");
        refreshBtn = new JButton("Refresh");
        delBtn = new JButton("Delete");

        openBtn.addActionListener(this);
        preBtn.addActionListener(this);
        nextBtn.addActionListener(this);
        delBtn.addActionListener(this);
        refreshBtn.addActionListener(this);

        ctrlPanel.add(openBtn);
        ctrlPanel.add(preBtn);
        ctrlPanel.add(nextBtn);
        ctrlPanel.add(refreshBtn);
        ctrlPanel.add(delBtn);

        imgLabel = new JLabel("", null, JLabel.CENTER);
        imgPanel = new JScrollPane(imgLabel);
        exifArea = new JTextArea(20, 20);
        exifArea.setEditable(false);
        exifPanel = new JScrollPane(exifArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        // exifPanel.setAutoscrolls(false);

        con.add(ctrlPanel,BorderLayout.SOUTH);
        con.add(imgPanel,BorderLayout.CENTER);
        con.add(exifPanel,BorderLayout.EAST);
        mainFrame.setSize(width, height);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setPath(path);
        minIdx = -1;
        maxIdx = files.length;
        showNext();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == openBtn) {
            JFileChooser chooser = new JFileChooser();
            if(chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (isImage(file)) {
                    showImage(file);
                }     
            }
        }
        if (e.getSource() == preBtn) {
            showPrevious();
        }
        if (e.getSource() == nextBtn) {
            showNext();
        }
        if (e.getSource() == refreshBtn) {
            files = path.listFiles();
        }
        if (e.getSource() == delBtn) {
            delete(files[idx]);
        }
    }

    public static boolean isImage(File file) {
        String[] valid_extensions = {"jpg", "jpeg", "gif", "bmp", "png", "tiff"};
        for (String ext : valid_extensions) {
            if (file.getName().toLowerCase().endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    public void showImage(File file) {
        if (file.getName().toLowerCase().endsWith(".gif")) {
            imgLabel.setIcon(new ImageIcon(file.getAbsolutePath()));
        } else {
            Image img = null;
            try {
                img = ImageIO.read(file);
            } catch (IOException ex) {
                System.out.println("File not exists");
            }
            Image scaledImg = img.getScaledInstance(-1, Math.min(img.getHeight(null), 
                imgPanel.getHeight()), Image.SCALE_DEFAULT);
            imgLabel.setIcon(new ImageIcon(scaledImg));
            displayExif(file);
        }
    }

    public void showPrevious() {
        while (idx > 0) {
            idx--;
            if (isImage(files[idx])) {
                showImage(files[idx]);
                if (idx < minIdx || minIdx == -1) {
                    minIdx = idx;
                }
                break;
            }
        }
        if (idx == 0) {
            idx = minIdx;
        }
    }

    public void showNext() {
        while (idx < files.length - 1) {
            idx++;
            if (isImage(files[idx])) {
                showImage(files[idx]);
                if (idx > maxIdx || maxIdx == files.length) {
                    maxIdx = idx;
                }
                break;
            }
        }
        if (idx == files.length - 1) {
            idx = maxIdx;
        }
    }

    public void delete(File path) {
        int ans = JOptionPane.showConfirmDialog(mainFrame, "Do you want to delete the file?");
        if (ans == JOptionPane.CANCEL_OPTION || ans == JOptionPane.NO_OPTION) {
            return;
        }
        try {
            path.delete();
        } catch (SecurityException ex) {
            System.err.println(ex);
        }
        files = this.path.listFiles();
        idx--;
        minIdx = -1;
        maxIdx = files.length;
        showNext();
    }

    public void setPath(File path) {
        if (!path.exists() || (!path.isDirectory() && !isImage(path))) {
            return;
        }
        else if (path.isDirectory()) {
            this.path = path;
        } else {
            this.path = path.getParentFile();
        }
        files = this.path.listFiles();
    }

    public void displayExif(File img) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(img);
            Directory directory = metadata.getDirectory(ExifDirectory.class);
            Iterator iter = directory.getTagIterator();
            String exifText = "EXIF\n";
            while (iter.hasNext()) {
                Tag tag = (Tag) iter.next();
                exifText += tag.getTagName() + "\t" + tag.getDescription() + "\n";
            } 
            exifArea.setText(exifText);
        } catch (Exception ex) {
            System.err.println(ex);
        }             
    }

    public static void main(String[] args) {
        new PicViewer(new File(args[0]));
    }
}