package com.qutifc.implementations.writer.json.components;

import com.qutifc.interfaces.components.Reader;
import com.qutifc.interfaces.components.Record;
import com.qutifc.interfaces.components.Writer;
import com.qutifc.securitycontracts.annotations.MapsTo;

public class WriterJSON implements Writer {
	@MapsTo(FieldName = "r")
	private Reader reader;
	@MapsTo(FieldName = "t")
	private String text;

	@Override
	public void setReader(Reader reader) {
		this.reader = reader;
	}

	@Override
	public void write() {
		String result = "[";
		boolean hasAtLeastOneRecord = false;

		Record currentRecord = this.reader.getNext();
		while (currentRecord != null) {
			if (hasAtLeastOneRecord) {
				result = result + ",";
			}

			String recordResult = "{";
			String[] keys = currentRecord.getKeys();
			String[] values = currentRecord.getValues();

			int i = 0;
			int keyCount = currentRecord.getColumnCount();
			while (i < keyCount) {
				recordResult = recordResult + "\"" + keys[i] + "\"" + ":" + "\"" + values[i] + "\"";
				if (i < keyCount - 1) {
					recordResult = recordResult + ",";
				}

				i = i + 1;
			}

			recordResult = recordResult + "}";

			result = result + recordResult;
			hasAtLeastOneRecord = true;
			currentRecord = this.reader.getNext();
		}

		result = result + "]";

		this.text = result;
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public String type() {
		return "json";
	}
}
