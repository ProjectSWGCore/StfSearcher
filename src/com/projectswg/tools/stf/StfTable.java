/*******************************************************************************
 * Part of NGECore2
 * Copyright (c) 2013 <Project SWG>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ******************************************************************************/
package com.projectswg.tools.stf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/*
 * Stf files don't appear to use the IFF format.
 * 
 * ID 0 will probably always be null because they start at 1.
 */
public class StfTable {
	
	private String[][] orderedTable;
	private List<Pair<String, String>> disorderedTable;

	public StfTable() {
	}

	public void readFile(String filePath) throws IOException {
		java.io.FileInputStream stf = new java.io.FileInputStream(filePath);

		byte[] buffer = new byte[stf.available()];
		stf.read(buffer);

		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.getInt(); // Size?
		byteBuffer.get(); // isMore?

		int arrayCount = byteBuffer.getInt();

		int rowCount = byteBuffer.getInt();

		orderedTable = new String[arrayCount][2];
		disorderedTable = new ArrayList<>();

		for (int i = 0; i < rowCount; i++) {
			int id = byteBuffer.getInt();
			byteBuffer.getInt();
			String value = "";
			value = StringUtilities.getUnicodeString(byteBuffer, true);
			orderedTable[id][0] = null;
			orderedTable[id][1] = value;
		}

		for (int i = 0; i < rowCount; i++) {
			int id = byteBuffer.getInt();
			String name = StringUtilities.getAsciiString(byteBuffer, true);
			orderedTable[id][0] = name;
			disorderedTable.add(new Pair<String, String>(name, orderedTable[id][1]));
		}

		stf.close();
	}

	public int getRowCount() {
		return ((orderedTable == null) ? 0 : orderedTable.length);
	}

	public int getColumnCount() {
		return ((orderedTable == null) ? 0 : 3);
	}

	/*
	 * @param id Iteration number
	 *
	 * @returns String's key-value pair from an alphanumeric list
	 */
	public Pair<String, String> getString(int id) {
		return disorderedTable.get(id);
	}
	
	/*
	 * @param id Identifying number of the string from the .stf file, unlike above
	 *
	 * @returns String's key-value pair from an unordered list
	 */
	public Pair<String, String> getStringById(int id) {
		return new Pair<String, String>(orderedTable[id][0], orderedTable[id][1]);
	}
	
	/*
	 * @param name Name of the string to return
	 *
	 * @returns The value for this key, or null if the key is not found
	 */
	public String getString(String name) {
		for (String[] columns : orderedTable) {
			if (columns[0].equals(name)) {
				return columns[1];
			}
		}

		return null;
	}

	public class Pair<K, V> {

		private K key;
		private V value;

		public Pair(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

	}
	
}
