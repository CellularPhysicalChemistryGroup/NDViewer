///////////////////////////////////////////////////////////////////////////////
//FILE:          MetadataPanel.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Henry Pinkard, henry.pinkard@gmail.com, & Arthur Edelstein, 2010 (University of California, San Francisco, 2012)
//
// AUTHOR:      Joshua Riback, 2023 (Baylor College of Medicine)
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package org.micromanager.ndviewer.internal.gui.contrast;

import java.awt.*;
import javax.swing.*;

import org.micromanager.ndviewer.internal.gui.contrast.HistogramPanel.CursorListener;
import org.micromanager.ndviewer.main.NDViewer;

/**
 * Draws one histogram of the Multi-Channel control panel - All edits made for @CPCGTools are denoted as 
 *
 *
 */
class ChannelControlPanel extends JPanel implements CursorListener {

   private static final Dimension CONTROLS_SIZE = new Dimension(130, 150);
   public static final Dimension MINIMUM_SIZE = new Dimension(400, CONTROLS_SIZE.height);

   private HistogramPanel hp_;
   private NDViewer display_;
   private JButton autoButton_;
   private JCheckBox channelNameCheckbox_;
   private JLabel colorPickerLabel_;
   private JButton fullButton_;
   private JPanel histogramPanelHolder_;
   private JLabel minMaxLabel_;
   private double binSize_;
   private int height_;
   private String histMaxLabel_;
   private int histMax_;
   private JPanel controls_;
   private JPanel controlsHolderPanel_;
   final private int maxIntensity_;
   final private int bitDepth_;
   private Color color_;
   private ContrastPanel contrastPanel_;
   private final String channelName_;
   private int pixelMin_, pixelMax_;
   
// Fields added in 2023 by @CellularPhysicalChemistryGroup for CPCGTools
   private String histMinLabel_;
   private double pixelMean_, pixelMeanSquared_;
//

   public ChannelControlPanel(NDViewer disp,
           ContrastPanel contrastPanel, String name, Color color, int bitDepth) {
      channelName_ = name;
      contrastPanel_ = contrastPanel;
      display_ = disp;
      color_ = color;
      bitDepth_ = bitDepth;
      maxIntensity_ = (int) Math.pow(2, bitDepth_) - 1;
      histMax_ = maxIntensity_ + 1;
      binSize_ = histMax_ / DisplaySettings.NUM_DISPLAY_HIST_BINS;
      histMaxLabel_ = "" + histMax_;
      histMinLabel_ = "" + 0;//Added in 2023 by @CellularPhysicalChemistryGroup for CPCGTools
      initComponents();
      channelNameCheckbox_.setSelected(display_.getDisplaySettingsObject().isActive(channelName_));
      redraw();
   }

   private void initComponents() {

      fullButton_ = new javax.swing.JButton();
      autoButton_ = new javax.swing.JButton();
      colorPickerLabel_ = new javax.swing.JLabel();
      channelNameCheckbox_ = new javax.swing.JCheckBox();
      histogramPanelHolder_ = new javax.swing.JPanel();
      minMaxLabel_ = new javax.swing.JLabel();

      setOpaque(false);
      setPreferredSize(new java.awt.Dimension(250, height_));

      fullButton_.setFont(fullButton_.getFont().deriveFont((float) 9));
      fullButton_.setName("Full channel histogram width");
      fullButton_.setText("Full");
      fullButton_.setToolTipText("Stretch the display gamma curve over the full pixel range");
      fullButton_.setMargin(new java.awt.Insets(2, 4, 2, 4));
      fullButton_.setPreferredSize(new java.awt.Dimension(75, 30));
      fullButton_.addActionListener(new java.awt.event.ActionListener() {

         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            fullButtonAction();
         }
      });

