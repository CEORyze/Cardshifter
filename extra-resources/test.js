/**
 * Attempts to replicate the logic located in PhrancisGame.java
 * This module is to set up all the core game components required to start a game
 * Location of original Java file:
 *   cardshifter-core/src/main/java/net/zomis/cardshifter/ecs/usage/PhrancisGame.java 
 * @module PhrancisGame
 */

var ZoneComponent = Java.type("com.cardshifter.modapi.cards.ZoneComponent");
var PlayerComponent = Java.type("com.cardshifter.modapi.base.PlayerComponent");
var DeckConfigFactory = Java.type("net.zomis.cardshifter.ecs.config.DeckConfigFactory");
var ConfigComponent = Java.type("net.zomis.cardshifter.ecs.config.ConfigComponent");

/** 
 * Enum that represents various player resources:
 * MAX_HEALTH, SNIPER, DOUBLE_ATTACK, TAUNT, DENY_COUNTERATTACK, HEALTH, MANA, MANA_MAX, SCRAP, 
 * ATTACK, MANA_COST, SCRAP_COST, ENCHANTMENTS_ACTIVE, SICKNESS, ATTACK_AVAILABLE
 */
var PhrancisResources = Java.type("net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources");

var ECSResourceMap = Java.type("com.cardshifter.modapi.resources.ECSResourceMap");
var ECSAttributeMap = Java.type("com.cardshifter.modapi.attributes.ECSAttributeMap");
var baseMod = Java.type("net.zomis.cardshifter.ecs.usage.PhrancisGame");
var Attributes = Java.type("com.cardshifter.modapi.attributes.Attributes");

/**
 * Declare game configuration
 * @param {Object} game - Game configuration data
 */
function declareConfiguration(game) {
	var neutral = game.newEntity();
	var zone = new ZoneComponent(neutral, "Cards");
	neutral.addComponent(zone);
	addCards(zone);
	
	/** Parameters related to DeckConfigFactory */
	var maxCardsPerType = 3;
	var minSize = 30;
	var maxSize = 30;
	
	/**
	 * Create playerComponent 0 & 1, i.e., Player1 & Player2
	 * Config a deck for each player
	 */
	for (var i = 0; i < 2; i++) {
		var entity = game.newEntity();
		var playerComponent = new PlayerComponent(i, "Player" + (i+1));
		entity.addComponent(playerComponent);
		var config = DeckConfigFactory.create(minSize, maxSize, zone.getCards(), maxCardsPerType);
		entity.addComponent(new ConfigComponent().addConfig("Deck", config));
	}
}
