**Cardshifter modding documentation**

---

#Card Library Guide - Effects

This guide will explain how to create custom effects for cards. We created an easy-to-use, flexible system that allows for creative effects to be applied to your mod. 

---

###On precise grammar...

It is important to note that the keywords and identifiers must be typed **exactly** as listed to be trustworthy of working. Misspelled words will not work at all. Capilatization must also be respected to ensure functionality.

---

##Resource effects

An effect generally takes this form for resource modification:

    trigger {
        action resource n [withPriority n] onCards {
            // filters
        }
    }

There are also summoning effects, but those will be covered separately. 

_Note that `priority` only applies to the `whilePresent` trigger and should be omitted for other triggers._

---

###Triggers

Various triggers are available for actions to be applied on. 

####`afterPlay`

- Works on all cards.
- Applies the nested effects after a card is played.

####`whilePresent`

- Only works on creature cards. 
- Applies the nested effects while the card is present on the Battlefield.

####`onEndOfTurn`

- Only works on creature cards. 
- Applies the nested effects at the end of each of the owner's turns.

####`onDeath`

- Only works on creature cards. 
- Applies the nested effects when the creature's health reaches 0 or less.

####`pick X atRandom`

- Works on all cards.
- This is a sub-trigger and picks `X` actions from the available list whenever the trigger is activated. 
- Note that the available actions list need to be enclosed in parentheses rather than curly brackets. 

Syntax:

    trigger {
        pick X atRandom (
            { action a },
            { action b },
            { action _n_ }
        )
    }

Example:

    afterPlay {
        pick 1 atRandom (
            { summon 1 of "Conscript" to "you" zone "Hand" },
            { heal 1 to 'you' }
        )
    }


---

##Resources

Many effects manipulate resources. Following is a list of the different resources. For a description of what each resource does, please see the `Card Library - Basics.md` guide.


###Important note

The name of the resource must always be `ALL_CAPS_WITH_UNDERSCORES` as this is what the game server is expecting. 

####Basic Resources

- `ATTACK`
- `HEALTH`
- `SICKNESS`
- `MANA_COST`
- `SCRAP`
- `SCRAP_COST`

####Behaviour-specific Resources

- `ATTACK_AVAILABLE`
- `DENY_COUNTERATTACK`
- `TAUNT`

---

##Actions

The primary resource actions are `change` and `set`. The important distinction is that you either _change value(s) by `n`_ from its current value, or that you _set value(s) to `n`_ regardless of their current value. Therefore, be careful to use the correct keyword, `change` or `set`, according to your intentions. 

####`change`

Syntax:

    trigger {
        change RESOURCE by n [withPriority n] onCards {
            // filters
        }
    }

Examples:

    // add one health to your cards on after play
    afterPlay {
        change HEALTH by 1 onCards {
            ownedBy 'you'
            zone 'Battlefield'
        }
    }
    // subtract two attack from opponent creatures while present
    whilePresent {
        change ATTACK by -2 withPriority 1 onCards {
            creature()
            ownedBy 'opponent'
            zone 'Battlefield'
        }
    }
    
####`set`

Syntax:

    trigger {
        set RESOURCE to n [withPriority n] onCards {
            // filters
        }
    }

Examples:

    // set opponent creature sickness to 2 (wait one turn) on death
    onDeath {
        set SICKNESS to 2 onCards {
            creature()
            ownedBy 'opponent'
            zone 'Battlefield'
        }
    }
    // make all your Bio creatures have 3 attack while present
    whilePresent {
        set ATTACK to 3 withPriority 1 onCards {
            creatureType "Bio"
            ownedBy 'you'
            zone 'Battlefield'
        }
    }


---

####`withPriority` 


This is only used with the `whilePresent` filter. It specifies in which order the actions are applied when the creature is present, i.e., when it enters play or upon a new turn while it is in play. Actions with the same priority are applied simultaneously. It can be any whole number but it is simpler to use `1, 2, 3` etc. The default value if not specified is `1`. 

---

##`onCards` Filters

- These are used to filter the effects to a particular set of targets. 
- A filter uses a number of keys such as `ownedBy`, `zone`, `creature()`, `creatureType` and `thisCard()`.
- A variety of filters are available for effects, and will be explained in detail below.

####`ownedBy`

This is a list of possible owners with descriptions. Note that owner values are String values, and therefore need to be contained in quotation marks. 

- `"you"`: Cards that you, as the player, own.
- `"opponent"`: Cards that your opponent owns. 
- `"next"`: Cards that are owned by the next player. Synonymous to `"opponent"` unless your mod supports more than 2 players. - `"active"`: Cards owned by the active player, in other words to the player whose turn it is.
- `"inactive"`: Opposite of `"active"`. 
- `"none"`: Cards owner by no player. There are no current game mechanics that use this. 

####`zone`

This is a list of possible zones with descriptions. Note that zone values are String values, and therefore need to be contained in quotation marks. 

- `"Battlefield"`: Creature cards that are currently in active play, i.e., in battle or on the battlefield. 
- `"Hand"`: Cards in a player's hand, not played yet.
- `"Discard"`: Cards which have been discarded from battle. Sometimes also referred to as graveyard. 
- `"Deck"`: Cards in a player's deck. 
- `"Exile"`: Not currently used. Cards which are exiled, which may vary depending on the mod implementation.  
- `"Cards"`: All cards loaded at game start. Not currently used as it is too meta.

####`creatureType`

A specific `creatureType` type, for example `"Mech"` or `"Bio"`. Affects all creature cards of that type. 

####`creature()`

Affects all creatures regardless of their type.

####`thisCard()`

Affects the card which has the effect itself and no other. 

---

##Player effects

These effects are targeted at affecting players directly. Please don't use those with the `whilePresent` trigger. 

####`drawCard`

Draw cards from top of deck to `Hand`.

Syntax

    trigger {
        drawCard 'you', n
    }

Examples

    onEndOfTurn {
        drawCard 'opponent', 1
    }
    afterPlay {
        drawCard 'you', 3
    }

####`heal`, `damage`

This heals or damages a target player.

Syntax:

    trigger {
        heal|damage n to 'owner'
    }

Examples:

    onDeath {
        damage 3 to 'opponent'
    }
    onEndOfTurn {
        heal 1 to 'you'
    }

---

##Summoning effects

Summoning effects create new entities of specific cards into a specified zone. This is particularly important for cards with the `token()` attribute as it is the only way to bring them into play. 

Syntax:

    trigger {
        summon n of "Card Name" to "ownedBy" zone "zone"
    }

It uses the same keywords for `ownedBy` and `zone` as the other card filters.

Examples:

    // create one Conscript in your hand when this is played
    afterPlay {
        summon 1 of "Conscript" to "you" zone "Hand"
    }
    // "Deathrattle" (Hearthstone) style effect
    onDeath {
        summon 1 of "The Chopper" to "you" zone "Battlefield"
    }
    