      autoButton_.setFont(autoButton_.getFont().deriveFont((float) 9));
      autoButton_.setName("Auto channel histogram width");
      autoButton_.setText("Auto");
      autoButton_.setToolTipText("Align the display gamma curve with minimum and maximum measured intensity values");
      autoButton_.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      autoButton_.setIconTextGap(0);
      autoButton_.setMargin(new java.awt.Insets(2, 4, 2, 4));
      autoButton_.setMaximumSize(new java.awt.Dimension(75, 30));
      autoButton_.setMinimumSize(new java.awt.Dimension(75, 30));
      autoButton_.setPreferredSize(new java.awt.Dimension(75, 30));
      autoButton_.addActionListener(new java.awt.event.ActionListener() {

         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            autoButtonAction();
         }
      });

      colorPickerLabel_.setBackground(color_);
      colorPickerLabel_.setToolTipText("Change the color for displaying this channel");
      colorPickerLabel_.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
      colorPickerLabel_.setOpaque(true);
      colorPickerLabel_.addMouseListener(new java.awt.event.MouseAdapter() {

         @Override
         public void mouseClicked(java.awt.event.MouseEvent evt) {
            colorPickerLabelMouseClicked();
         }
      });

      channelNameCheckbox_.setText(channelName_.equals(NDViewer.NO_CHANNEL) ? "" : channelName_ );
      channelNameCheckbox_.setToolTipText("Show/hide this channel in the multi-dimensional viewer");
      channelNameCheckbox_.addActionListener(new java.awt.event.ActionListener() {

         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            channelNameCheckboxAction();
         }
      });

      histogramPanelHolder_.setToolTipText("Adjust the brightness and contrast by dragging triangles at top and bottom. Change the gamma by dragging the curve. (These controls only change display, and do not edit the image data.)");
      histogramPanelHolder_.setAlignmentX(0.3F);
      histogramPanelHolder_.setPreferredSize(new java.awt.Dimension(0, 100));
      histogramPanelHolder_.setLayout(new BorderLayout());

      minMaxLabel_.setFont(new java.awt.Font("Lucida Grande", 0, 10));
      minMaxLabel_.setText("Min:   Max:");

      this.setMinimumSize(MINIMUM_SIZE);
      this.setPreferredSize(MINIMUM_SIZE);

      hp_ = addHistogramPanel();

      this.setLayout(new BorderLayout());
      controlsHolderPanel_ = new JPanel(new BorderLayout());
      controlsHolderPanel_.setPreferredSize(CONTROLS_SIZE);

      controls_ = new JPanel();
      this.add(controlsHolderPanel_, BorderLayout.LINE_START);
      this.add(histogramPanelHolder_, BorderLayout.CENTER);

      controlsHolderPanel_.add(controls_, BorderLayout.PAGE_START);
      GridBagLayout gbl = new GridBagLayout();
      controls_.setLayout(gbl);

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.gridwidth = 5;
      gbc.weightx = 1;
      gbc.weighty = 1;
      gbc.anchor = GridBagConstraints.LINE_START;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      controls_.add(channelNameCheckbox_, gbc);

      fullButton_.setPreferredSize(new Dimension(45, 20));
      autoButton_.setPreferredSize(new Dimension(45, 20));
      colorPickerLabel_.setPreferredSize(new Dimension(18, 18));
      FlowLayout flow = new FlowLayout();
      flow.setHgap(4);
      flow.setVgap(0);
      JPanel line2 = new JPanel(flow);
      line2.setPreferredSize(CONTROLS_SIZE);
      line2.add(fullButton_);
      line2.add(autoButton_);
      line2.add(colorPickerLabel_);
      line2.setPreferredSize(new Dimension(CONTROLS_SIZE.width, 20));

      gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.weightx = 1;
      gbc.weighty = 1;
      gbc.gridwidth = 5;
      gbc.anchor = GridBagConstraints.LINE_START;
      controls_.add(line2, gbc);

      gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 4;
      gbc.weightx = 1;
      gbc.weighty = 1;
      gbc.gridwidth = 5;
      gbc.anchor = GridBagConstraints.LINE_START;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      controls_.add(minMaxLabel_, gbc);

      controls_.setPreferredSize(controls_.getMinimumSize());
   }
   
//  Method added by @CellularPhysicalChemistryGroup - adds controls to the side of the histograms 
    public void addChannelSideControls(String text/*"BK,FF,&Calib."*/){
        javax.swing.JCheckBox newCheckbox = new javax.swing.JCheckBox();
        newCheckbox.setText(text);
        newCheckbox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                display_.setChannelSideControlsSelection(text, channelName_, newCheckbox.isSelected());
                display_.onContrastUpdated();
                redraw();
            }
        });
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controls_.add(newCheckbox, gbc);
        
        //Not sure if these are needed
        controls_.setPreferredSize(controls_.getMinimumSize());
        display_.onContrastUpdated();
        redraw();
    }
