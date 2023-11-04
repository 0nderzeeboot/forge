package forge.game.staticability;

import com.google.common.collect.ImmutableList;

import forge.game.Game;
import forge.game.ability.AbilityKey;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardCollectionView;
import forge.game.card.CardZoneTable;

import forge.game.spellability.SpellAbility;
import forge.game.trigger.Trigger;
import forge.game.trigger.TriggerType;
import forge.game.zone.ZoneType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

public class StaticAbilityPanharmonicon {
    static String MODE = "Panharmonicon";

    public static int handlePanharmonicon(final Game game, final Trigger t, final Map<AbilityKey, Object> runParams) {
        int n = 0;

        if (t.isStatic() && t.getMode() != TriggerType.TapsForMana && t.getMode() != TriggerType.ManaAdded) {
            // exclude "helper" trigger
            return n;
        }

        // These effects say "abilities of objects trigger an additional time" which excludes Delayed Trigger
        // 603.2e
        if (t.getSpawningAbility() != null) {
            return n;
        }

        // "triggers only once" means it can't happen
        if (t.hasParam("ActivationLimit")) {
            // currently no other limits, so no further calculation needed
            return n;
        }

        CardCollectionView cardList = null;
        // if LTB look back
        if ((t.getMode() == TriggerType.ChangesZone || t.getMode() == TriggerType.ChangesZoneAll) && "Battlefield".equals(t.getParam("Origin"))) {
            if (runParams.containsKey(AbilityKey.LastStateBattlefield)) {
                cardList = (CardCollectionView) runParams.get(AbilityKey.LastStateBattlefield);
            }
            if (cardList == null) {
                cardList = game.getLastStateBattlefield();
            }
        } else {
            cardList = game.getCardsIn(ZoneType.STATIC_ABILITIES_SOURCE_ZONES);
        }

        // Checks only the battlefield, as those effects only work from there
        for (final Card ca : cardList) {
            for (final StaticAbility stAb : ca.getStaticAbilities()) {
                if (!stAb.checkConditions(MODE)) {
                    continue;
                }
                if (applyPanharmoniconAbility(stAb, t, runParams)) {
                    n++;
                }
            }
        }

        return n;
    }

    public static boolean applyPanharmoniconAbility(final StaticAbility stAb, final Trigger trigger, final Map<AbilityKey, Object> runParams) {
        final Card card = stAb.getHostCard();

        final TriggerType trigMode = trigger.getMode();

        // What card is the source of the trigger?
        if (!stAb.matchesValidParam("ValidCard", trigger.getHostCard())) {
            return false;
        }

        // Is our trigger's mode among the other modes?
        if (stAb.hasParam("ValidMode")) {
            if (!ArrayUtils.contains(stAb.getParam("ValidMode").split(","), trigMode.toString())) {
                return false;
            }
        }

        // outside of Room Entered abilities, Panharmonicon effects always talk about Permanents you control which means only Battlefield
        if (!trigMode.equals(TriggerType.RoomEntered)) {
            if (!trigger.getHostCard().isInZone(ZoneType.Battlefield)) {
                return false;
            }
        }

        if (trigMode.equals(TriggerType.ChangesZone)) {
            // Cause of the trigger – the card changing zones
            Card moved = (Card) runParams.get(AbilityKey.Card);
            if ("Battlefield".equals(trigger.getParam("Origin"))) {
                moved = (Card) runParams.get(AbilityKey.CardLKI);
            }
            if (!stAb.matchesValidParam("ValidCause", moved)) {
                return false;
            }
            if (!stAb.matchesValidParam("Origin", runParams.get(AbilityKey.Origin))) {
                return false;
            }
            if (!stAb.matchesValidParam("Destination", runParams.get(AbilityKey.Destination))) {
                return false;
            }
        } else if (trigMode.equals(TriggerType.ChangesZoneAll)) {
            // Check if the cards have a trigger at all
            final String origin = stAb.getParam("Origin");
            final String destination = stAb.getParam("Destination");
            // check if some causes were ignored
            CardZoneTable table = (CardZoneTable) runParams.get(AbilityKey.CardsFiltered);
            if (table == null) {
                table = (CardZoneTable) runParams.get(AbilityKey.Cards);
            }

            List<ZoneType> trigOrigin = null;
            ZoneType trigDestination = null;
            if (trigger.hasParam("Destination") && !trigger.getParam("Destination").equals("Any")) {
                trigDestination = ZoneType.valueOf(trigger.getParam("Destination"));
            }
            if (trigger.hasParam("Origin") && !trigger.getParam("Origin").equals("Any")) {
                trigOrigin = ZoneType.listValueOf(trigger.getParam("Origin"));
            }
            CardCollection causesForTrigger = table.filterCards(trigOrigin, trigDestination, trigger.getParam("ValidCards"), trigger.getHostCard(), trigger);

            CardCollection causesForStatic = table.filterCards(origin == null ? null : ImmutableList.of(ZoneType.smartValueOf(origin)), ZoneType.smartValueOf(destination), stAb.getParam("ValidCause"), card, stAb);

            // check that whatever caused the trigger to fire is also a cause the static applies for
            if (Collections.disjoint(causesForTrigger, causesForStatic)) {
                return false;
            }
        } else if (trigMode.equals(TriggerType.Attacks)) {
            if (!stAb.matchesValidParam("ValidCause", runParams.get(AbilityKey.Attacker))) {
                return false;
            }
        } else if (trigMode.equals(TriggerType.AttackersDeclared)
                || trigMode.equals(TriggerType.AttackersDeclaredOneTarget)) {
            if (!stAb.matchesValidParam("ValidCause", runParams.get(AbilityKey.Attackers))) {
                return false;
            }
        } else if (trigMode.equals(TriggerType.SpellCastOrCopy)
                || trigMode.equals(TriggerType.SpellCast) || trigMode.equals(TriggerType.SpellCopy)) {
            // Check if the spell cast and the caster match
            final SpellAbility sa = (SpellAbility) runParams.get(AbilityKey.SpellAbility);
            if (!stAb.matchesValidParam("ValidCause", sa.getHostCard())) {
                return false;
            }
            if (!stAb.matchesValidParam("ValidActivator", sa.getActivatingPlayer())) {
                return false;
            }
        }

        return true;
    }
}
