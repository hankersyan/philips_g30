package io.hankers.mdi.philips_g30;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.json.JSONObject;

import io.hankers.mdi.mdi_utils.MDILog;
import io.hankers.mdi.mdi_utils.MqttPublisher;
import io.hankers.mdi.mdi_utils.Models.Ubyte;
import io.hankers.mdi.mdi_utils.Models.Ushort;

public class Models {

	static final boolean BIGENDIAN = true;

	public static class MessageBase {
		Ubyte type;
		Ubyte length;
		long _timestamp = new Date().getTime() / 1000 * 1000;

		Ubyte indicator = new Ubyte();
		Ushort hr = new Ushort();
		Ubyte pulse = new Ubyte();
		Ubyte sys = new Ubyte();
		Ubyte dia = new Ubyte();
		Ubyte mean = new Ubyte();
		Ubyte rr = new Ubyte();
		String temp = null;
		Ubyte spo2 = new Ubyte();
		Ubyte etco2 = new Ubyte();
		Ubyte fico2 = new Ubyte();
		Ushort sys2 = new Ushort();
		Ushort dia2 = new Ushort();
		Ushort mean2 = new Ushort();

		public static MessageBase newInstance(byte[] buf, int offset, int length) {
			MessageBase ret = null;

			int type = buf[0];
			switch (type) {
			case 0x10:
				ret = new SingleECG();
				break;
			case 0x20:
				ret = new Nibp();
				break;
			case 0x30:
				ret = new SPO2();
				break;
			case 0x40:
				ret = new RESP();
				break;
			case 0x50:
				ret = new CO2();
				break;
			case 0x60:
				ret = new Ibp();
				break;
			default: {
				if (type >= 0x11 && type <= 0x1C) {
					ret = new MultipleECG();
				}
			}
				break;
			}

			if (ret == null) {
				return null;
			}

			InputStream ins = new ByteArrayInputStream(buf, offset, length);
			try {
				ret.read(ins);
			} catch (IOException e) {
				MDILog.e(e);
			}
			try {
				ins.close();
			} catch (IOException e) {
				MDILog.e(e);
			}
			return ret;
		}

		public void read(InputStream ins) throws IOException {
			type.read(ins);
			length.read(ins);

			readBody(ins);
		}

		void readBody(InputStream ins) throws IOException {

		}

		public void copyFrom(MessageBase msg) {
			if (msg.hr != null && msg.hr.value() > 0 && msg.hr.value() < 351) {
				hr = msg.hr;
			}
			if (msg.pulse != null && msg.pulse.value() > 0 && msg.pulse.value() < 351) {
				pulse = msg.pulse;
			}
			if (msg.sys != null && msg.sys.value() > 0 && msg.sys.value() < 351) {
				sys = msg.sys;
			}
			if (msg.dia != null && msg.dia.value() > 0 && msg.dia.value() < 351) {
				dia = msg.dia;
			}
			if (msg.mean != null && msg.mean.value() > 0 && msg.mean.value() < 351) {
				mean = msg.mean;
			}
			if (msg.sys2 != null && msg.sys2.value() > 0 && msg.sys2.value() < 351) {
				sys2 = msg.sys2;
			}
			if (msg.dia2 != null && msg.dia2.value() > 0 && msg.dia2.value() < 351) {
				dia2 = msg.dia2;
			}
			if (msg.mean2 != null && msg.mean2.value() > 0 && msg.mean2.value() < 351) {
				mean2 = msg.mean2;
			}
			if (msg.temp != null && !msg.temp.isEmpty()) {
				temp = msg.temp;
			}
			if (msg.spo2 != null && msg.spo2.value() > 0 && msg.spo2.value() < 101) {
				spo2 = msg.spo2;
			}
			if (msg.rr != null && msg.rr.value() > 0 && msg.rr.value() < 351) {
				rr = msg.rr;
			}
			if (msg.etco2 != null && msg.etco2.value() > 0 && msg.etco2.value() < 351) {
				mean2 = msg.mean2;
			}
			if (msg.fico2 != null && msg.fico2.value() > 0 && msg.fico2.value() < 351) {
				fico2 = msg.fico2;
			}
		}
		
		public void publish() {
			String content = this.toString();
			if (content != null && !content.isEmpty()) {
				MqttPublisher.addMessage(content);
			}
		}

