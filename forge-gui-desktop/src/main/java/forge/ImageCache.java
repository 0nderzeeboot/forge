/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;
import com.mortennobel.imagescaling.ResampleOp;

import forge.assets.FSkinProp;
import forge.card.CardRules;
import forge.game.card.CardView;
import forge.game.player.PlayerView;
import forge.item.InventoryItem;
import forge.model.FModel;
import forge.properties.ForgeConstants;
import forge.properties.ForgePreferences.FPref;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinIcon;
import forge.util.ImageUtil;

/**
 * This class stores ALL card images in a cache with soft values. this means
 * that the images may be collected when they are not needed any more, but will
 * be kept as long as possible.
 * <p/>
 * The keys are the following:
 * <ul>
 * <li>Keys start with the file name, extension is skipped</li>
 * <li>The key without suffix belongs to the unmodified image from the file</li>
 * </ul>
 * 
 * @author Forge
 * @version $Id: ImageCache.java 25093 2014-03-08 05:36:37Z drdev $
 */
public class ImageCache {
    // short prefixes to save memory

    private static final Set<String> _missingIconKeys = new HashSet<String>();
    private static final LoadingCache<String, BufferedImage> _CACHE = CacheBuilder.newBuilder().softValues().build(new ImageLoader());
    /**Default image for cards.*/
    private static final BufferedImage _defaultImage;
    /**Default image for artifact cards.*/
    private static final BufferedImage _defaultImageA;
    /**Default image for black cards.*/
    private static final BufferedImage _defaultImageB;
    /**Default image for colorless cards.*/
    private static final BufferedImage _defaultImageC;
    /**Default image for green cards.*/
    private static final BufferedImage _defaultImageG;
    /**Default image for land cards.*/
    private static final BufferedImage _defaultImageL;
    /**Default image for multicolored cards.*/
    private static final BufferedImage _defaultImageM;
    /**Default image for red cards.*/
    private static final BufferedImage _defaultImageR;
    /**Default image for blue cards.*/
    private static final BufferedImage _defaultImageU;
    /**Default image for white cards.*/
    private static final BufferedImage _defaultImageW;
    
