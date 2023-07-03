/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.ndviewer.internal.gui;

import java.awt.geom.Point2D;
import java.util.HashMap;

import org.micromanager.ndviewer.api.NDViewerDataSource;

/**
 *
 * @author henrypinkard
 */
public class DataViewCoords {

   private volatile int overlayMode_;

   private double displayImageWidth_, displayImageHeight_; //resolution of the image to be displayed 
   private double sourceDataFullResWidth_, sourceDataFullResHeight_; //resolution in pixels of the display image at full res
   private double xView_, yView_; //top left pixel in full res coordinates
   private HashMap<String, Object> axes_ = new HashMap<String, Object>();
   private int resolutionIndex_;
   private NDViewerDataSource cache_;
   private boolean rgb_;
   private boolean sourceDataWidthInitialized_ = false;

   //Parameters that track what part of the dataset is being viewed
   public int xMax_, yMax_, xMin_, yMin_;

   public DataViewCoords(NDViewerDataSource cache, double xView, double yView,
                         Double initialWidth, Double initialHeight, int[] imageBounds, boolean rgb) {
      cache_ = cache;
      xView_ = 0;
      yView_ = 0;
      if (initialWidth == null) {
         sourceDataFullResWidth_ = 700;
         sourceDataFullResHeight_ = 700;
         sourceDataWidthInitialized_ = false;
      } else {
         sourceDataFullResWidth_ = initialWidth;
         sourceDataFullResHeight_ = initialHeight;
         sourceDataWidthInitialized_ = true;
      }
      rgb_ = rgb;
      setImageBounds(imageBounds);
   }

   public void setImageBounds(int[] bounds) {
      if (bounds != null) {
         if (bounds != null && !sourceDataWidthInitialized_ && bounds[0] != Integer.MIN_VALUE) {
            sourceDataFullResWidth_ = bounds[2] - bounds[0];
            sourceDataFullResHeight_ = bounds[3] - bounds[1];
            sourceDataWidthInitialized_ = true;
         }
         xMin_ = bounds[0];
         yMin_ = bounds[1];
         xMax_ = bounds[2];
         yMax_ = bounds[3];
      } else {
         xMin_ = Integer.MIN_VALUE;
         yMin_ = Integer.MIN_VALUE;
         xMax_ = Integer.MAX_VALUE;
         yMax_ = Integer.MAX_VALUE;
      }
   }

   /**
    *
    * @return
    */
   public Point2D.Double getSourceImageSizeAtResLevel() {
      return new Point2D.Double(sourceDataFullResWidth_ / getDownsampleFactor(), sourceDataFullResHeight_ / getDownsampleFactor());
   }

   public boolean isRGB() {
      return rgb_;
   }

   public void setFullResSourceDataSize(double newWidth, double newHeight) {
      sourceDataFullResWidth_ = newWidth;
      sourceDataFullResHeight_ = newHeight;
      updateResIndex();
   }

   public Point2D.Double getFullResSourceDataSize() {
      return new Point2D.Double(sourceDataFullResWidth_, sourceDataFullResHeight_);
   }

   public Point2D.Double getDisplayImageSize() {
      return new Point2D.Double(displayImageWidth_, displayImageHeight_);
   }

   /**
    * Computes the scaling between display pixels and whatever pixels they were
    * derived from
    */
   public double getMagnificationFromResLevel() {
      //need this floor because it happens along the way to image creation
      return displayImageWidth_ / Math.floor(sourceDataFullResWidth_ / getDownsampleFactor());
   }

   public double getMagnification() {
      return displayImageWidth_ / (double) sourceDataFullResWidth_;
   }

   private void updateResIndex() {
      double resIndexFloat = Math.log(sourceDataFullResWidth_ / (double) displayImageWidth_) / Math.log(2);
      resolutionIndex_ = (int) Math.max(0, Math.ceil(resIndexFloat));

      // Let the storage know the viewer will be requesting data at this resolution
      int currentMaxResIndex = cache_.getMaxResolutionIndex();
      if (resolutionIndex_ > currentMaxResIndex) {
         cache_.increaseMaxResolutionLevel(resolutionIndex_);
      }
   }

   /**
    * Compute the resolution index used for gettting data based on zoom and
    * available resolution indices
    *
    * @return
    */
   public int getResolutionIndex() {
      return resolutionIndex_;
   }

   public double getDownsampleFactor() {
      return Math.pow(2, getResolutionIndex());
   }

   public Point2D.Double getViewOffset() {
      return new Point2D.Double(xView_, yView_);
   }

   public void setViewOffset(double xOffset, double yOffset) {
      xView_ = xOffset;
      yView_ = yOffset;
   }

   public void setDisplayImageSize(int width, int height) {
      displayImageWidth_ = width;
      displayImageHeight_ = height;
      updateResIndex();
   }

   public void setAxisPosition(String axis, Object position) {
      axes_.put(axis, position);
   }

   public Object getAxisPosition(String axis) {
      if (!axes_.containsKey(axis)) {
         return 0;
      }
      return axes_.get(axis);
   }

   public DataViewCoords copy() {
      DataViewCoords view = new DataViewCoords(cache_, xView_, yView_,
              sourceDataFullResWidth_, sourceDataFullResHeight_, new int[]{xMin_, yMin_, xMax_, yMax_}, rgb_);
      for (String axisName : axes_.keySet()) {
         view.axes_.put(axisName, axes_.get(axisName));
      }
      view.displayImageHeight_ = displayImageHeight_;
      view.displayImageWidth_ = displayImageWidth_;
      view.xView_ = xView_;
      view.yView_ = yView_;
      view.rgb_ = rgb_;
      view.resolutionIndex_ = resolutionIndex_;
      view.overlayMode_ = overlayMode_;
      view.sourceDataWidthInitialized_ = sourceDataWidthInitialized_;
      return view;
   }

   public String getActiveChannel() {
      return axes_.get("channel") != null ? "" + axes_.get("channel") : "" ;
   }

   public HashMap<String, Object> getAxesPositions() {
      return axes_;
   }

   public void setActiveChannel(String channelName) {
      axes_.put("channel", channelName);
   }

   public int[] getBounds() {
      return new int[]{xMin_, yMin_, xMax_, yMax_};
   }

}