		public String toString() {
			JSONObject json = new JSONObject();
			if (hr != null && hr.value() > 0 && hr.value() < 351) {
				json.put("HEART_BEAT", hr.value());
			}
			if (pulse != null && pulse.value() > 0 && pulse.value() < 351) {
				json.put("PULSE", pulse.value());
			}
			if (sys != null && sys.value() > 0 && sys.value() < 351) {
				json.put("NBP_SYS", sys.value());
			}
			if (dia != null && dia.value() > 0 && dia.value() < 351) {
				json.put("NBP_DIA", dia.value());
			}
			if (mean != null && mean.value() > 0 && mean.value() < 351) {
				json.put("NBP_MEAN", mean.value());
			}
			if (sys2 != null && sys2.value() > 0 && sys2.value() < 351) {
				json.put("IBP_SYS", sys2.value());
			}
			if (dia2 != null && dia2.value() > 0 && dia2.value() < 351) {
				json.put("IBP_DIA", dia2.value());
			}
			if (mean2 != null && mean2.value() > 0 && mean2.value() < 351) {
				json.put("IBP_MEAN", mean2.value());
			}
			if (temp != null && !temp.isEmpty()) {
				json.put("TEMP", temp);
			}
			if (spo2 != null && spo2.value() > 0 && spo2.value() < 101) {
				json.put("SPO2", spo2.value());
			}
			if (rr != null && rr.value() > 0 && rr.value() < 351) {
				json.put("RESP_RATE", rr.value());
			}
			if (etco2 != null && etco2.value() > 0 && etco2.value() < 351) {
				json.put("ETCO2", etco2.value());
			}
			if (fico2 != null && fico2.value() > 0 && fico2.value() < 351) {
				json.put("FICO2", fico2.value());
			}
			
			if (!json.isEmpty()) {
				if (_timestamp > 0) {
					json.put("timestamp", _timestamp);
				}
				return json.toString();
			}
			return null;
		}
	}

	public static class SingleECG extends MessageBase {

		@Override
		void readBody(InputStream ins) throws IOException {
			hr.read(ins, BIGENDIAN);
			if (!isValid(hr.value())) {

			}
		}

		boolean isValid(int val) {
			return val <= 350 && val >= 1;
		}
	}

	public static class MultipleECG extends MessageBase {

		@Override
		void readBody(InputStream ins) throws IOException {
			hr.read(ins, BIGENDIAN);
			indicator.read(ins);
			checkStatus();
		}

		void checkStatus() {
			// first bit
			if ((indicator.value() & 0x80) > 0) {
				MDILog.i("HearRate in multiple ecg NOT found");
				hr.setValue(0);
			}
		}

		boolean isValid(short val) {
			return val <= 350 && val >= 1;
		}
	}

	public static class Nibp extends MessageBase {
		Ubyte temp1 = new Ubyte();
		Ubyte temp2 = new Ubyte();

		@Override
		void readBody(InputStream ins) throws IOException {
			pulse.read(ins);
			sys.read(ins);
			dia.read(ins);
			mean.read(ins);
			temp1.read(ins);
			temp2.read(ins);
			rr.read(ins);

			if (temp1.value() > 0) {
				temp = String.format("{}.{}", temp1, temp2);
			}
		}
	}

	public static class Ibp extends MessageBase {
		Ubyte unknown = new Ubyte();

		@Override
		void readBody(InputStream ins) throws IOException {
			indicator.read(ins);
			unknown.read(ins);
			// first 4 bits
			if ((indicator.value() & 0xF0) == 0) {
				// IBP1
				sys2.read(ins, BIGENDIAN);
				dia2.read(ins, BIGENDIAN);
				mean2.read(ins, BIGENDIAN);
			} else if ((indicator.value() & 0x0F) == 0) {
				// IBP2
				ins.skip(6);
				sys2.read(ins, BIGENDIAN);
				dia2.read(ins, BIGENDIAN);
				mean2.read(ins, BIGENDIAN);
			}
		}
	}

	public static class SPO2 extends MessageBase {

		@Override
		void readBody(InputStream ins) throws IOException {
			pulse.read(ins);
			spo2.read(ins);
			indicator.read(ins);

			// first bit
			if ((indicator.value() & 0x80) > 0) {
				MDILog.i("checking out in spo2");
				pulse.setValue(0);
				spo2.setValue(0);
			}

			// second bit
			if ((indicator.value() & 0x40) > 0) {
				MDILog.i("electrode fall off");
				pulse.setValue(0);
				spo2.setValue(0);
			}
		}
	}

	public static class RESP extends MessageBase {
		Ubyte unknown = new Ubyte();

		@Override
		void readBody(InputStream ins) throws IOException {
			rr.read(ins);
			unknown.read(ins);
			indicator.read(ins);

			// first bit
			if ((indicator.value() & 0x80) > 0) {
				MDILog.i("electrode fall off");
				rr.setValue(0);
				unknown.setValue(0);
			}
		}
	}

	public static class CO2 extends MessageBase {
		Ubyte unknown = new Ubyte();

		@Override
		void readBody(InputStream ins) throws IOException {
			unknown.read(ins);
			etco2.read(ins);
			fico2.read(ins);
		}
	}

}
