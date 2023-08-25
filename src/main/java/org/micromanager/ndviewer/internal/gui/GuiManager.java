package org.micromanager.ndviewer.internal.gui;

import mmcorej.org.json.JSONObject;
import org.micromanager.ndviewer.api.CanvasMouseListenerInterface;
import org.micromanager.ndviewer.api.ControlsPanelInterface;
import org.micromanager.ndviewer.api.OverlayerPlugin;
import org.micromanager.ndviewer.main.NDViewer;
import org.micromanager.ndviewer.overlay.Overlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class GuiManager {

   private DisplayWindow displayWindow_;

   private ImageMaker imageMaker_;
   private BaseOverlayer overlayer_;
   private Timer animationTimer_;
   private double animationFPS_ = 7;

   private NDViewer display_;

   //public GuiManager() {};
   
   public GuiManager(NDViewer ndViewer, boolean acquisition) {
      displayWindow_ = new DisplayWindow(ndViewer, !acquisition);

      overlayer_ = new BaseOverlayer(ndViewer);
      imageMaker_ = new ImageMaker(ndViewer, ndViewer.getDataSource());
      display_ = ndViewer;

   }

   public void onScrollersAdded() {
      displayWindow_.onScrollersAdded();
   }

   public void onCanvasResize(int w, int h) {
      if (displayWindow_ == null) {
         return; // during startup
      }
      displayWindow_.onCanvasResized(w, h);

   }

   public void setWindowTitle(String s) {
      if (displayWindow_ != null) {
         displayWindow_.setTitle(s);
      }
   }


   public boolean isScrollerAxisLocked(String axis) {
      return displayWindow_.isScrollerAxisLocked(axis);
   }

   public void onAnimationToggle(AxisScroller scoller, boolean animate) {
      if (animationTimer_ != null) {
         animationTimer_.stop();
      }
      if (animate) {
         animationTimer_ = new Timer((int) (1000 / animationFPS_), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               int newPos = (scoller.getPosition() + 1)
                       % (scoller.getMaximum() - scoller.getMinimum());
//               HashMap<String, Integer> posMap = new HashMap<String, Integer>();
               display_.setAxisPosition(scoller.getAxis(), newPos);
            }
         });
         animationTimer_.start();
      }
   }

   public ViewerCanvas getCanvas() {
      return displayWindow_.getCanvas();
   }

   public void superlockAllScrollers() {
      displayWindow_.superlockAllScrollers();
   }

   public void unlockAllScroller() {
      displayWindow_.unlockAllScrollers();
   }

   public void setAnimateFPS(double doubleValue) {
      animationFPS_ = doubleValue;
      if (animationTimer_ != null) {
         ActionListener action = animationTimer_.getActionListeners()[0];
         animationTimer_.stop();
         animationTimer_ = new Timer((int) (1000 / animationFPS_), action);
         animationTimer_.start();
      }
   }

   public void displayOverlay(Overlay overlay) {
      displayWindow_.displayOverlay(overlay);
   }

   public void showScaleBar(boolean selected) {
      overlayer_.setShowScaleBar(selected);
   }

   public boolean isCompositeMode() {
      return display_.getDisplayModel().isCompositeMode();
   }

   public void shutdown() {
      displayWindow_.onDisplayClose();

      imageMaker_.close();
      imageMaker_ = null;

      overlayer_.shutdown();
      overlayer_ = null;

      if (animationTimer_ != null) {
         animationTimer_.stop();
      }
      animationTimer_ = null;
      displayWindow_ = null;
   }

   public void displayNewImage(Image img, HashMap<String,int[]> hists, DataViewCoords view,
                               JSONObject imageMD, OverlayerPlugin overlayerPlugin) {
      displayWindow_.displayImage(img, hists, view);
      displayWindow_.setImageMetadata(imageMD);
      overlayer_.createOverlay(view, overlayerPlugin);
      displayWindow_.repaintCanvas();
   }

   public Image makeOrGetImage(DataViewCoords view) {
      return imageMaker_.makeOrGetImage(view);
   }

   public JSONObject getLatestTags() {
      return imageMaker_.getLatestTags();
   }

   public HashMap<String,int[]> getHistograms() {
      return imageMaker_.getHistograms();
   }

   public void expandDisplayedRangeToInclude(java.util.List<HashMap<String, Object>> newIamgeEvents, java.util.List<String> activeChannels) {
      if (displayWindow_ != null) {
         displayWindow_.expandDisplayedRangeToInclude(newIamgeEvents, activeChannels);
      }
   }

   public void addControlPanel(ControlsPanelInterface panel) {
      displayWindow_.addControlPanel(panel);
   }

   public void setCustomCanvasMouseListener(CanvasMouseListenerInterface m) {
      displayWindow_.setCustomCanvasMouseListener(m);
   }

   public void setShowZPosition(boolean selected) {
      overlayer_.setShowZPosition(selected);
   }

   public void setShowTimeLabel(boolean selected) {
      overlayer_.setShowTimeLabel(selected);
   }

   public void updateActiveChannelCheckboxes() {
      displayWindow_.updateActiveChannelCheckboxes();
   }

   public void addContrastControlsIfNeeded(String channelName) {
      displayWindow_.addContrastControlsIfNeeded(channelName);
   }

   public void readHistogramControlsStateFromGUI() {
      displayWindow_.readHistogramControlsStateFromGUI();

   }

   public void updateGUIFromDisplaySettings() {
      displayWindow_.updateActiveChannelCheckboxes();
   }

//  Method added by @CellularPhysicalChemistryGroup - adds controls to the side of the histograms
//      for now should only be called once after all channels are added  
    public void addChannelSideControls(String text) {
        displayWindow_.addChannelSideControls(text);
    }
//
}
