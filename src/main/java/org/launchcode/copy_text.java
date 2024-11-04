package org.launchcode;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class copy_text extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            copy_text copytext = new copy_text();
            copytext.setVisible(true);
        });
    }
    private JTextArea resultArea;
    private JButton captureButton;
    private Point startPoint;
    private Rectangle selectionRect;
    private BufferedImage screenImage;

    public copy_text() {
        // Set up the frame
        setTitle("Screen OCR Tool");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create a button to trigger OCR
        captureButton = new JButton("Capture Text from Screen");
        captureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                captureScreenAndExtractText();
            }
        });

        // Text area to display OCR result
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Set up layout
        setLayout(new BorderLayout());
        add(captureButton, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void captureScreenAndExtractText() {
        try {
            // Take a screenshot of the whole screen
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            screenImage = robot.createScreenCapture(new Rectangle(screenSize));

            // Create a transparent overlay window for selection
            JWindow selectionWindow = new JWindow();
            selectionWindow.setBackground(new Color(0, 0, 0, 50)); // Semi-transparent background
            selectionWindow.setSize(screenSize);
            selectionWindow.setVisible(true);

            // Add a custom component to handle the selection drawing
            JComponent selectionComponent = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (selectionRect != null) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setColor(Color.RED);
                        g2.setStroke(new BasicStroke(2));
                        g2.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
                    }
                }
            };

            selectionWindow.add(selectionComponent);

            // Event listeners for mouse actions
            selectionWindow.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                    selectionRect = new Rectangle();
                    selectionComponent.repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    selectionWindow.setVisible(false);
                    selectionWindow.dispose();
                    performOCROnSelection();
                }
            });

            selectionWindow.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    selectionRect.setBounds(
                            Math.min(startPoint.x, e.getX()),
                            Math.min(startPoint.y, e.getY()),
                            Math.abs(startPoint.x - e.getX()),
                            Math.abs(startPoint.y - e.getY())
                    );
                    selectionComponent.repaint();
                }
            });

        } catch (AWTException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error capturing the screen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void performOCROnSelection() {
        if (selectionRect != null && screenImage != null) {
            try {
                // Crop the selected area from the screenshot
                BufferedImage selectedImage = screenImage.getSubimage(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);

                // Perform OCR on the selected image
                ITesseract tesseract = new Tesseract();
                tesseract.setDatapath("./"); // Set the path to tessdata directory

                String ocrResult = tesseract.doOCR(selectedImage);

                // Display OCR result in the text area
                resultArea.setText("OCR Result:\n\n" + ocrResult);
            } catch (TesseractException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error performing OCR: " + e.getMessage(), "OCR Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
