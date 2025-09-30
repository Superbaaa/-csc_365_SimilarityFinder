import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GUI extends JFrame {
    public FileScraper fileScraper;
    private JComboBox<String> comboBox;
    private JTextArea resultArea;

    public GUI(FileScraper scraper) {
        this.fileScraper = scraper;
        setupGUI();
    }

    private void setupGUI() {
        setTitle("Wiki Recommendation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Choose document:"));

        comboBox = new JComboBox<>();
        for (String title : fileScraper.pageTitles) {
            comboBox.addItem(title);
        }
        topPanel.add(comboBox);

        JButton findBtn = new JButton("Find Similar");
        findBtn.addActionListener(this::findSimilarAction);
        topPanel.add(findBtn);

        add(topPanel, BorderLayout.NORTH);

        resultArea = new JTextArea(15, 60);
        resultArea.setEditable(false);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private void findSimilarAction(ActionEvent e) {
        int index = comboBox.getSelectedIndex();
        String result = fileScraper.displayTopTwoSimilarDocuments(index);
        resultArea.setText(result);

    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FileScraper scraper = new FileScraper("./src/wikipedia");
                scraper.processPage();
                new GUI(scraper).setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        });
    }
}
