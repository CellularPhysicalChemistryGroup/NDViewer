///////////////////////////////////////////////////////////////////////////////
//FILE:          MultiChannelHistograms.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Henry Pinkard, henry.pinkard@gmail.com, 2012
//
// COPYRIGHT:    University of California, San Francisco, 2012
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import org.micromanager.ndviewer.main.NDViewer;


final class MultiChannelHistograms extends JPanel {

   private HashMap<String, ChannelControlPanel> ccpList_;
   private NDViewer display_;
   private ContrastPanel contrastPanel_;

   public MultiChannelHistograms(NDViewer disp, ContrastPanel contrastPanel) {
      super();
      display_ = disp;
//      display_.registerForEvents(this);

      this.setLayout(new GridLayout(1, 1));
      contrastPanel_ = contrastPanel;
      ccpList_ = new HashMap<String, ChannelControlPanel>();
//      setupChannelControls();

      // default initialization show a single set of contrast controls
      addContrastControlsIfNeeded(NDViewer.NO_CHANNEL);
   }

   public void readHistogramControlsStateFromGUI() {
      HistogramControlsState state = contrastPanel_.getHistogramControlsState();
      display_.setHistogramSettings(state.autostretch, state.ignoreOutliers, state.syncChannels,
              state.logHist, state.composite, state.percentToIgnore);
   }
      
   public void updateActiveChannelCheckboxes() {
      for (ChannelControlPanel c : ccpList_.values()) {
         c.updateActiveCheckbox(display_.getDisplayModel().isChannelActive(c.getChannelName()));
      }
   }
   
   public void onDisplayClose() {
      display_ = null;
      ccpList_ = null;
      contrastPanel_ = null;
   }

   public void addContrastControlsIfNeeded(String channelName) {
      synchronized (ccpList_) {
         if (ccpList_.containsKey(channelName)) {
            return; // Already added
         }

         // bring back RGB if you want...
//      boolean rgb;
//      try {
//         rgb = display_.isRGB();
//      } catch (Exception ex) {
//         Log.log(ex);
//         rgb = false;
//      }
//      if (rgb) {
//         nChannels *= 3;
//      }


         // If theres a dummy contrast control placeholder here, remove it
         if (!channelName.equals(NDViewer.NO_CHANNEL) &&
                 getContrastControlKeys().contains(NDViewer.NO_CHANNEL)) {
            removeContrastControls(NDViewer.NO_CHANNEL);
         }


         DisplaySettings dispSettings = display_.getDisplaySettingsObject();
         //refresh display settings

         Color color;
         int bitDepth = 16;
         try {
            bitDepth = dispSettings.getBitDepth(channelName);
            color = dispSettings.getColor(channelName);
         } catch (Exception ex) {
            ex.printStackTrace();
            color = Color.white;
         }

         //create new channel control panels as needed
         ChannelControlPanel ccp = new ChannelControlPanel(display_, contrastPanel_, channelName, color, bitDepth);
         ccpList_.put(channelName, ccp);
         this.add(ccpList_.get(channelName));

         ((GridLayout) this.getLayout()).setRows(ccpList_.keySet().size());

         Dimension dim = new Dimension(ChannelControlPanel.MINIMUM_SIZE.width,
                 ccpList_.keySet().size() * ChannelControlPanel.MINIMUM_SIZE.height);
         this.setMinimumSize(dim);
         this.setSize(dim);
         this.setPreferredSize(dim);
         //Dunno if this is even needed
         contrastPanel_.revalidate();
      }
   }


   public void removeContrastControls(String channelName) {
      ChannelControlPanel c = ccpList_.remove(channelName);
      this.remove(c);
      contrastPanel_.revalidate();
   }

   public void autoscaleAllChannels() {
      if (ccpList_ != null && ccpList_.size() > 0) {
         for (ChannelControlPanel c : ccpList_.values()) {
            c.autoButtonAction();
         }
      }
   }

   public void rejectOutliersChangeAction() {
      if (ccpList_ != null && ccpList_.size() > 0) {
         for (ChannelControlPanel c : ccpList_.values()) {
            c.autoButtonAction();
         }
      }
   }

   public int getNumberOfChannels() {
      return ccpList_.size();
   }

   void updateHistogramData(HashMap<String, int[]> hists) {
      synchronized (ccpList_) {
         if (ccpList_ == null || hists == null || ccpList_.size() == 0) {
            return; // no channels added yet
         }
         if (ccpList_.keySet().size() != hists.keySet().size()) {
            // Still initializing new channel
            return;
         }
         for (String i : hists.keySet()) {
            ChannelControlPanel c = ccpList_.get(i);
            int[] hist = hists.get(i);
            c.updateHistogram(hist);
         }
      }
   }

   public List<String> getContrastControlKeys() {
      return new LinkedList<String>(ccpList_.keySet());
   }
   
//  Method added by @CellularPhysicalChemistryGroup - adds controls to the side of the histograms
//      for now should only be called once after all channels are added  
    public void addChannelSideControls(String text) {
        for (ChannelControlPanel c : ccpList_.values()) {
            c.addChannelSideControls(text);
        }
    }
//
   
}
