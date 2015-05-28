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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StringUtilities {

	public static String getUnicodeString(ByteBuffer buffer, boolean integer) {
		return getString(buffer, "UTF-16LE", integer);
	}

	public static String getAsciiString(ByteBuffer buffer, boolean integer) {
		return getString(buffer, "US-ASCII", integer);
	}

	private static String getString(ByteBuffer buffer, String charFormat, boolean integer) {
		String result;
		int length;
		
		if (charFormat.equals("UTF-16LE")) {
			if (integer) {
				length = buffer.order(ByteOrder.LITTLE_ENDIAN).getInt() * 2;
			} else {
				length = buffer.order(ByteOrder.LITTLE_ENDIAN).getInt();
			}
		} else {
			if (integer) {
				length = buffer.order(ByteOrder.LITTLE_ENDIAN).getInt();
			} else {
				length = buffer.order(ByteOrder.LITTLE_ENDIAN).getShort();
			}
		}
		
		int bufferPosition = buffer.position();
		
		try {
			result = new String(buffer.array(), bufferPosition, length, charFormat);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
		
		buffer.position(bufferPosition + length);
		
		return result;
	}
}
