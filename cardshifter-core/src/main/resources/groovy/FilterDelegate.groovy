import com.cardshifter.modapi.base.ComponentRetriever
import com.cardshifter.modapi.base.CreatureTypeComponent
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.cards.CardComponent
import com.cardshifter.modapi.cards.Cards
import com.cardshifter.modapi.phase.PhaseController
import com.cardshifter.modapi.players.Players
import net.zomis.cardshifter.ecs.effects.TargetFilter

import java.util.function.BiPredicate

class FilterDelegate {
    TargetFilter predicate = {Entity source, Entity target -> true}
    StringBuilder description = new StringBuilder()

    static FilterDelegate fromClosure(Closure filter) {
        FilterDelegate deleg = new FilterDelegate()
        filter.delegate = deleg
        filter.call()
        return deleg
    }

    List<Entity> findMatching(Entity source) {
        source.game.findEntities({Entity e ->
            predicate.test(source, e)
        })
    }

    private void addAnd() {
        if (description.length() > 0) {
            description.append(' ')
        }
    }

    def ownedBy(String owner) {
        addAnd()
        description.append("owned by $owner")
        predicate = predicate.and({Entity source, Entity target ->
            if (owner == 'you') {
                return Players.findOwnerFor(source) == Players.findOwnerFor(target)
            }
            if (owner == 'opponent') {
                return Players.findOwnerFor(source) == Players.findOwnerFor(target)
            }
            if (owner == 'current player') {
                return ComponentRetriever.singleton(source.game, PhaseController).currentEntity ==
                        Players.findOwnerFor(target)
            }
            if (owner == 'inactive player') {
                return ComponentRetriever.singleton(source.game, PhaseController).currentEntity !=
                        Players.findOwnerFor(target)
            }
            assert false : 'Unknown owner string: ' + owner
        })
    }

    def creatureType(String... type) {
        addAnd()
        description.append('creatures of type ' + String.join(' or ', type))
        predicate = predicate.and({Entity source, Entity target ->
            CreatureTypeComponent creatureType = target.getComponent(CreatureTypeComponent)
            if (creatureType) {
                return creatureType.getCreatureType() in type
            } else {
                return false
            }
        })
    }

    def zone(String... zone) {
        addAnd()
        description.append('on ' + String.join(' or ', zone))
        predicate = predicate.and({Entity source, Entity target ->
            CardComponent cardComponent = target.getComponent(CardComponent)
            Cards.isCard(target) && cardComponent.getCurrentZone() && cardComponent.getCurrentZone().getName() in zone
        })
    }

    def creature(boolean creature) {
        addAnd()
        description.append(creature ? 'creatures' : 'non-creatures')
        predicate = predicate.and({Entity source, Entity target ->
            CreatureTypeComponent creatureType = target.getComponent(CreatureTypeComponent)
            return creatureType != null
        })
    }

    def cardName(String... name) {
        addAnd()
        description.append('cards with name \'' + String.join("'/'", name) + '\'')
        predicate = predicate.and({Entity source, Entity target ->
            target.name in name
        })
    }

    def thisCard() {
        addAnd()
        description.append('this card')
        predicate = predicate.and({Entity source, Entity target ->
            source == target
        })
    }

}
