package ashley.core;

import java.util.BitSet;

import ashley.utils.ObjectMap;

/**
 * A family represents a group of components. It is used to describe what entities a system
 * should process. 
 * 
 * Example: Family.getFamilyFor(PositionComponent.class, VelocityComponent.class)
 * 
 * Families can't be instantiate directly but must be accessed via Family.getFamilyFor(), this is
 * to avoid duplicate families that describe the same components.
 * 
 * @author Stefan Bachmann
 */
public class Family {
	private static final ObjectMap<String, Family> familiesByAllBits = new ObjectMap<String, Family>();
	private static final ObjectMap<String, Family> familiesByOneBits = new ObjectMap<String, Family>();

	
	private static int familyIndex = 0;
	
	/** A bitset used for quick comparison between families & entities */
	private BitSet allBits;
	private BitSet oneBits;
	private BitSet excludeBits;
	
	/** Each family has a unique index, used for bitmasking */
	private final int index;
	
	/** Private constructor, use static method Family.getFamilyFor() */
	private Family(BitSet allBits,BitSet oneBits, BitSet excludeBits){
		this.allBits = allBits;
		this.oneBits = oneBits;
		this.excludeBits = excludeBits;
		this.index = familyIndex++;
	}
	
	private Family(){
		this(new BitSet(), new BitSet(), new BitSet());
	}
	
	/**
	 * Returns this family's unique index
	 */
	public int getFamilyIndex(){
		return this.index;
	}
	
	/**
	 * Checks if the passed entity matches this family's requirements.
	 * @param entity The entity to check for matching
	 * @return Whether the entity matches or not
	 */
	public boolean matches(Entity entity){
		BitSet entityComponentBits = entity.getComponentBits();
		
		boolean matches = true;
		
		if(!allBits.isEmpty()) {
            for (int i = allBits.nextSetBit(0); i >= 0; i = allBits.nextSetBit(i+1)) {
                if(!entityComponentBits.get(i)) {
                    matches = false;
                    break;
                }
            }
        }
		
		// Check if the entity possesses ANY of the components in the oneBits.
        // If so, the system is interested.
        if(matches && !oneBits.isEmpty()) {
            matches = oneBits.intersects(entityComponentBits);
        }
        
        // Check if the entity possesses ANY of the exclusion components,
        // if it does then the system is not interested.
        if(matches && !excludeBits.isEmpty()) {
            matches = !excludeBits.intersects(entityComponentBits);
        }
        
        return matches;
	}
	
	
	@SafeVarargs
	public Family all(Class<? extends Component> ...componentTypes){
		allBits.clear();
		
		for(int i=0; i<componentTypes.length; i++){
			allBits.set(ComponentType.getIndexFor(componentTypes[i]));	
		}
		
		return this;
	}
	
	@SafeVarargs
	public Family one(Class<? extends Component> ...componentTypes){
		oneBits.clear();
		
		for(int i=0; i<componentTypes.length; i++){
			oneBits.set(ComponentType.getIndexFor(componentTypes[i]));	
		}
		
		return this;
	}
	
	@SafeVarargs
	public Family exclude(Class<? extends Component> ...componentTypes){
		excludeBits.clear();
		
		for(int i=0; i<componentTypes.length; i++){
			excludeBits.set(ComponentType.getIndexFor(componentTypes[i]));	
		}
		
		return this;
	}
	
	/**
	 * @deprecated use {@link Family#getFamilyForAll(Class...)} or {@link #getFamilyForOne(Class...)}
	 * </p>
	 * Returns a family with the passed componentTypes as a descriptor. Each set of component types will
	 * always return the same Family instance.
	 * @param componentTypes The components to describe the family
	 * @return The family
	 */
	@SafeVarargs
	public static Family getFamilyFor(Class<? extends Component> ...componentTypes){
		return getFamilyForAll(componentTypes);
	}
	
	/**
	 * Returns a family with the passed componentTypes as a descriptor. Each set of component types will
	 * always return the same Family instance.
	 * </p>
	 * 
	 * The Family is determined by ALL the the components
	 * @param componentTypes The components to describe the family
	 * @return The family
	 */
	@SafeVarargs
	public static Family getFamilyForAll(Class<? extends Component> ...componentTypes){
		BitSet bits = new BitSet();
		
		for(int i=0; i<componentTypes.length; i++){
			bits.set(ComponentType.getIndexFor(componentTypes[i]));	
		}
		
		String hash = bits.toString();
		Family family = familiesByAllBits.get(hash, null);
		if(family == null){
			family = new Family();
			family.allBits = bits;
			familiesByAllBits.put(hash, family);
		}
		
		return family;
	}

	@SafeVarargs
	public static Family getFamilyForOne(Class<? extends Component> ...componentTypes){
		BitSet bits = new BitSet();
		
		for(int i=0; i<componentTypes.length; i++){
			bits.set(ComponentType.getIndexFor(componentTypes[i]));	
		}
		
		String hash = bits.toString();
		Family family = familiesByOneBits.get(hash, null);
		if(family == null){
			family = new Family();
			family.oneBits = bits;
			familiesByOneBits.put(hash, family);
		}
		
		return family;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Family){
			if(obj == this)
				return true;
			else return allBits.equals(((Family)obj).allBits);
		}
		return false;
	}
}