    /**Buffer for default card image.*/
    static {
        BufferedImage defImage = null;
        try {
            defImage = ImageIO.read(new File(ForgeConstants.NO_CARD_FILE));
        } catch (Exception ex) {
            System.err.println("could not load default card image");
        } finally {
            _defaultImage = (null == defImage) ? new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB) : defImage; 
        }
    }

    /**Buffer for default artifact card image.*/
    static {
        BufferedImage defImage = null;
        try {
            defImage = ImageIO.read(new File(ForgeConstants.NO_CARD_FILE_A));
        } catch (Exception ex) {
            System.err.println("could not load default artifact card image");
        } finally {
            _defaultImageA = (null == defImage) ? new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB) : defImage; 
        }
    }

    /**Buffer for default black card image.*/
    static {
        BufferedImage defImage = null;
        try {
            defImage = ImageIO.read(new File(ForgeConstants.NO_CARD_FILE_B));
        } catch (Exception ex) {
            System.err.println("could not load default black card image");
        } finally {
            _defaultImageB = (null == defImage) ? new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB) : defImage; 
        }
    }

    /**Buffer for default colorless card image.*/
    static {
        BufferedImage defImage = null;
        try {
            defImage = ImageIO.read(new File(ForgeConstants.NO_CARD_FILE_C));
        } catch (Exception ex) {
            System.err.println("could not load default colorless card image");
        } finally {
            _defaultImageC = (null == defImage) ? new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB) : defImage; 
        }
    }

    /**Buffer for default green card image.*/
    static {
        BufferedImage defImage = null;
        try {
            defImage = ImageIO.read(new File(ForgeConstants.NO_CARD_FILE_G));
        } catch (Exception ex) {
            System.err.println("could not load default green card image");
        } finally {
            _defaultImageG = (null == defImage) ? new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB) : defImage; 
        }
    }

    /**Buffer for default land card image.*/
    static {
        BufferedImage defImage = null;
        try {
            defImage = ImageIO.read(new File(ForgeConstants.NO_CARD_FILE_L));
        } catch (Exception ex) {
            System.err.println("could not load default land card image");
        } finally {
            _defaultImageL = (null == defImage) ? new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB) : defImage; 
        }
    }

    /**Buffer for default multicolored card image.*/
    static {
        BufferedImage defImage = null;
        try {
            defImage = ImageIO.read(new File(ForgeConstants.NO_CARD_FILE_M));
        } catch (Exception ex) {
            System.err.println("could not load default multicolor card image");
        } finally {
            _defaultImageM = (null == defImage) ? new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB) : defImage; 
        }
    }

    /**Buffer for default red card image.*/
    static {
        BufferedImage defImage = null;
        try {
            defImage = ImageIO.read(new File(ForgeConstants.NO_CARD_FILE_R));
        } catch (Exception ex) {
            System.err.println("could not load default red card image");
        } finally {
            _defaultImageR = (null == defImage) ? new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB) : defImage; 
        }
    }

    /**Buffer for default blue card image.*/
    static {
        BufferedImage defImage = null;
        try {
            defImage = ImageIO.read(new File(ForgeConstants.NO_CARD_FILE_U));
        } catch (Exception ex) {
            System.err.println("could not load default blue card image");
        } finally {
            _defaultImageU = (null == defImage) ? new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB) : defImage; 
        }
    }

    /**Buffer for default white card image.*/
    static {
        BufferedImage defImage = null;
        try {
            defImage = ImageIO.read(new File(ForgeConstants.NO_CARD_FILE_W));
        } catch (Exception ex) {
            System.err.println("could not load default white card image");
        } finally {
            _defaultImageW = (null == defImage) ? new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB) : defImage; 
        }
    }

    public static void clear() {
        _CACHE.invalidateAll();
        _missingIconKeys.clear();
    }

    /**
     * retrieve an image from the cache.  returns null if the image is not found in the cache
     * and cannot be loaded from disk.  pass -1 for width and/or height to avoid resizing in that dimension.
     */
    public static BufferedImage getImage(final CardView card, final Iterable<PlayerView> viewers, final int width, final int height) {
        final String key = card.getCurrentState().getImageKey(viewers);
        return scaleImage(key, width, height, true);
    }

    /**
     * retrieve an image from the cache.  returns null if the image is not found in the cache
     * and cannot be loaded from disk.  pass -1 for width and/or height to avoid resizing in that dimension.
     * Same as getImage() but returns null if the image is not available, instead of a default image.
     */
    public static BufferedImage getImageNoDefault(final CardView card, final Iterable<PlayerView> viewers, final int width, final int height) {
        final String key = card.getCurrentState().getImageKey(viewers);
        return scaleImage(key, width, height, false);
    }

    /**
     * retrieve an image from the cache.  returns null if the image is not found in the cache
     * and cannot be loaded from disk.  pass -1 for width and/or height to avoid resizing in that dimension.
     */
    public static BufferedImage getImage(InventoryItem ii, int width, int height) {
        return scaleImage(ii.getImageKey(false), width, height, true);
    }

    /**
     * retrieve an icon from the cache.  returns the current skin's ICO_UNKNOWN if the icon image is not found
     * in the cache and cannot be loaded from disk.
     */
    public static SkinIcon getIcon(String imageKey) {
        final BufferedImage i;
        if (_missingIconKeys.contains(imageKey) ||
                null == (i = scaleImage(imageKey, -1, -1, false))) {
            _missingIconKeys.add(imageKey);
            return FSkin.getIcon(FSkinProp.ICO_UNKNOWN);
        }
        return new FSkin.UnskinnedIcon(i);
    }
    
    /**
     * This requests the original unscaled image from the cache for the given key.
     * If the image does not exist then it can return a default image if desired.
     * <p>
     * If the requested image is not present in the cache then it attempts to load
     * the image from file (slower) and then add it to the cache for fast future access. 
     * </p>
     */
    public static BufferedImage getOriginalImage(String imageKey, boolean useDefaultIfNotFound) {
        if (null == imageKey) { 
            if (useDefaultIfNotFound) return _defaultImage;
            return null;
        }
        
        boolean altState = imageKey.endsWith(ImageKeys.BACKFACE_POSTFIX);
        if(altState)
            imageKey = imageKey.substring(0, imageKey.length() - ImageKeys.BACKFACE_POSTFIX.length());
        if (imageKey.startsWith(ImageKeys.CARD_PREFIX)) {
            imageKey = ImageUtil.getImageKey(ImageUtil.getPaperCardFromImageKey(imageKey), altState, true);
            if (StringUtils.isBlank(imageKey)) { 
                return _defaultImage;
            }
        }

        // Load from file and add to cache if not found in cache initially. 
        BufferedImage original = getImage(imageKey);

        // No image file exists for the given key so optionally associate with
        // a default "not available" image, however do not add it to the cache,
        // as otherwise it's problematic to update if the real image gets fetched.
        if (original == null && useDefaultIfNotFound) { 
            System.out.println("No original for " + imageKey + ", using default.");
            //Currently doesn't fetch a separate default image for each side.
            if (imageKey.startsWith("t:")){//it's a token! Limited support here for now.
               if (imageKey.startsWith("t:w_")) return _defaultImageW;
               if (imageKey.startsWith("t:u_")) return _defaultImageU;
               if (imageKey.startsWith("t:b_")) return _defaultImageB;
               if (imageKey.startsWith("t:r_")) return _defaultImageR;
               if (imageKey.startsWith("t:g_")) return _defaultImageG;
               if (imageKey.startsWith("t:c_")) return _defaultImageC;
               return _defaultImage; 
            }
            original = getDefaultImage(StaticData.instance().getCommonCards().getCard(imageKey.substring(imageKey.indexOf("/")+1, imageKey.length()-5)).getRules());
        }

        return original;
    }
    
    /**Gets the default image for a card.*/
    private static BufferedImage getDefaultImage(CardRules rules) {
      if (rules.getType().isLand()) return _defaultImageL;
      if (rules.getType().isArtifact()) return _defaultImageA;
      if (rules.getColor().isMulticolor()) return _defaultImageM;
      if (rules.getColor().hasWhite()) return _defaultImageW;
      if (rules.getColor().hasBlue()) return _defaultImageU;
      if (rules.getColor().hasBlack()) return _defaultImageB;
      if (rules.getColor().hasRed()) return _defaultImageR;
      if (rules.getColor().hasGreen()) return _defaultImageG;
      if (rules.getColor().isColorless()) return _defaultImageC;
      return _defaultImage;
   }

    private static BufferedImage scaleImage(String key, final int width, final int height, boolean useDefaultImage) {
        if (StringUtils.isEmpty(key) || (3 > width && -1 != width) || (3 > height && -1 != height)) {
            // picture too small or key not defined; return a blank
            return null;
        }

        String resizedKey = String.format("%s#%dx%d", key, width, height);

        final BufferedImage cached = _CACHE.getIfPresent(resizedKey);
        if (null != cached) {
            //System.out.println("found cached image: " + resizedKey);
            return cached;
        }
        
        BufferedImage original = getOriginalImage(key, useDefaultImage);
        if (original == null) { return null; }

        if (original == _defaultImageL || original == _defaultImageA || original ==
               _defaultImageM || original == _defaultImageW || original == _defaultImageU ||
               original == _defaultImageB || original == _defaultImageR || original ==
               _defaultImageG || original == _defaultImage) {
            // Don't put the default image in the cache under the key for the card.
            // Instead, cache it under its own key, to avoid duplication of the
            // default image and to remove the need to invalidate the cache when
            // an image gets downloaded.
            resizedKey = String.format("__DEFAULT__#%dx%d", width, height);
            final BufferedImage cachedDefault = _CACHE.getIfPresent(resizedKey);
            if (null != cachedDefault) {
                return cachedDefault;
            }
        }
        
        // Calculate the scale required to best fit the image into the requested
        // (width x height) dimensions whilst retaining aspect ratio.
        double scaleX = (-1 == width ? 1 : (double)width / original.getWidth());
        double scaleY = (-1 == height? 1 : (double)height / original.getHeight());
        double bestFitScale = Math.min(scaleX, scaleY);
        if ((bestFitScale > 1) && !FModel.getPreferences().getPrefBoolean(FPref.UI_SCALE_LARGER)) {
            bestFitScale = 1;
        }

        BufferedImage result;
        if (1 == bestFitScale) { 
            result = original;
        } else {
            
            int destWidth  = (int)(original.getWidth()  * bestFitScale);
            int destHeight = (int)(original.getHeight() * bestFitScale);
                         
            ResampleOp resampler = new ResampleOp(destWidth, destHeight);
            result = resampler.filter(original, null);
        }
        
        //System.out.println("caching image: " + resizedKey);
        _CACHE.put(resizedKey, result);
        return result;
    }

    /**
     * Returns the Image corresponding to the key.
     */
    private static BufferedImage getImage(final String key) {
        FThreads.assertExecutedByEdt(true);
        try {
            return ImageCache._CACHE.get(key);
        } catch (final ExecutionException ex) {
            if (ex.getCause() instanceof NullPointerException) {
                return null;
            }
            ex.printStackTrace();
            return null;
        } catch (final InvalidCacheLoadException ex) {
            // should be when a card legitimately has no image
            return null;
        }
    }
}
