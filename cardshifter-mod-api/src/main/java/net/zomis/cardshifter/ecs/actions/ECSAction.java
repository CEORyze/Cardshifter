package net.zomis.cardshifter.ecs.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.zomis.cardshifter.ecs.base.Entity;

public class ECSAction {

	private final Entity owner;
	private final String name;

	private final Predicate<ECSAction> allowed;
	private final Consumer<ECSAction> perform;
	private final List<TargetSet> targetSets = new ArrayList<>();
	
	public ECSAction(Entity owner, String name, Predicate<ECSAction> allowed, Consumer<ECSAction> perform) {
		this.owner = owner;
		this.name = name;
		this.allowed = allowed;
		this.perform = perform;
	}
	
	public String getName() {
		return name;
	}
	
	public Entity getOwner() {
		return owner;
	}
	
	public ECSAction copy() {
		return new ECSAction(this.owner, this.name, this.allowed, this.perform);
	}

	public boolean perform(Entity performer) {
		if (!this.isAllowed(performer)) {
			return false;
		}
		if (!this.targetSets.stream().allMatch(targets -> targets.hasEnoughTargets())) {
			return false;
		}
		
		this.owner.getGame().executeEvent(new ActionPerformEvent(owner, this, performer), () -> this.perform.accept(this));
		this.targetSets.forEach(TargetSet::clearTargets);
		return true;
	}

	public boolean isAllowed(Entity performer) {
		ActionAllowedCheckEvent event = new ActionAllowedCheckEvent(owner, this, performer);
		if (!owner.getGame().getEvents().executePostEvent(event).isAllowed()) {
			return false;
		}
		return this.allowed.test(this);
	}

	public List<TargetSet> getTargetSets() {
		return new ArrayList<>(targetSets);
	}
	
	public ECSAction addTargetSet(int min, int max) {
		// TODO: Consider using an ECSAction builder and put `addTargetSet` there
		this.targetSets.add(new TargetSet(this, min, max));
		return this;
	}
	
	@Override
	public String toString() {
		return name + " for entity " + owner;
	}
	
}