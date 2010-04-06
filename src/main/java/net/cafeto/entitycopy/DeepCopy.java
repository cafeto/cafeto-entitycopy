/**
 * Copyright (c) 2010, Fabio Ospitia Trujillo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the Cafeto.Net nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.cafeto.entitycopy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
}
