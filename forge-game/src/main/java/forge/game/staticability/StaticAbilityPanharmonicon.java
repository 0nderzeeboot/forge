package forge.game.staticability;

import com.google.common.collect.ImmutableList;
import forge.game.ability.AbilityKey;
import forge.game.card.Card;
import forge.game.card.CardZoneTable;

import forge.game.spellability.SpellAbility;
import forge.game.trigger.Trigger;
import forge.game.trigger.TriggerType;
import forge.game.zone.ZoneType;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

public class StaticAbilityPanharmonicon {
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

        if (trigMode.equals(TriggerType.ChangesZone)) {
            // Cause of the trigger – the card changing zones
            if (!stAb.matchesValidParam("ValidCause", runParams.get(AbilityKey.Card))) {
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
            final String origin = stAb.hasParam("Origin") ? stAb.getParam("Origin") : null;
            final String destination = stAb.hasParam("Destination") ? stAb.getParam("Destination") : null;
            final CardZoneTable table = (CardZoneTable) runParams.get(AbilityKey.Cards);

            if (table.filterCards(origin == null ? null : ImmutableList.of(ZoneType.smartValueOf(origin)), ZoneType.smartValueOf(destination), stAb.getParam("ValidCause"), card, stAb).isEmpty()) {
                return false;
            }
        } else if (trigMode.equals(TriggerType.SpellCastOrCopy)
                || trigMode.equals(TriggerType.SpellCast) || trigMode.equals(TriggerType.SpellCopy)) {
            // Check if the spell cast and the caster match
            final SpellAbility sa = (SpellAbility) runParams.get(AbilityKey.CastSA);
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