//

   public String getChannelName() {
      return channelName_;
   }

   private void redraw() {
      int contrastMin = display_.getDisplaySettingsObject().getContrastMin(channelName_);
      int contrastMax = display_.getDisplaySettingsObject().getContrastMax(channelName_);
      double gamma = display_.getDisplaySettingsObject().getContrastGamma(channelName_);

//  Change by @CellularPhysicalChemistryGroup so histogram is flexible.
      double[] corrections = display_.getChannelIntensityCorrections(channelName_);     
      hp_.setCursorText(convertIntensityToText(contrastMin,corrections[0],corrections[1]) + "", convertIntensityToText(contrastMax,corrections[0],corrections[1]) + "");
//

      hp_.setCursors(contrastMin / binSize_, (contrastMax + 1) / binSize_, gamma);
      hp_.repaint();
   }

   private void fullButtonAction() {
      display_.getDisplaySettingsObject().setContrastMin(channelName_, 0);
      display_.getDisplaySettingsObject().setContrastMax(channelName_, (int) (Math.pow(2, bitDepth_) - 1));

      display_.onContrastUpdated();
   }

   public void autoButtonAction() {
      display_.getDisplaySettingsObject().setContrastMin(channelName_, pixelMin_);
      display_.getDisplaySettingsObject().setContrastMax(channelName_, pixelMax_);

      display_.onContrastUpdated();
      redraw();

   }

   private void colorPickerLabelMouseClicked() {
      Color newColor = JColorChooser.showDialog(this, "Choose a color for the "
              + channelName_ + " channel", color_);
      if (newColor != null) {
         color_ = newColor;
      }
      display_.setChannelColor(channelName_, color_);
      colorPickerLabel_.setBackground(color_);
      hp_.setTraceStyle(true, color_);
      String name = channelName_;
      if (channelName_.length() > 11) {
         name = name.substring(0, 9) + "...";
      }
      channelNameCheckbox_.setText(name);
      display_.onContrastUpdated();
      this.repaint();
   }

   private void channelNameCheckboxAction() {
      display_.channelSetActiveByCheckbox(channelName_, channelNameCheckbox_.isSelected());
      display_.onContrastUpdated();
      redraw();

   }

   private HistogramPanel addHistogramPanel() {
      HistogramPanel hp = new HistogramPanel() {

         @Override
         public void paint(Graphics g) {
            if (channelNameCheckbox_.isSelected()) {
               super.paint(g);
               
//  Change by @CellularPhysicalChemistryGroup so histogram is flexible.               
               //For drawing min label - new for CPCG (since not zero. Should be in ChannelControlPanel!
               g.setColor(UIManager.getColor("Label.foreground"));
               g.setFont(new Font("Lucida Grande", 0, 10));
               g.drawString(histMinLabel_, 8, 10);
//
               //For drawing max label
               g.setColor(UIManager.getColor("Label.foreground"));
               g.setFont(new Font("Lucida Grande", 0, 10));
               g.drawString(histMaxLabel_, this.getSize().width - 8 * histMaxLabel_.length(), this.getSize().height);
            } else {
               g.setColor(super.getBackground());
               g.fillRect(super.getX(), super.getY(), super.getWidth(), super.getHeight());
            }
         }
      };
      hp.setMargins(12, 12);
      hp.setTraceStyle(true, color_);
      hp.setToolTipText("Click and drag curve to adjust gamma");
      histogramPanelHolder_.add(hp, BorderLayout.CENTER);
      hp.addCursorListener(this);
      return hp;
   }
   
