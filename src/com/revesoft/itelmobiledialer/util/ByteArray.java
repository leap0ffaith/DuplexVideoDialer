package com.revesoft.itelmobiledialer.util;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ByteArray implements Serializable {

	public static final int DEFAULT_CAPACITY = 100;
	public byte[] arr;
	public int offset, length;
	private int[] hashBuffer;
	private boolean readOnly;

	public ByteArray(byte[] p_arr, int p_offset, int p_length) {
		if (p_arr == null || p_offset < 0 || p_offset + p_length > p_arr.length) {
			throw new IllegalArgumentException("Invalid Argument");
		}
		this.arr = p_arr;
		this.offset = p_offset;
		this.length = p_length;
		readOnly = true;
	}


	public ByteArray(byte[] p_arr) {
		if (p_arr == null) {
			this.arr = new byte[DEFAULT_CAPACITY];
			offset = length = 0;
			readOnly = false;
		} else {
			this.arr = p_arr;
			offset = 0;
			length = p_arr.length;
			readOnly = true;
		}
	}

	public ByteArray(int capacity) {
		arr = new byte[capacity];
		offset = length = 0;
		readOnly = false;
	}

	public ByteArray() {
		this(DEFAULT_CAPACITY);
	}

	public ByteArray(String str) {
		arr = str.getBytes();
		offset = 0;
		length = arr.length;
		readOnly = true;
	}

	public ByteArray(ByteArray b) {
		this(b.length);
		copy(b);
	}

	public ByteArray copy(ByteArray b) {
		offset = length = 0;
		readOnly = false;
		return append(b);
	}

	public ByteArray copy(byte[] b) {
		offset = length = 0;
		readOnly = false;
		return append(b);
	}

	public void reset() {
		offset = length = 0;
		readOnly = false;
	}

	public synchronized int hashCode() {
		if (hashBuffer == null)
			hashBuffer = new int[4];
		for (int i = 0; i < hashBuffer.length; i++)
			hashBuffer[i] = 0;
		for (int i = 0; i < length; i++) {
			hashBuffer[i & 0x3] += arr[i + offset];
		}
		return hashBuffer[3] << 24 | hashBuffer[2] << 16 | hashBuffer[1] << 8
				| hashBuffer[0];
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		ByteArray other = (ByteArray) obj;

		if (length != other.length) {
			return false;
		}

		for (int i = 0; i < length; i++) {
			if (arr[offset + i] != other.arr[other.offset + i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return new String(arr, offset, length);
	}

	public ByteArray append(byte[] a) {
		return a == null ? this : append(a, 0, a.length);
	}

	public ByteArray appendIPAddress(byte[] a) {
		if (a == null)
			return this;
		int i = 0, x;
		for (; i < a.length - 1; i++) {
			x = a[i] & 0x00FF;
			append(x);
			append('.');
		}
		x = a[i] & 0x00FF;
		append(x);

		return this;
	}

	public ByteArray append(byte[] a, int p_offset, int p_length) {
		if (a == null || p_offset < 0 || p_length < 0
				|| p_offset + p_length > a.length)
			return this;
		if (readOnly || this.length + p_length > arr.length - this.offset)
			throw new IllegalArgumentException("Auto size increment disabled");

		System.arraycopy(a, p_offset, arr, this.offset + this.length, p_length);
		this.length += p_length;
		return this;
	}

	public ByteArray append(ByteArray p) {

		ByteArray a = (ByteArray) p;
		return a == null ? this : append(a.arr, a.offset, a.length);

	}

	public ByteArray append(char a) {
		if (readOnly || this.length + 1 > arr.length - this.offset) {
			throw new IllegalArgumentException("Auto size increment disabled");
		}
		arr[offset + length++] = (byte) a;
		return this;
	}

	public ByteArray append(char a, char b) {
		if (readOnly || this.length + 2 > arr.length - this.offset) {
			throw new IllegalArgumentException("Auto size increment disabled");
		}
		arr[offset + length++] = (byte) a;
		arr[offset + length++] = (byte) b;
		return this;

	}

	public ByteArray append(char a, char b, char c) {
		if (readOnly || this.length + 3 > arr.length - this.offset) {
			throw new IllegalArgumentException("Auto size increment disabled");
		}
		arr[offset + length++] = (byte) a;
		arr[offset + length++] = (byte) b;
		arr[offset + length++] = (byte) c;
		return this;

	}

	public ByteArray append(char a, char b, char c, char d) {
		if (readOnly || this.length + 4 > arr.length - this.offset) {
			throw new IllegalArgumentException("Auto size increment disabled");
		}
		arr[offset + length++] = (byte) a;
		arr[offset + length++] = (byte) b;
		arr[offset + length++] = (byte) c;
		arr[offset + length++] = (byte) d;

		return this;

	}

	public ByteArray append(int n) {
		byte[] conLength = Integer.toString(n).getBytes();
		return append(conLength);
	}
	
	public ByteArray append(double n) {
		byte[] conLength = Double.toString(n).getBytes();
		return append(conLength);
	}

	public int getLength() {
		return length;
	}

	public boolean isEmpty() {
		return getLength() == 0;
	}

	public byte[] getBytes() {
		byte[] b = new byte[length];
		System.arraycopy(arr, offset, b, 0, length);
		return b;
	}

	public ByteArray prepend(ByteArray b) {
		if (b.length == 0)
			return this;
		if (readOnly || this.length + b.length > arr.length - this.offset)
			throw new IllegalArgumentException("Auto size increment disabled");
		System.arraycopy(arr, offset, arr, offset + b.length, length);
		System.arraycopy(b.arr, b.offset, arr, offset, b.length);
		length += b.length;
		return this;
	}

	public ByteArray appendByte(int i) {
		return append((char) i);
	}

	public ByteArray append(String string) {
		return this.append(string.getBytes());
	}

	public byte endCharacter() {
		return arr[offset + length - 1];
	}

	public byte startCharacter() {
		return arr[offset];
	}

	public ByteArray readOnlyCopy() {
		ByteArray b = new ByteArray(arr, offset, length);
		b.readOnly = true;
		return b;
	}

	public ByteArray increment() {
		offset++;
		length--;
		return this;
	}

	public byte getCharacterAt(int index) {
		if (index >= length)
			return 0;
		return arr[offset + index];
	}

	public ByteArray append(ByteArray b, int length) {
		if (length < 0)
			length = b.length;
		return append(b.arr, b.offset, length);
	}

	public boolean getStrUptoSlashR(String searchStr, ByteArray buffer,
			int maxLength) {
		return getStrUptoSlashR(new ByteArray(searchStr), buffer, maxLength);
	}

	public boolean getStrUptoSlashR(ByteArray searchStr, ByteArray buffer,
			int maxLength) {
		int i = 0;
		int temp = iTelStrstr(searchStr);
		if (temp == -1) {
			buffer.length = 0;
			return false;
		}
		for (i = 0; i < maxLength; i++) {
			if (offset + i + temp >= length || arr[offset + i + temp] == '\r') {
				return true;
			}
			buffer.appendByte(arr[offset + i + temp]);
		}
		return false;
	}

	public boolean getStrValueUptoSlashR(String searchStr, ByteArray buffer,
			int maxLength) {
		return getStrValueUptoSlashR(new ByteArray(searchStr), buffer,
				maxLength);
	}
	
	public boolean getStrValueUptoSemiColon(String searchStr, ByteArray buffer,
			int maxLength) {
		return getStrValueUptoSemicolon(new ByteArray(searchStr), buffer,
				maxLength);
	}

	public boolean getStrValueUptoSlashR(ByteArray searchStr, ByteArray buffer,
			int maxLength) {
		int i = 0;
		int temp = iTelStrstr(searchStr);
		if (temp == -1) {
			buffer.length = 0;
			return false;
		}
		temp += searchStr.length;
		for (i = 0; i < maxLength; i++) {
			if (offset + i + temp >= length || arr[offset + i + temp] == '\r') {
				return true;
			}
			buffer.appendByte(arr[offset + i + temp]);
		}
		return false;
	}
	
	public boolean getStrValueUptoSemicolon(ByteArray searchStr, ByteArray buffer,
			int maxLength) {
		int i = 0;
		int temp = iTelStrstr(searchStr);
		if (temp == -1) {
			buffer.length = 0;
			return false;
		}
		temp += searchStr.length;
		for (i = 0; i < maxLength; i++) {
			if (offset + i + temp >= length || arr[offset + i + temp] == ';') {
				return true;
			}
			buffer.appendByte(arr[offset + i + temp]);
		}
		return false;
	}

	public int iTelStrstr(String searchStr) {
		return iTelStrstr(new ByteArray(searchStr));
	}

	public int iTelStrstr(ByteArray searchStr) {
		byte[] searchStrArr = searchStr.arr;
		int searchStrOffset = searchStr.offset;
		int searchStrlength = searchStr.length;

		int i, j;
		for (i = 0, j = 0; i < length; i++) {
			byte a = arr[offset + i];
			byte b = searchStrArr[searchStrOffset + j];

			if (a >= 'A' && a <= 'Z')
				a = (byte) (a + 32);
			if (b >= 'A' && b <= 'Z')
				b = (byte) (b + 32);

			if (a == b) {
				int oldI = i;
				for (; i < length && searchStrOffset + j < searchStrlength; i++, j++) {
					a = arr[offset + i];
					b = searchStrArr[searchStrOffset + j];
					if (a >= 'A' && a <= 'Z')
						a = (byte) (a + 32);
					if (b >= 'A' && b <= 'Z')
						b = (byte) (b + 32);

					if (a != b)
						break;
				}
				if (searchStrOffset + j >= searchStrlength) {
					return oldI;
				}

				i = oldI;
				j = 0;
			}
		}
		return -1;
	}

	public boolean startsWith(String startStr) {
		return startsWith(startStr.getBytes());
	}

	public boolean startsWith(byte[] startStr) {
		int i = 0;
		while (i < length && i < startStr.length) {
			byte a = arr[offset + i];
			byte b = startStr[i];

			if (a >= 'A' && a <= 'Z')
				a = (byte) (a + 32);
			if (b >= 'A' && b <= 'Z')
				b = (byte) (b + 32);

			if (a != b)
				return false;
			i++;
		}
		if (i == length && i != startStr.length)
			return false;
		return true;
	}

	public void forward(int length) {
		if (length > this.length)
			throw new IllegalArgumentException("Forward length too big");
		offset += length;
		this.length -= length;
	}

	public byte charAt(int i) {
		if (i >= this.length)
			throw new ArrayIndexOutOfBoundsException("Index too big");
		return arr[offset + i];
	}
}
