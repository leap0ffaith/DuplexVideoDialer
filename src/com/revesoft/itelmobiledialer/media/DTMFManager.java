package com.revesoft.itelmobiledialer.media;

public class DTMFManager {

	private volatile int dtmf = -1;
	
	public void sendDtmf(char dtmfCh) {
		if (dtmfCh == '*')
			setDtmf(10);// dtmf = 10;
		else if (dtmfCh == '#')
			setDtmf(11);// dtmf = 11;
		else
			setDtmf(dtmfCh - '0');// dtmf = dtmfCh - '0';
		// Log.i(tag, "Sending DTMF: " + getDtmf());
	}

	public int[] prepareNextDTMF(int dtmf, int count, int seqNo,
			byte[] rtpEvent) {
		// Log.i(tag, "prepareNextDTMF dtmf: " + dtmf + " count: " + count
		// + " seqNo: " + seqNo);
		switch (dtmf) {
		case 0:// DTMF 0 - 13 packets
			if (count > 13)
				return new int[] { 0, count, seqNo };
			switch (count) {
			case 1:
			case 2:
			case 3:
				rtpEvent[12] = 0x00;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = 0x40;
				break;
			case 4:
				seqNo++;
				rtpEvent[12] = 0x00;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = (byte) 0xe0;
				break;
			case 5:
				seqNo++;
				rtpEvent[12] = 0x00;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x02;
				rtpEvent[15] = (byte) 0x80;
				break;

			case 6:
				seqNo++;
				rtpEvent[12] = 0x00;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = 0x20;
				break;
			case 7:
				seqNo++;
				rtpEvent[12] = 0x00;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = (byte) 0xc0;
				break;
			case 8:
				seqNo++;
				rtpEvent[12] = 0x00;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = 0x60;
				break;
			case 9:
				seqNo++;
				rtpEvent[12] = 0x00;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x05;
				rtpEvent[15] = 0x00;
				break;
			case 10:
				seqNo++;
				rtpEvent[12] = 0x00;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x05;
				rtpEvent[15] = (byte) 0xa0;
				break;
			case 11:
				seqNo++;
			case 12:
			case 13:
				rtpEvent[12] = 0x00;
				rtpEvent[13] = (byte) 0x80;
				rtpEvent[14] = 0x06;
				rtpEvent[15] = 0x40;
				break;

			default:
				return new int[] { -1, count, seqNo };
			}
			break;

		case 1:// DTMF 1 - 11 packets
			if (count > 11)
				return new int[] { 0, count, seqNo };
			switch (count) {
			case 1:
			case 2:
			case 3:
				rtpEvent[12] = 0x01;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = 0x40;
				break;
			case 4:
				seqNo++;
				rtpEvent[12] = 0x01;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = (byte) 0xe0;
				break;
			case 5:
				seqNo++;
				rtpEvent[12] = 0x01;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x02;
				rtpEvent[15] = (byte) 0x80;
				break;
			case 6:
				seqNo++;
				rtpEvent[12] = 0x01;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = 0x20;

				break;
			case 7:
				seqNo++;
				rtpEvent[12] = 0x01;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = (byte) 0xc0;
				break;
			case 8:
				seqNo++;
				rtpEvent[12] = 0x01;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = 0x60;
				break;
			case 9:
				seqNo++;
			case 10:
			case 11:
				rtpEvent[12] = 0x01;
				rtpEvent[13] = (byte) 0x80;
				rtpEvent[14] = 0x05;
				rtpEvent[15] = 0x00;
				break;
			default:
				return new int[] { -1, count, seqNo };
			}
			break;

		case 2:// DTMF 2 - 11 packets
			if (count > 11)
				return new int[] { 0, count, seqNo };
			switch (count) {
			case 1:
			case 2:
			case 3:
				rtpEvent[12] = 0x02;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = 0x40;
				break;
			case 4:
				seqNo++;
				rtpEvent[12] = 0x02;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = (byte) 0xe0;
				break;
			case 5:
				seqNo++;
				rtpEvent[12] = 0x02;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x02;
				rtpEvent[15] = (byte) 0x80;
				break;

			case 6:
				seqNo++;
				rtpEvent[12] = 0x02;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = 0x20;

				break;
			case 7:
				seqNo++;
				rtpEvent[12] = 0x02;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = (byte) 0xc0;

				break;
			case 8:
				seqNo++;
				rtpEvent[12] = 0x02;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = 0x60;

				break;
			case 9:
				seqNo++;
			case 10:
			case 11:
				rtpEvent[12] = 0x02;
				rtpEvent[13] = (byte) 0x80;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = (byte) 0xb0;
				break;

			default:
				return new int[] { -1, count, seqNo };
			}
			break;
		case 3:// DTMF 3 - 12 packets
			if (count > 12)
				return new int[] { 0, count, seqNo };
			switch (count) {
			case 1:
			case 2:
			case 3:
				rtpEvent[12] = 0x03;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = 0x40;
				break;
			case 4:
				seqNo++;
				rtpEvent[12] = 0x03;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = (byte) 0xe0;
				break;
			case 5:
				seqNo++;
				rtpEvent[12] = 0x03;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x02;
				rtpEvent[15] = (byte) 0x80;
				break;
			case 6:
				seqNo++;
				rtpEvent[12] = 0x03;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = 0x20;
				break;
			case 7:
				seqNo++;
				rtpEvent[12] = 0x03;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = (byte) 0xc0;
				break;
			case 8:
				seqNo++;
				rtpEvent[12] = 0x03;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x05;
				rtpEvent[15] = 0x00;
				break;
			case 9:
				seqNo++;
				rtpEvent[12] = 0x03;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x05;
				rtpEvent[15] = 0x50;
				break;
			case 10:
				seqNo++;
			case 11:
			case 12:
				rtpEvent[12] = 0x03;
				rtpEvent[13] = (byte) 0x80;
				rtpEvent[14] = 0x05;
				rtpEvent[15] = 0x00;
				break;
			default:
				return new int[] { -1, count, seqNo };
			}
			break;

		case 4:// DTMF 4 - 11 packets
			if (count > 11)
				return new int[] { 0, count, seqNo };
			switch (count) {
			case 1:
			case 2:
			case 3:
				rtpEvent[12] = 0x04;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = 0x40;
				break;
			case 4:
				seqNo++;
				rtpEvent[12] = 0x04;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = (byte) 0xe0;
				break;
			case 5:
				seqNo++;
				rtpEvent[12] = 0x04;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x02;
				rtpEvent[15] = (byte) 0x80;
				break;

			case 6:
				seqNo++;
				rtpEvent[12] = 0x04;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = 0x20;
				break;
			case 7:
				seqNo++;
				rtpEvent[12] = 0x04;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = (byte) 0xc0;
				break;
			case 8:
				seqNo++;
				rtpEvent[12] = 0x04;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = 0x60;
				break;
			case 9:
				seqNo++;
			case 10:
			case 11:
				rtpEvent[12] = 0x04;
				rtpEvent[13] = (byte) 0x80;
				rtpEvent[14] = 0x05;
				rtpEvent[15] = 0x00;
				break;
			default:
				return new int[] { -1, count, seqNo };
			}
			break;
		case 5:// DTMF 5 - 11 packets
			if (count > 11)
				return new int[] { 0, count, seqNo };
			switch (count) {
			case 1:
			case 2:
			case 3:
				rtpEvent[12] = 0x05;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = 0x40;
				break;
			case 4:
				seqNo++;
				rtpEvent[12] = 0x05;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = (byte) 0xe0;
				break;
			case 5:
				seqNo++;
				rtpEvent[12] = 0x05;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x02;
				rtpEvent[15] = (byte) 0x80;
				break;

			case 6:
				seqNo++;
				rtpEvent[12] = 0x05;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = 0x20;
				break;
			case 7:
				seqNo++;
				rtpEvent[12] = 0x05;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = (byte) 0xc0;
				break;
			case 8:
				seqNo++;
				rtpEvent[12] = 0x05;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = 0x60;
				break;
			case 9:
				seqNo++;
			case 10:
			case 11:
				rtpEvent[12] = 0x05;
				rtpEvent[13] = (byte) 0x80;
				rtpEvent[14] = 0x05;
				rtpEvent[15] = 0x00;
				break;
			default:
				return new int[] { -1, count, seqNo };
			}
			break;

		case 6:// DTMF 6 - 10 packets
			if (count > 10)
				return new int[] { 0, count, seqNo };
			switch (count) {
			case 1:
			case 2:
			case 3:
				rtpEvent[12] = 0x06;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = 0x40;
				break;
			case 4:
				seqNo++;
				rtpEvent[12] = 0x06;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = (byte) 0xe0;

				break;
			case 5:
				seqNo++;
				rtpEvent[12] = 0x06;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x02;
				rtpEvent[15] = (byte) 0x80;

				break;

			case 6:
				seqNo++;
				rtpEvent[12] = 0x06;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = 0x20;

				break;
			case 7:
				seqNo++;
				rtpEvent[12] = 0x06;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = (byte) 0xc0;

				break;
			case 8:
				seqNo++;
			case 9:
			case 10:
				rtpEvent[12] = 0x06;
				rtpEvent[13] = (byte) 0x80;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = 0x10;
				break;
			default:
				return new int[] { -1, count, seqNo };
			}
			break;
		case 7:// DTMF 7 - 10 packets
			if (count > 10)
				return new int[] { 0, count, seqNo };
			switch (count) {
			case 1:
			case 2:
			case 3:
				rtpEvent[12] = 0x07;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = 0x40;
				break;
			case 4:
				seqNo++;
				rtpEvent[12] = 0x07;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = (byte) 0xe0;
				break;
			case 5:
				seqNo++;
				rtpEvent[12] = 0x07;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x02;
				rtpEvent[15] = (byte) 0x80;
				break;

			case 6:
				seqNo++;
				rtpEvent[12] = 0x07;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = 0x20;
				break;
			case 7:
				seqNo++;
				rtpEvent[12] = 0x07;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = (byte) 0xc0;
				break;
			case 8:
				seqNo++;
			case 9:
			case 10:
				rtpEvent[12] = 0x07;
				rtpEvent[13] = (byte) 0x80;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = 0x10;
				break;
			default:
				return new int[] { -1, count, seqNo };
			}
			break;
		case 8:// DTMF 8 - 10 packets
			if (count > 10)
				return new int[] { 0, count, seqNo };
			switch (count) {
			case 1:
			case 2:
			case 3:
				rtpEvent[12] = 0x08;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = 0x40;

				break;
			case 4:
				seqNo++;
				rtpEvent[12] = 0x08;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = (byte) 0xe0;

				break;
			case 5:
				seqNo++;
				rtpEvent[12] = 0x08;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x02;
				rtpEvent[15] = (byte) 0x80;
				break;

			case 6:
				seqNo++;
				rtpEvent[12] = 0x08;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = 0x20;

				break;
			case 7:
				seqNo++;
				rtpEvent[12] = 0x08;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = (byte) 0xc0;

				break;
			case 8:
				seqNo++;
			case 9:
			case 10:
				rtpEvent[12] = 0x08;
				rtpEvent[13] = (byte) 0x80;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = 0x60;
				break;
			default:
				return new int[] { -1, count, seqNo };
			}
			break;
		case 9:// DTMF 9 - 10 packets
			if (count > 10)
				return new int[] { 0, count, seqNo };
			switch (count) {
			case 1:
			case 2:
			case 3:
				rtpEvent[12] = 0x09;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = 0x40;
				break;
			case 4:
				seqNo++;
				rtpEvent[12] = 0x09;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = (byte) 0xe0;
				break;
			case 5:
				seqNo++;
				rtpEvent[12] = 0x09;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x02;
				rtpEvent[15] = (byte) 0x80;
				break;

			case 6:
				seqNo++;
				rtpEvent[12] = 0x09;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = 0x20;
				break;
			case 7:
				seqNo++;
				rtpEvent[12] = 0x09;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = (byte) 0xc0;
				break;
			case 8:
				seqNo++;
			case 9:
			case 10:
				rtpEvent[12] = 0x09;
				rtpEvent[13] = (byte) 0x80;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = 0x10;
				break;
			default:
				return new int[] { -1, count, seqNo };
			}
			break;
		case 10:// DTMF * - 11 packets
			if (count > 11)
				return new int[] { 0, count, seqNo };
			switch (count) {
			case 1:
			case 2:
			case 3:
				rtpEvent[12] = 0x0a;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = 0x40;
				break;
			case 4:
				seqNo++;
				;
				rtpEvent[12] = 0x0a;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = (byte) 0xe0;
				break;
			case 5:
				seqNo++;
				rtpEvent[12] = 0x0a;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x02;
				rtpEvent[15] = (byte) 0x80;
				break;
			case 6:
				seqNo++;
				rtpEvent[12] = 0x0a;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = 0x20;
				break;
			case 7:
				seqNo++;
				rtpEvent[12] = 0x0a;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = (byte) 0xc0;
				break;
			case 8:
				seqNo++;
				rtpEvent[12] = 0x0a;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = 0x60;
				break;
			case 9:
				seqNo++;
			case 10:
			case 11:
				rtpEvent[12] = 0x0a;
				rtpEvent[13] = (byte) 0x80;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = (byte) 0xb0;
				break;

			default:
				return new int[] { -1, count, seqNo };
			}
			break;
		case 11:// DTMF # - 11 packets
			if (count > 11)
				return new int[] { 0, count, seqNo };
			switch (count) {
			case 1:
			case 2:
			case 3:
				rtpEvent[12] = 0x0b;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = 0x40;
				break;
			case 4:
				seqNo++;
				rtpEvent[12] = 0x0b;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x01;
				rtpEvent[15] = (byte) 0xe0;
				break;
			case 5:
				seqNo++;
				rtpEvent[12] = 0x0b;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x02;
				rtpEvent[15] = (byte) 0x80;
				break;

			case 6:
				seqNo++;
				rtpEvent[12] = 0x0b;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = 0x20;
				break;
			case 7:
				seqNo++;
				rtpEvent[12] = 0x0b;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x03;
				rtpEvent[15] = (byte) 0xc0;

				break;
			case 8:
				seqNo++;
				rtpEvent[12] = 0x0b;
				rtpEvent[13] = 0x00;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = 0x60;

				break;
			case 9:
				seqNo++;
			case 10:
			case 11:
				rtpEvent[12] = 0x0b;
				rtpEvent[13] = (byte) 0x80;
				rtpEvent[14] = 0x04;
				rtpEvent[15] = (byte) 0xb0;

				break;
			default:
				return new int[] { -1, count, seqNo };
			}
			break;
		}
		count++;
		return new int[] { 1, count, seqNo };// means there are more packet of
		// this dtmf
	}

	public synchronized void setDtmf(int d) {
		dtmf = d;
		// Log.i(tag,"the dtmf is set to "+d);
	}

	public synchronized int getDtmf() {
		return dtmf;
	}
}
