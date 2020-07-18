package com.qutifc.implementations.writer.csv.components;

import com.qutifc.interfaces.components.Reader;
import com.qutifc.interfaces.components.Record;
import com.qutifc.interfaces.components.Writer;
import com.qutifc.securitycontracts.annotations.MapsTo;

public class WriterCSV implements Writer {
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
		String result = null;

		int i = 0;
		Record currentRecord = this.reader.getNext();
		while (currentRecord != null) {
			if (i == 0) {
				result = this.join(",", currentRecord.getKeys(), currentRecord.getColumnCount()) + "\n";
			}

			result = result + this.join(",", currentRecord.getValues(), currentRecord.getColumnCount()) + "\n";

			i = i + 1;
			currentRecord = this.reader.getNext();
		}

		this.text = result;
	}

	@Override
	public String getText() {
		return this.text;
	}

	private String join(String delimiter, String[] strings, int stringCount) {
		int i = 0;
		String result = "";
		while (i < stringCount) {
			if (i == 0) {
				result = strings[i];
			} else {
				result = result + delimiter + strings[i];
			}

			i = i + 1;
		}

		return result;
	}

	@Override
	public String type() {
		return "csv";
	}
}