//  Change by @CellularPhysicalChemistryGroup so histogram is flexible.
   public void updateHistogram(int[] rawHistogram) {
      hp_.setVisible(true);
      //Draw histogram and stats
      GraphData histogramData = new GraphData();
      // Convert from full histogram to display histogram
      pixelMin_ = -1;
      pixelMax_ = 0;
      pixelMean_ = 0;
      pixelMeanSquared_ = 0;
      int tot = 0;
      int binSize = rawHistogram.length == 256 ? 1 : (int) (Math.pow(2, bitDepth_ - 8));
      int numBins = (int) Math.min(rawHistogram.length / binSize, DisplaySettings.NUM_DISPLAY_HIST_BINS);
      int[] displayHistogram_ = new int[DisplaySettings.NUM_DISPLAY_HIST_BINS];
      for (int i = 0; i < numBins; i++) {
         displayHistogram_[i] = 0;
         for (int j = 0; j < binSize; j++) {
            int rawHistIndex = (int) (i * binSize + j);
            int rawHistVal = rawHistogram[rawHistIndex];
            pixelMean_+= ((double) rawHistIndex)*((double) rawHistVal);
            pixelMeanSquared_+= ((double) rawHistIndex)*((double) rawHistIndex)*((double) rawHistVal);
            if ((pixelMean_<0)||(pixelMeanSquared_<0)){
                tot=1;
            }
            tot+=rawHistVal;
            displayHistogram_[i] += rawHistVal;
            if (rawHistVal > 0) {
               pixelMax_ = rawHistIndex;
               if (pixelMin_ == -1) {
                  pixelMin_ = rawHistIndex;
               }
            }
         }
         if (display_.getDisplaySettingsObject().isLogHistogram()) {
            displayHistogram_[i] = displayHistogram_[i] > 0 ? (int) (1000 * Math.log(displayHistogram_[i])) : 0;
         }
      }
      pixelMean_=pixelMean_/tot;
      pixelMeanSquared_=pixelMeanSquared_/tot;
      histogramData.setData(displayHistogram_);
      hp_.setData(histogramData);
      hp_.setAutoScale();
      hp_.repaint();
      
      double[] corrections = display_.getChannelIntensityCorrections(channelName_);
      minMaxLabel_.setText("Mean: " + convertIntensityToText((int) pixelMean_,corrections[0],corrections[1])+ "   " + "Std: " +convertIntensityToText((int) Math.sqrt(pixelMeanSquared_-pixelMean_*pixelMean_),0.0,corrections[1])); 
      //minMaxLabel_.setText("Min: " + convertIntensityToText(pixelMin_,corrections[0],corrections[1]) + "   " + "Max: " + convertIntensityToText(pixelMax_,corrections[0],corrections[1]));
      histMinLabel_ = String.valueOf(convertIntensityToText(0,corrections[0],corrections[1]));
      histMaxLabel_ = String.valueOf(convertIntensityToText(rawHistogram.length-1,corrections[0],corrections[1]));

      redraw();

   }

   private String convertIntensityToText(int val, double offset, double scale){
       return String.format("%.2f", scale*val+offset);
   }
//

   public void contrastMaxInput(int max) {
      contrastPanel_.disableAutostretch();
      display_.getDisplaySettingsObject().setContrastMax(channelName_, max);
      display_.onContrastUpdated();
      redraw();

   }

   @Override
   public void contrastMinInput(int min) {
      contrastPanel_.disableAutostretch();
      display_.getDisplaySettingsObject().setContrastMax(channelName_, min);
      display_.onContrastUpdated();
      redraw();

   }

   public void onLeftCursor(double pos) {
      contrastPanel_.disableAutostretch();
      display_.getDisplaySettingsObject().setContrastMin(channelName_, 
              (int) (Math.min(DisplaySettings.NUM_DISPLAY_HIST_BINS - 1, pos) * binSize_));
      display_.onContrastUpdated();
      redraw();
   }

   @Override
   public void onRightCursor(double pos) {
      contrastPanel_.disableAutostretch();
      display_.getDisplaySettingsObject().setContrastMax(channelName_, 
              (int) (Math.min(DisplaySettings.NUM_DISPLAY_HIST_BINS - 1, pos) * binSize_));
      display_.onContrastUpdated();
      redraw();

   }

//  Change by @CellularPhysicalChemistryGroup - Josh does not like giving access to gamma. Will implement Fire color instead at somepoint soon. 
   @Override
   public void onGammaCurve(double gamma) {
      /*if (gamma != 0) {
         if (gamma > 0.9 & gamma < 1.1) {
            gamma = 1;
         }
         display_.getDisplaySettingsObject().setGamma(channelName_, gamma);
         display_.onContrastUpdated();
      }*/
      //No Gamma changes 
      if (gamma!=1){
        gamma = 1;
        display_.getDisplaySettingsObject().setGamma(channelName_, gamma);
        display_.onContrastUpdated();
      }
      redraw();
   }
//
   
   void updateActiveCheckbox(boolean active) {
      channelNameCheckbox_.setSelected(active);
      redraw();
   }

}
