package com.qutifc.implementations.parser.json.components;

import com.qutifc.interfaces.components.Parser;
import com.qutifc.interfaces.components.Record;
import com.qutifc.interfaces.components.Token;
import com.qutifc.interfaces.components.TokenCollection;
import com.qutifc.securitycontracts.annotations.MapsTo;

public class JSONParser implements Parser {
	@MapsTo(FieldName = "t")
	private TokenCollection tokens;
	@MapsTo(FieldName = "r")
	private Record[] records;
	@MapsTo(FieldName = "rc")
	private int numRecords;

	@Override
	public void setTokens(TokenCollection tokens) {
		this.tokens = tokens;
	}

	@Override
	public Record[] getRecords() {
		return this.records;
	}

	@Override
	public int getRecordCount() {
		return this.numRecords;
	}

	@Override
	public void parse() {
		int recordCount = 0;
		Record[] tempRecords = new Record[100];

		Token currentToken = this.tokens.getNext();
		while (currentToken != null) {
			String currentTokenName = currentToken.getName();
			if (currentTokenName.equals("OpenObject")) {
				String[] tempKeys = new String[100];
				String[] tempValues = new String[100];
				int keyCount = 0; // key count and value count should be the same

				Record record = Record.get();

				currentToken = this.tokens.getNext();
				currentTokenName = currentToken.getName();
				while (currentTokenName.equals("String")) {
					tempKeys[keyCount] = currentToken.getValue();
					currentToken = this.tokens.getNext();
					currentTokenName = currentToken.getName();
					if (currentTokenName.equals("KeyValSep")) {
						currentToken = this.tokens.getNext();
						currentTokenName = currentToken.getName();
						if (currentTokenName.equals("String")) {
							tempValues[keyCount] = currentToken.getValue();
						}
					}

					keyCount = keyCount + 1;

					// get rid of the separator ","
					currentToken = this.tokens.getNext();
					currentTokenName = currentToken.getName();

					if (currentTokenName.contentEquals("Separator")) {
						// move to the next key
						currentToken = this.tokens.getNext();
						currentTokenName = currentToken.getName();
					}
				}

				currentTokenName = currentToken.getName();
				if (currentTokenName.equals("CloseObject")) {
					String[] keys = new String[keyCount];
					String[] values = new String[keyCount];

					int i = 0;
					while (i < keyCount) {
						keys[i] = tempKeys[i];
						values[i] = tempValues[i];
						i = i + 1;
					}

					record.setKeys(keys);
					record.setValues(values);
					record.setColumnCount(keyCount);

					tempRecords[recordCount] = record;
					recordCount = recordCount + 1;
				}
			}

			currentToken = this.tokens.getNext();
		}

		Record[] result = new Record[recordCount];
		int i = 0;
		while (i < recordCount) {
			result[i] = tempRecords[i];
			i = i + 1;
		}

		this.numRecords = recordCount;
		this.records = result;
	}

	@Override
	public String type() {
		return "json";
	}
}
