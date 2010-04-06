/**
 * 
 */
package net.cafeto.entitycopy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author fospitia
 * 
 */
public class DeepCopy implements Serializable {

	private static final long serialVersionUID = 4435114234387228484L;

	/**
	 * 
	 */
	private DeepCopyType type = DeepCopyType.DEFAULT;

	/**
	 * 
	 */
	private Map<String, DeepCopy> deepCopies = new HashMap<String, DeepCopy>();

	/**
	 * 
	 */
	public DeepCopy() {
	}

	/**
	 * @param type
	 */
	public DeepCopy(DeepCopyType type) {
		this.type = type;
	}

	/**
	 * @return
	 */
	public DeepCopyType getType() {
		return type;
	}

	/**
	 * @param type
	 */
	public void setType(DeepCopyType type) {
		this.type = type;
	}

	/**
	 * @return
	 */
	public Map<String, DeepCopy> getDeepCopies() {
		return deepCopies;
	}

	/**
	 * @param deepCopies
	 */
	public void setDeepCopies(Map<String, DeepCopy> deepCopies) {
		this.deepCopies = deepCopies;
	}

	/**
	 * @param name
	 * @param deepCopy
	 * @return
	 */
	public DeepCopy add(String name, DeepCopy deepCopy) {
		int endIndex = name.indexOf('.');
		if (endIndex == -1) {
			getDeepCopies().put(name, deepCopy);
			return this;
		}

		DeepCopy deep = null;
		String key = name.substring(0, endIndex);
		if (getDeepCopies().containsKey(key)) {
			deep = getDeepCopies().get(key);
		} else {
			deep = createDefault();
			getDeepCopies().put(key, deep);
		}
		String rest = name.substring(endIndex + 1);
		deep.add(rest, deepCopy);
		return this;
	}

	/**
	 * @return
	 */
	public static DeepCopy createNone() {
		return new DeepCopy(DeepCopyType.NONE);
	}

	/**
	 * @return
	 */
	public static DeepCopy createDefault() {
		return new DeepCopy(DeepCopyType.DEFAULT);
	}

	/**
	 * @return
	 */
	public static DeepCopy createFull() {
		return new DeepCopy(DeepCopyType.FULL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("Type: " + type);
		for (Entry<String, DeepCopy> entry : deepCopies.entrySet()) {
			buffer.append("\nKey: " + entry.getKey());
			buffer.append(" Type: " + entry.getValue().getType());
			if (entry.getValue().getDeepCopies().size() > 0) {
				buffer.append("\nDeepCopies: \n" + entry.getValue());
			}
		}
		return buffer.toString();
	}
}
