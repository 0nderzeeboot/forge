package forge.adventure.libgdxgui.itemmanager;

import forge.adventure.libgdxgui.Graphics;
import forge.adventure.libgdxgui.assets.FSkinColor;
import forge.adventure.libgdxgui.assets.FSkinFont;
import forge.adventure.libgdxgui.card.CardRenderer;
import forge.adventure.libgdxgui.card.CardZoom;
import forge.item.PaperCard;
import forge.adventure.libgdxgui.itemmanager.filters.*;
import forge.adventure.libgdxgui.toolbox.FList;
import forge.adventure.libgdxgui.toolbox.FList.CompactModeHandler;

import java.util.Map.Entry;

/** 
 * ItemManager for cards
 */
public class CardManager extends ItemManager<PaperCard> {
    public CardManager(boolean wantUnique0) {
        super(PaperCard.class, wantUnique0);
    }

    @Override
    protected void addDefaultFilters() {
        addDefaultFilters(this);
    }

    @Override
    protected TextSearchFilter<PaperCard> createSearchFilter() {
        return createSearchFilter(this);
    }

    @Override
    protected AdvancedSearchFilter<PaperCard> createAdvancedSearchFilter() {
        return createAdvancedSearchFilter(this);
    }

    protected void onCardLongPress(int index, Entry<PaperCard, Integer> value, float x, float y) {
        CardZoom.show(model.getOrderedList(), index, CardManager.this);
    }

    /* Static overrides shared with SpellShopManager*/

    public static void addDefaultFilters(final ItemManager<? super PaperCard> itemManager) {
        itemManager.addFilter(new CardColorFilter(itemManager));
        itemManager.addFilter(new CardFormatFilter(itemManager));
        itemManager.addFilter(new CardTypeFilter(itemManager));
    }

    public static TextSearchFilter<PaperCard> createSearchFilter(final ItemManager<? super PaperCard> itemManager) {
        return new CardSearchFilter(itemManager);
    }

    public static AdvancedSearchFilter<PaperCard> createAdvancedSearchFilter(final ItemManager<? super PaperCard> itemManager) {
        return new AdvancedSearchFilter<>(itemManager);
    }

    @Override
    public ItemRenderer getListItemRenderer(final CompactModeHandler compactModeHandler) {
        return new ItemRenderer() {
            @Override
            public float getItemHeight() {
                return CardRenderer.getCardListItemHeight(compactModeHandler.isCompactMode());
            }

            @Override
            public void drawValue(Graphics g, Entry<PaperCard, Integer> value, FSkinFont font, FSkinColor foreColor, FSkinColor backColor, boolean pressed, float x, float y, float w, float h) {
                CardRenderer.drawCardListItem(g, font, foreColor, value.getKey(), isInfinite() ? 0 : value.getValue(), getItemSuffix(value), x, y, w, h, compactModeHandler.isCompactMode());
            }

            @Override
            public boolean tap(Integer index, Entry<PaperCard, Integer> value, float x, float y, int count) {
                return CardRenderer.cardListItemTap(model.getOrderedList(), index, CardManager.this, x, y, count, compactModeHandler.isCompactMode());
            }

            @Override
            public boolean longPress(Integer index, Entry<PaperCard, Integer> value, float x, float y) {
                if (CardRenderer.cardListItemTap(model.getOrderedList(), index, CardManager.this, x, y, 1, compactModeHandler.isCompactMode())) {
                    return true; //avoid calling onCardLongPress if user long presses on card art
                }
                onCardLongPress(index, value, x, y);
                return true;
            }

            @Override
            public boolean allowPressEffect(FList<Entry<PaperCard, Integer>> list, float x, float y) {
                //only allow press effect if right of card art
                return x > CardRenderer.getCardListItemHeight(compactModeHandler.isCompactMode()) * CardRenderer.CARD_ART_RATIO;
            }
        };
    }
}
